package com.github.standobyte.jojo.itemtracking.itemcap;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrackedItemPacket;

import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Currently item stacks are being tracked in:
 *   ItemEntity
 *   LockableLootTileEntity
 *   Hoppers
 *   Player inventory
 *   Horse chest inventory
 *   Minecart with chest inventory
 *   Mobs equipment
 *   Armor stands equipment
 *   Shot arrows, knives, clackers, blade hats
 *
 */
public class TrackerItemStack {
    private static final Random RANDOM = new Random();
    private final ItemStack itemStack;
    @Nullable private UUID trackerUuid;
    private UUID trackingPlayerId;
    
    private RegistryKey<World> positionDimension;
    private OptionalInt positionEntity = OptionalInt.empty();
    private BlockPos positionBlock = null;
    private BlockState containerBlockState;
    private Predicate<UUID> itemStillThere;
    private KnownItemState itemState;
    
    public TrackerItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    public TrackerItemStack(ItemStack itemStack, UUID trackerId) {
        this.itemStack = itemStack;
        this.trackerUuid = trackerId;
    }
    
    @Nullable
    public static TrackerItemStack setTracked(ItemStack itemStack, ServerPlayerEntity player) {
        return setTracked(itemStack, player, MathHelper.createInsecureUUID(RANDOM));
    }
    
    @Nullable
    public static TrackerItemStack setTracked(ItemStack itemStack, ServerPlayerEntity player, UUID trackerId) {
        if (itemStack.getCount() != 1) {
            throw new IllegalArgumentException("Cannot track stacked items, only item stacks with count == 1 are supported");
        }
        return itemStack.getCapability(TrackerItemStackProvider.CAPABILITY).resolve().map(cap -> {
            if (cap.trackerUuid == null) {
                cap.trackerUuid = trackerId;
                cap.trackingPlayerId = player.getUUID();
                SidedItemTrackerMap serverItemTracking = SaveFileUtilCapProvider.getSaveFileCap(player).getItemsTracker();
                serverItemTracking.addServerTrackedId(cap.trackerUuid);
                serverItemTracking.updateTracker(cap.trackerUuid, cap, player.level);
            }
            return cap;
        }).orElse(null);
    }
    
    public static Optional<TrackerItemStack> getItemTracker(ItemStack itemStack) {
        return getItemTracker(itemStack, false);
    }
    
    public static Optional<TrackerItemStack> getItemTracker(ItemStack itemStack, boolean allowEmpty) {
        if (!allowEmpty && itemStack.isEmpty()) {
            return Optional.empty();
        }
        return itemStack.getCapability(TrackerItemStackProvider.CAPABILITY).resolve().map(
                cap -> cap.isTracked() ? cap : null);
    }
    
    /* when an item is being added to inventory, the original ItemStack's count is being taken from (to split the item between slots),
     * so we have to find the new ItemStack inside the inventory first
     */
    public static Optional<TrackerItemStack> getItemTrackerInInventory(ItemStack originalItemStack, Stream<ItemStack> inventoryItems, boolean allowEmpty) {
        return getItemTracker(originalItemStack, allowEmpty).flatMap(oldTracker -> {
            UUID trackerId = oldTracker.getTrackerId();
            Optional<TrackerItemStack> newTracker = inventoryItems
                    .map(movedItem -> movedItem.getCapability(TrackerItemStackProvider.CAPABILITY).resolve().map(tracker -> {
                        if (trackerId.equals(tracker.getTrackerId())) {
                            return tracker;
                        }
                        return null;
                    }))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
            return newTracker;
        });
    }
    
    public static Predicate<ItemStack> trackerIdCheck(UUID trackerId) {
        return invItem -> hasTrackerId(invItem, trackerId);
    }
    
    public static boolean hasTrackerId(ItemStack item, UUID trackerId) {
        return trackerId.equals(TrackerItemStack.getItemTracker(item).map(TrackerItemStack::getTrackerId).orElse(null));
    }
    
    public void onUpdate(ServerWorld world) {
        SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getItemsTracker().updateTracker(trackerUuid, this, world);
        if (trackingPlayerId != null) {
            PlayerEntity player = world.getPlayerByUUID(trackingPlayerId);
            if (player instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(new TrackedItemPacket(
                        trackerUuid, itemStack, positionEntity, Optional.ofNullable(positionBlock)), 
                        (ServerPlayerEntity) player);
            }
        }
    }
    
