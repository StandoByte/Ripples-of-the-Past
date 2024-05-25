package com.github.standobyte.jojo.itemtracking.itemcap;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrackedItemPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TrackerItemStack {
    private static final Random RANDOM = new Random();
    private final ItemStack itemStack;
    @Nullable private UUID trackerUuid; // TODO replace UUID with int?
    private UUID trackingPlayerId;
    
    private OptionalInt positionEntity = OptionalInt.empty();
    private BlockPos positionBlock = null;
    private Predicate<UUID> itemStillThere;
    
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
                SaveFileUtilCapProvider.getSaveFileCap(player).getItemsTracker().updateTracker(cap.trackerUuid, cap, player.level);
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
    public static Optional<TrackerItemStack> getItemTrackerInInventory(ItemStack originalItemStack, Stream<ItemStack> inventoryItems) {
        return getItemTracker(originalItemStack, true).flatMap(oldTracker -> {
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
    
    public void setItemStillThereCheck(Predicate<UUID> check) {
        this.itemStillThere = check;
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
    
    public void setAtEntity(int entityId, World world) {
        this.positionEntity = OptionalInt.of(entityId);
        this.positionBlock = null;
        if (!world.isClientSide()) {
            onUpdate((ServerWorld) world);
        }
    }
    
    public void setAtBlockPos(BlockPos blockPos, World world) {
        this.positionEntity = OptionalInt.empty();
        this.positionBlock = blockPos;
        if (!world.isClientSide()) {
            onUpdate((ServerWorld) world);
        }
    }
    
    public void setDisappeared(ServerWorld world) {
        positionEntity = OptionalInt.empty();
        positionBlock = null;
        itemStillThere = null;
        onUpdate(world);
    }
    
    @Nullable
    public Entity getAtEntity(World world) {
        return positionEntity.isPresent() ? world.getEntity(positionEntity.getAsInt()) : null;
    }
    
    @Nullable
    public BlockPos getAtBlockPos(World world) {
        return positionBlock;
    }
    
    public void tick(ServerWorld world) {
        if (itemStillThere != null && !itemStillThere.test(trackerUuid)) {
            setDisappeared(world);
        }
    }
    
    public void clear() {
        trackerUuid = null;
    }
    
    public Vector3d markerPos(World world, float partialTick) {
        if (positionEntity.isPresent()) {
            Entity entity = world.getEntity(positionEntity.getAsInt());
            if (entity != null) {
                return entity.getPosition(partialTick).add(0, entity.getBbHeight() + 0.25, 0);
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
}
