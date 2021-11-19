package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.task.MeleeAttackTask;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StandEntityMeleeAttack extends StandEntityAction {

    public StandEntityMeleeAttack(StandEntityAction.Builder builder) {
        super(builder);
        this.doNotAutoSummonStand = true;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        return performer instanceof StandEntity && !((StandEntity) performer).canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkConditions(user, performer, power, target);
    }

    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            StandEntity stand;
            if (power.isActive()) {
                stand = (StandEntity) getPerformer(user, power);
            }
            else {
                IStandPower standPower = ((IStandPower) power);
                ((EntityStandType) power.getType()).summon(user, standPower, entity -> {
                    entity.setArmsOnlyMode(true, false);
                }, true);
                stand = ((StandEntity) standPower.getStandManifestation());
            }
            stand.setTask(new MeleeAttackTask(stand, false));
            stand.setTaskTarget(target);
        }
    }
}
