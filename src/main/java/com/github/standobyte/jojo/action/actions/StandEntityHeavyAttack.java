package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

/* FIXME (stats)
 *   damage: strength
 *   speed: speed
 *   
 *   replace cd with recovery ticks
 */
public class StandEntityHeavyAttack extends StandEntityAction {

    public StandEntityHeavyAttack(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        return performer instanceof StandEntity && !((StandEntity) performer).canAttackMelee() ? 
                ActionConditionResult.NEGATIVE
                : super.checkSpecificConditions(user, performer, power, target);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            if (ticks == getStandActionTicks(userPower, standEntity)) {
                standEntity.swingAlternateHands();
            }
            if (ticks == 1) {
                standEntity.punch(true);
            }
        }
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return Math.max((int) (standEntity.getTicksForSinglePunch()), 1);
    }
}
