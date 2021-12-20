package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.task.RangedAttackTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StandEntityHeldRangedAttack extends StandEntityAction {

    public StandEntityHeldRangedAttack(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        return performer instanceof StandEntity && !((StandEntity) performer).canAttackRanged() ? 
                ActionConditionResult.NEGATIVE
                : super.checkConditions(user, performer, power, target);
    }

    @Override
    public void onStartedHolding(World world, LivingEntity user, IStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            LivingEntity entity = getPerformer(user, power);
            if (entity instanceof StandEntity) {
                StandEntity stand = (StandEntity) getPerformer(user, power);
                stand.setTask(new RangedAttackTask(stand, isShiftVariation(), true));
            }
        }
    }
    
    @Override
    public void onStoppedHolding(World world, LivingEntity user, IStandPower power, int ticksHeld) {
        if (!world.isClientSide() && power.isActive()) {
            ((StandEntity) power.getStandManifestation()).clearTask();
        }
    }
}
