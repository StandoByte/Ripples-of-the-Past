package com.github.standobyte.jojo.action.actions;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.Hand;
import net.minecraft.world.World;

// FIXME smth like the voice line system for stands
public class StandEntityLightAttack extends StandEntityAction {

    public StandEntityLightAttack(StandEntityAction.Builder builder) {
        super(builder.standAutoSummonMode(AutoSummonMode.ONE_ARM).staminaCost(20F).standUserSlowDownFactor(1.0F)
                .defaultStandOffsetFromUser().standOffsetFromUser(-0.75, 0.75)
                .standTakesCrosshairTarget().standPose(StandPose.LIGHT_ATTACK));
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase) {
        if (standEntity.isArmsOnlyMode() && standEntity.swingingArm == Hand.OFF_HAND) {
            standEntity.setArmsOnlyMode(true, true);
        }
        standEntity.alternateHands();
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            standEntity.punch(PunchType.LIGHT);
        }
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        float earlyStart = 0F;
        if (standEntity.getCurrentTaskAction() == this && standEntity.getCurrentTaskPhase() == Phase.RECOVERY) {
            earlyStart = 2F * (0.5F - standEntity.getCurrentTaskCompletion(0));
        }
        int ticks = StandStatFormulas.getLightAttackWindup(standEntity.getAttackSpeed(), earlyStart);
        return ticks;
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getLightAttackRecovery(standEntity.getAttackSpeed());
    }
    
    @Override
    public boolean isCancelable(IStandPower standPower, StandEntity standEntity, Phase phase, @Nullable StandEntityAction newAction) {
        if (phase == Phase.RECOVERY) {
            return newAction != null && newAction.isCombatAction();
        }
        return super.isCancelable(standPower, standEntity, phase, newAction);
    }
    
    @Override
    public boolean canBeScheduled(IStandPower standPower, StandEntity standEntity) {
        return standEntity.getCurrentTaskAction() != this;
    }
    
    @Override
    public boolean isCombatAction() {
        return true;
    }
}
