package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;

public class StandEntityBlock extends StandEntityAction {
    
    public StandEntityBlock() {
        this(new StandEntityAction.Builder());
    }

    protected StandEntityBlock(StandEntityAction.Builder builder) {
        super(builder.standAutoSummonMode(AutoSummonMode.ARMS).holdType().standPose(StandPose.BLOCK)
                .defaultStandOffsetFromUser().standUserSlowDownFactor(0.3F).standOffsetFromUser(0, 0.3));
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
    protected boolean isCombatAction() {
        return true;
    }
}
