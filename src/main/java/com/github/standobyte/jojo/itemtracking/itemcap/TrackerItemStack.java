package com.github.standobyte.jojo.itemtracking.itemcap;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.UUID;
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
    
    public TrackerItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    public static boolean setTracked(ItemStack itemStack, ServerPlayerEntity player) {
        if (itemStack.getCount() != 1) {
            throw new IllegalArgumentException("Cannot track stacked items, only item stacks with count == 1 are supported");
        }
        return itemStack.getCapability(TrackerItemStackProvider.CAPABILITY).map(cap -> {
            if (cap.trackerUuid == null) {
                cap.trackerUuid = MathHelper.createInsecureUUID(RANDOM);
                cap.trackingPlayerId = player.getUUID();
                SaveFileUtilCapProvider.getSaveFileCap(player).getItemsTracker().addTracker(cap.trackerUuid, cap);
                return true;
            }
            return false;
        }).orElse(false);
    }
    
    public static void updateItemAtEntity(World world, ItemStack itemStack, int entityId) {
        if (itemStack.isEmpty() || world.isClientSide()) return;
        itemStack.getCapability(TrackerItemStackProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.isTracked()) {
                cap.setAtEntity(entityId);
                cap.onUpdate((ServerWorld) world);
            }
        });
    }
    
    public static void updateItemAtBlock(World world, ItemStack itemStack, BlockPos blockPos) {
        if (itemStack.isEmpty() || world.isClientSide()) return;
        itemStack.getCapability(TrackerItemStackProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.isTracked()) {
                cap.setAtBlockPos(blockPos);
                cap.onUpdate((ServerWorld) world);
            }
        });
    }
    
    public static void updateDisappeared(World world, ItemStack itemStack) {
        if (world.isClientSide()) return;
        itemStack.getCapability(TrackerItemStackProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.isTracked()) {
                cap.positionEntity = OptionalInt.empty();
                cap.positionBlock = null;
                cap.onUpdate((ServerWorld) world);
            }
        });
    }
    
    public static void trackedInEntityInv(ItemStack setItem, Stream<ItemStack> inventoryItems, 
            World world, int entityId) {
        trackedInInventory(setItem, inventoryItems, world, OptionalInt.of(entityId), null);
    }
    
    public static void trackedInBlockInv(ItemStack setItem, Stream<ItemStack> inventoryItems, 
            World world, BlockPos blockPos) {
        trackedInInventory(setItem, inventoryItems, world, OptionalInt.empty(), blockPos);
    }
    
    private static void trackedInInventory(ItemStack setItem, Stream<ItemStack> inventoryItems, 
            World world, OptionalInt entityId, BlockPos blockPos) {
        if (!world.isClientSide()) {
            setItem.getCapability(TrackerItemStackProvider.CAPABILITY).ifPresent(oldItemTracker -> {
                if (oldItemTracker.isTracked()) {
                    UUID trackerId = oldItemTracker.getTrackerId();
                    inventoryItems.anyMatch(movedItem -> {
                        return movedItem.getCapability(TrackerItemStackProvider.CAPABILITY).map(newTracker -> {
                            if (trackerId.equals(newTracker.getTrackerId())) {
                                newTracker.positionEntity = entityId;
                                newTracker.positionBlock = blockPos;
                                newTracker.onUpdate((ServerWorld) world);
                                return true;
                            }
                            return false;
                        }).orElse(false);
                    });
                }
            });
        }
    }
    
    public void onUpdate(ServerWorld world) {
        SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getItemsTracker().addTracker(trackerUuid, this);
        if (trackingPlayerId != null) {
            PlayerEntity player = world.getPlayerByUUID(trackingPlayerId);
            if (player instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(new TrackedItemPacket(
                        trackerUuid, itemStack, positionEntity, Optional.ofNullable(positionBlock)), 
                        (ServerPlayerEntity) player);
            }
        }
    }
    
    public void setAtEntity(int entityId) {
        this.positionEntity = OptionalInt.of(entityId);
        this.positionBlock = null;
    }
    
    public void setAtBlockPos(BlockPos blockPos) {
        this.positionEntity = OptionalInt.empty();
        this.positionBlock = blockPos;
    }
    
    @Nullable
    public Entity getAtEntity(World world) {
        return positionEntity.isPresent() ? world.getEntity(positionEntity.getAsInt()) : null;
    }
    
    @Nullable
    public BlockPos getAtBlockPos(World world) {
        return positionBlock;
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
