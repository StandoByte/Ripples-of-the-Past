package com.github.standobyte.jojo.action.actions;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class StandEntityLightAttack extends StandEntityAction {

    public StandEntityLightAttack(StandEntityAction.Builder builder) {
        super(builder.standAutoSummonMode(AutoSummonMode.ONE_ARM).staminaCost(10F).standUserSlowDownFactor(1.0F)
                .standOffsetFront().standOffsetFromUser(-0.75, 0.75)
                .standTakesCrosshairTarget().standPose(StandPose.LIGHT_ATTACK));
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, ActionTarget target, int ticks) {
        if (standEntity.isArmsOnlyMode() && standEntity.swingingArm == Hand.OFF_HAND) {
            standEntity.setArmsOnlyMode(true, true);
        }
        standEntity.alternateHands();
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            standEntity.punch(PunchType.LIGHT, target, this);
        }
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackWindup(speed, standEntity.getComboMeter());
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackRecovery(speed, standEntity.getComboMeter());
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackRecovery(speed, standEntity.getComboMeter())
                * (standEntity.isArmsOnlyMode() ? 2 : 4);
    }
    
    @Override
    public void standTickRecovery(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (ticks == 0) {
            standEntity.clickQueuedAction(standEntity.getCurrentTask().get());
        }
    }
    
    @Override
    protected SoundEvent getSound(StandEntity standEntity, IStandPower standPower, Phase phase, ActionTarget target) {
        return target.getType() != TargetType.ENTITY || standEntity.isArmsOnlyMode()
        		? null : super.getSound(standEntity, standPower, phase, target);
    }
    
    @Override
    public boolean isCancelable(IStandPower standPower, StandEntity standEntity, Phase phase, @Nullable StandEntityAction newAction) {
        if (phase == Phase.RECOVERY) {
            return true;
        }
        return super.isCancelable(standPower, standEntity, phase, newAction);
    }
    
    @Override
    protected boolean canQueue(StandEntityAction nextAction, IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    @Override
    public boolean isCombatAction() {
        return true;
    }
}
