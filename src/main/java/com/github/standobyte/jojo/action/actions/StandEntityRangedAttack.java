package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.task.RangedAttackTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StandEntityRangedAttack extends StandEntityAction {

    public StandEntityRangedAttack(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        return performer instanceof StandEntity && !((StandEntity) performer).canAttackRanged() ? 
                ActionConditionResult.NEGATIVE
                : super.checkConditions(user, performer, power, target);
    }

    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            StandEntity stand = (StandEntity) getPerformer(user, power);
            stand.setTask(new RangedAttackTask(stand, isShiftVariation(), false));
        }
    }
}
