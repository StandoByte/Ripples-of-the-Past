package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StandEntityBlock extends StandEntityAction {
    private static final StandEntityAction.Builder BUILDER = new StandEntityAction.Builder().doNotAutoSummonStand().holdType(0);

    public StandEntityBlock() {
        super(BUILDER);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        if (!(performer instanceof StandEntity)) {
            return ActionConditionResult.POSITIVE;
        }
        StandEntity stand = (StandEntity) performer;
        if (stand.canStartBlocking() || stand.isStandBlocking()) {
            return ActionConditionResult.POSITIVE;
        }
        return ActionConditionResult.NEGATIVE;
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, IStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            StandEntity stand;
            if (power.isActive()) {
                stand = (StandEntity) getPerformer(user, power);
            }
            else {
                ((EntityStandType) power.getType()).summon(user, power, entity -> {
                    entity.setArmsOnlyMode();
                }, true);
                stand = (StandEntity) power.getStandManifestation();
            }
            stand.blockTaskManual();
        }
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, IStandPower power, int ticksHeld) {
        if (!world.isClientSide() && power.isActive()) {
            ((StandEntity) power.getStandManifestation()).clearTask();
        }
    }
}
