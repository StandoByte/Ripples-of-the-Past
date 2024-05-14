package com.github.standobyte.jojo.action.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public final class StandEntityUnsummon extends StandEntityAction {

    public StandEntityUnsummon() {
        super(new StandEntityAction.Builder().standUserWalkSpeed(1.0F));
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        LivingEntity user = standEntity.getUser();
        if (user != null && (standEntity.isCloseToUser() || standEntity.isFollowingUser() || standEntity.unsummonTicks > 0)) {
            int maxTicks = standEntity.getUnsummonDuration();
            if (standEntity.unsummonTicks >= maxTicks) {
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
            int unsummonDuration = standEntity.getUnsummonDuration();
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
    
    @Override
    protected void onTaskStopped(World world, StandEntity standEntity, IStandPower standPower, StandEntityTask task, @Nullable StandEntityAction newAction) {
        standEntity.unsummonTicks = 0;
        standEntity.unsummonOffset = standEntity.getDefaultOffsetFromUser().copy();
    }
    
    @Override
    public float getStandAlpha(StandEntity standEntity, int ticksLeft, float partialTick) {
        int maxTicks = standEntity.getUnsummonDuration();
        return (float) (maxTicks - standEntity.unsummonTicks) / (float) maxTicks;
    }
    
    @Override
    public void playSound(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        playSoundAtStand(standEntity.level, standEntity, standEntity.getStandUnsummonSound(), standPower, phase);
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
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        if (world.isClientSide()) {
            LivingEntity user = standPower.getUser();
            if (user != null && user == ClientUtil.getClientPlayer() && !standEntity.isArmsOnlyMode()) {
                ActionsOverlayGui.getInstance().onStandUnsummon();
            }
        }
    }
}