    public void onShrink(ServerWorld world) {
        if (positionBlock != null) {
            TileEntity tileEntity = world.getBlockEntity(positionBlock);
            if (tileEntity instanceof JukeboxTileEntity) {
                JukeboxTileEntity jukebox = (JukeboxTileEntity) tileEntity;
                BlockState blockState = world.getBlockState(positionBlock);
                world.levelEvent(1010, positionBlock, 0);
                jukebox.clearContent();
                blockState = blockState.setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(false));
                world.setBlock(positionBlock, blockState, 2);
            }
        }
        else if (positionEntity.isPresent()) {
            Entity entity = getAtEntity(world);
            if (entity instanceof ItemFrameEntity) {
                ItemFrameEntity itemFrame = (ItemFrameEntity) entity;
                itemFrame.setItem(ItemStack.EMPTY);
            }
        }
    }
    
    public void setAtEntity(int entityId, World world, KnownItemState itemState) {
        JojoMod.LOGGER.debug("entity {}", entityId);
        this.positionEntity = OptionalInt.of(entityId);
        this.positionBlock = null;
        this.containerBlockState = null;
        this.positionDimension = world.dimension();
        this.itemState = itemState;
        if (!world.isClientSide()) {
            onUpdate((ServerWorld) world);
        }
    }
    
    public void setAtBlockPos(BlockPos blockPos, World world, KnownItemState itemState) {
        JojoMod.LOGGER.debug("block {}", blockPos);
        this.positionEntity = OptionalInt.empty();
        this.positionBlock = blockPos;
        this.containerBlockState = world.getBlockState(blockPos);
        this.positionDimension = world.dimension();
        this.itemState = itemState;
        if (!world.isClientSide()) {
            onUpdate((ServerWorld) world);
        }
    }
    
    public void setItemStillThereCheck(@Nullable Predicate<UUID> check) {
        this.itemStillThere = check;
    }
    
    public void setDisappeared(ServerWorld world) {
        JojoMod.LOGGER.debug("a gde");
        this.positionEntity = OptionalInt.empty();
        this.positionBlock = null;
        this.containerBlockState = null;
        this.positionDimension = null;
        this.itemStillThere = null;
        this.itemState = null;
        onUpdate(world);
    }
    
    @Nullable
    public Entity getAtEntity(World world) {
        return positionEntity.isPresent() ? world.getEntity(positionEntity.getAsInt()) : null;
    }
    
    public OptionalInt getAtEntityId() {
        return positionEntity;
    }
    
    @Nullable
    public BlockPos getAtBlockPos() {
        return positionBlock;
    }
    
    @Nullable
    public KnownItemState getItemState() {
        return itemState;
    }
    
    public void tick(MinecraftServer server) {
        if (this.positionDimension != null) {
            ServerWorld world = server.getLevel(positionDimension);
            if (world != null && !checkItemIsThere(world)) {
                setDisappeared(world);
            }
        }
    }
    
    public boolean checkItemIsThere(ServerWorld world) {
        if (this.positionDimension == null) return false;
        
        if (positionEntity.isPresent()) {
            Entity entity = world.getEntity(positionEntity.getAsInt());
            if (entity == null || entity.removed) {
                return false;
            }
        }
        else if (positionBlock != null && containerBlockState != null) {
            BlockState blockState = world.getBlockState(positionBlock);
            if (this.containerBlockState.getBlock() != blockState.getBlock()) {
                return false;
            }
        }
        
        return itemStillThere == null || itemStillThere.test(trackerUuid);
    }
    
    public void copy(TrackerItemStack oldTracker) {
        this.trackerUuid = oldTracker.trackerUuid;
        this.trackingPlayerId = oldTracker.trackingPlayerId;
        
        this.positionDimension = oldTracker.positionDimension;
        this.positionEntity = oldTracker.positionEntity;
        this.positionBlock = oldTracker.positionBlock;
        this.containerBlockState = oldTracker.containerBlockState;
        this.itemStillThere = oldTracker.itemStillThere;
        this.itemState = oldTracker.itemState;
    }
    
    public void clear() {
        this.trackerUuid = null;
        
        this.trackingPlayerId = null;
        
        this.positionDimension = null;
        this.positionEntity = OptionalInt.empty();
        this.positionBlock = null;
        this.containerBlockState = null;
        this.itemStillThere = null;
        this.itemState = null;
    }
    
    public void moveToItem(ItemStack newItem, ServerWorld world) {
        TrackerItemStack.getItemTracker(newItem).ifPresent(newTracker -> {
            newTracker.copy(this);
            SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getItemsTracker().updateTracker(newTracker.getTrackerId(), newTracker, world);
        });
//        forceOldItemNbtToSync();
        this.clear();
    }
    
    public Vector3d markerPos(World world, float partialTick) {
        if (positionEntity.isPresent()) {
            Entity entity = world.getEntity(positionEntity.getAsInt());
            if (entity != null) {
                Vector3d position;
                if (entity.level.isClientSide()) {
                    position = entity.getPosition(partialTick);
                }
                else {
                    position = entity.position();
                }
                return position.add(0, entity.getBbHeight() + 0.25, 0);
            }
        }
        if (positionBlock != null) {
            return Vector3d.upFromBottomCenterOf(positionBlock, 1.0);
        }
        
        return null;
    }
    
    public ItemStack getItem() {
        return itemStack;
    }
    
    public boolean isTracked() {
        return trackerUuid != null;
    }
    
    public UUID getTrackerId() {
        return trackerUuid;
    }
    
    
    public INBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (trackerUuid != null) {
            nbt.putUUID("Id", trackerUuid);
            if (trackingPlayerId != null) {
                nbt.putUUID("Player", trackingPlayerId);
            }
        }
        return nbt;
    }
    
    public void fromNBT(INBT inbt) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        if (nbt.hasUUID("Id")) {
            trackerUuid = nbt.getUUID("Id");
            if (nbt.hasUUID("Player")) {
                trackingPlayerId = nbt.getUUID("Player");
            }
        }
    }
    
    
    public static enum KnownItemState {
        ENTITY_IS_ITEM,
        ENTITY_HAS_ITEM,
        STUCK_ARROW,
        STUCK_KNIFE,
        BLOCK_IS_ITEM,
        BLOCK_HAS_ITEM
    }
}
