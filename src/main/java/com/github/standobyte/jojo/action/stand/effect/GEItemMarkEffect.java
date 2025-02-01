package com.github.standobyte.jojo.action.stand.effect;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.itemtracking.SidedItemTrackerMap;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;

public class GEItemMarkEffect extends StandEffectInstance {
    private UUID itemTrackerId = null;
    private TrackerItemStack itemTracker;
    
    public GEItemMarkEffect(UUID itemTrackerId) {
        this(ModStandEffects.GE_ITEM_MARK.get());
        this.itemTrackerId = itemTrackerId;
    }
    
    public GEItemMarkEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    @Nullable
    public TrackerItemStack getItemTracker(boolean update) {
        if (update) {
            itemTracker = SidedItemTrackerMap.getSidedTrackers(world).getTracker(itemTrackerId);
        }
        return itemTracker;
    }
    
    public UUID getItemTrackerId() {
        return itemTrackerId;
    }
    
    @Override
    protected void start() {}
    
    @Override
    protected void tick() {
        if (itemTrackerId == null) {
            if (!world.isClientSide()) {
                remove();
            }
            return;
        }
        
        if (!world.isClientSide()) {
            TrackerItemStack tracker = getItemTracker(true);
            if (tracker == null || !tracker.isTracked()) {
                remove();
            }
        }
    }

    @Override
    protected void stop() {
        if (!world.isClientSide()) {
            TrackerItemStack tracker = getItemTracker(true);
            if (tracker != null) {
                SidedItemTrackerMap serverItemTracking = SaveFileUtilCapProvider.getSaveFileCap((ServerWorld) world).getItemsTracker();
                serverItemTracking.removeTracker(tracker.getTrackerId());
            }
        }
    }

    @Override
    protected boolean needsTarget() {
        return false;
    }
    
    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        if (itemTrackerId != null) {
            nbt.putUUID("ItemTracker", itemTrackerId);
        }
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        if (nbt.hasUUID("ItemTracker")) {
            itemTrackerId = nbt.getUUID("ItemTracker");
            SidedItemTrackerMap serverItemTracking = SaveFileUtilCapProvider.getSaveFileCap((ServerWorld) world).getItemsTracker();
            serverItemTracking.addServerTrackedId(itemTrackerId);
        }
    }

    @Override
    public void writeAdditionalPacketData(PacketBuffer buf, boolean sendingToUser) {
        if (sendingToUser) {
            NetworkUtil.writeOptionally(buf, itemTrackerId, buf::writeUUID);
        }
    }

    @Override
    public void readAdditionalPacketData(PacketBuffer buf, boolean clientIsUser) {
        if (clientIsUser) {
            itemTrackerId = NetworkUtil.readOptional(buf, buf::readUUID).orElse(null);
        }
    }
    
    
    // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! item tracker capability isn't synced from a remote server
    public static boolean isItemMarked(ItemStack item, LivingEntity player) {
        return TrackerItemStack.getItemTracker(item).flatMap(tracker -> {
            if (tracker.isTracked()) {
                return IStandPower.getStandPowerOptional(player).map(power -> power
                        .getContinuousEffects()
                        .getEffects()
                        .filter(effect -> effect.effectType == ModStandEffects.GE_ITEM_MARK.get())
                        .map(effect -> (GEItemMarkEffect) effect)
                        .anyMatch(effect -> tracker.getTrackerId().equals(effect.getItemTrackerId())));
            }
            return Optional.of(false);
        }).orElse(false);
    }
}
