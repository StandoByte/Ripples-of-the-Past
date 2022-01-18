package com.github.standobyte.jojo.action.actions;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

// FIXME smth like the voice line system for stands
public class StandEntityLightAttack extends StandEntityAction {

    public StandEntityLightAttack(StandEntityAction.Builder builder) {
        super(builder.standAutoSummonMode(AutoSummonMode.ONE_ARM).standUserSlowDownFactor(1.0F)
                .standOffsetFromUser(0, 0.15, true).yOffsetFromUser(0, true)
                .standTakesCrosshairTarget().standPose(StandPose.LIGHT_ATTACK));
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        return performer instanceof StandEntity && !((StandEntity) performer).canAttackMelee() ? 
                ActionConditionResult.NEGATIVE
                : super.checkSpecificConditions(user, performer, power, target);
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
        return StandStatFormulas.getLightAttackWindup(standEntity.getAttackSpeed());
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getLightAttackComboDelay(standEntity.getAttackSpeed());
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
    protected boolean isCombatAction() {
        return true;
    }
}
