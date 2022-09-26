package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public final class StandEntityUnsummon extends StandEntityAction {

    public StandEntityUnsummon() {
        super(new StandEntityAction.Builder().standUserSlowDownFactor(1.0F));
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        LivingEntity user = standEntity.getUser();
        if (user != null && (standEntity.isCloseToUser() || standEntity.isFollowingUser() || standEntity.unsummonTicks > 0)) {
            int maxTicks = getUnsummonDuration(standEntity);
            if (standEntity.unsummonTicks == maxTicks) {
                if (!world.isClientSide()) {
                    userPower.getType().forceUnsummon(user, userPower);
                }
            }
            else {
                if (!standEntity.isArmsOnlyMode() && standEntity.unsummonTicks == 0) {
                    standEntity.unsummonOffset = standEntity.getOffsetFromUser();
                }
                standEntity.unsummonTicks++;
            }
        }
        else {
            standEntity.unsummonTicks = 0;
        }
    }
    
    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        if (!standEntity.isArmsOnlyMode()) {
            int unsummonDuration = getUnsummonDuration(standEntity);
            if (unsummonDuration == 0) return super.getOffsetFromUser(standPower, standEntity, task);
            
            Vector3d offsetVec = standEntity.unsummonOffset.toRelativeVec();
            offsetVec = offsetVec.scale(1 - ((double) task.getTick() / (double) unsummonDuration));
            return standEntity.unsummonOffset.withRelativeVec(offsetVec);
        }
        return super.getOffsetFromUser(standPower, standEntity, task);
    }
    
    @Override
	protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, StandEntityAction newAction, Phase phase) {
        return !standEntity.isArmsOnlyMode() && newAction != this;
    }
    
    @Override
    public boolean canStaminaRegen(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    public int getUnsummonDuration(StandEntity standEntity) {
        return standEntity.isArmsOnlyMode() ? 10 : 15;
    }
    
    @Override
    protected void onTaskStopped(World world, StandEntity standEntity, IStandPower standPower, StandEntityTask task, @Nullable StandEntityAction newAction) {
        standEntity.unsummonTicks = 0;
        standEntity.unsummonOffset = standEntity.getDefaultOffsetFromUser().copy();
    }
    
    @Override
    public float getStandAlpha(StandEntity standEntity, int ticksLeft, float partialTick) {
        int maxTicks = getUnsummonDuration(standEntity);
        return (float) (maxTicks - standEntity.unsummonTicks) / (float) maxTicks;
    }
    
    @Override
    public SoundEvent getSoundOverride(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        return standEntity.getStandUnsummonSound();
    }
    
    @Override
    protected void playSoundAtStand(World world, StandEntity standEntity, SoundEvent sound, IStandPower standPower, Phase phase) {
        if (world.isClientSide()) {
            ClientTickingSoundsHelper.playStandEntityUnsummonSound(standEntity, sound, 1.0F, 1.0F);
        }
    }

    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return Integer.MAX_VALUE;
    }
}
