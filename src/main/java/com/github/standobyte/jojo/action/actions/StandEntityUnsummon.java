package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public final class StandEntityUnsummon extends StandEntityAction {

    public StandEntityUnsummon() {
        super(new StandEntityAction.Builder().standPerformDuration(Integer.MAX_VALUE).standUserSlowDownFactor(1.0F));
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        LivingEntity user = standEntity.getUser();
        if (user != null && standEntity.isCloseToUser()) {
            int maxTicks = getUnsummonDuration(standEntity);
            if (standEntity.unsummonTicks == maxTicks) {
                if (!world.isClientSide()) {
                    userPower.getType().forceUnsummon(user, userPower);
                }
            }
            else {
                if (standEntity.isArmsOnlyMode()) {
                    standEntity.setTaskPosOffset(0, 0, 0);
                }
                else {
                    standEntity.tickUnsummonOffset();
                }
                standEntity.unsummonTicks++;
            }
        }
        else {
            standEntity.unsummonTicks = 0;
        }
    }
    
    @Override
    protected boolean allowArmsOnly() {
        return true;
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
    public void onClear(IStandPower standPower, StandEntity standEntity) {
        standEntity.unsummonTicks = 0;
        standEntity.unsummonOffset = standEntity.getDefaultOffsetFromUser().copy();
    }
    
    @Override
    public float getStandAlpha(StandEntity standEntity, int ticksLeft, float partialTick) {
        int maxTicks = getUnsummonDuration(standEntity);
        return (float) (maxTicks - standEntity.unsummonTicks) / (float) maxTicks;
    }
    
    @Override
    protected SoundEvent getSound(StandEntity standEntity, IStandPower standPower, Phase phase, ActionTarget target) {
        return standEntity.getStandUnsummonSound();
    }
    
    @Override
    protected void playSoundAtStand(World world, StandEntity standEntity, SoundEvent sound, IStandPower standPower, Phase phase) {
        if (world.isClientSide()) {
            ClientTickingSoundsHelper.playStandEntityUnsummonSound(standEntity, sound, 1.0F, 1.0F);
        }
    }
}
