package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class StandEntityHeavyAttack extends StandEntityAction {

    public StandEntityHeavyAttack(StandEntityAction.Builder builder) {
        super(builder.standPose(StandPose.HEAVY_ATTACK).staminaCost(75F)
                .standOffsetFromUser(-0.75, 0.75).standTakesCrosshairTarget(TargetType.ENTITY));
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase) {
        standEntity.alternateHands();
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            standEntity.punch(standEntity.getComboMeter() >= 0.5F ? PunchType.HEAVY_COMBO : PunchType.HEAVY_NO_COMBO);
        }
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackWindup(standEntity.getAttackSpeed(), standEntity.getComboMeter());
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackRecovery(standEntity.getAttackSpeed());
    }
    
    @Override
    public boolean isCombatAction() {
        return true;
    }
    
    @Override
    public StandPose getStandPose(IStandPower standPower, StandEntity standEntity) {
        return standEntity.isHeavyPunchInCombo() ? StandPose.HEAVY_ATTACK_COMBO : super.getStandPose(standPower, standEntity);
    }
}
