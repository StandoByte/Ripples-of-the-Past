package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
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
        super(builder.standPose(StandPose.HEAVY_ATTACK).standUserSlowDownFactor(0.5F));
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        return performer instanceof StandEntity && !((StandEntity) performer).canAttackMelee() ? 
                ActionConditionResult.NEGATIVE
                : super.checkSpecificConditions(user, performer, power, target);
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase) {
        standEntity.alternateHands();
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            standEntity.punch(PunchType.HEAVY);
        }
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return 10;
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return 5;
    }
    
    @Override
    protected boolean isCombatAction() {
        return true;
    }
}
