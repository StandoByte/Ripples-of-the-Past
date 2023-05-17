package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;

public class StandEntityBlock extends StandEntityAction {
    
    public StandEntityBlock() {
        this(new StandEntityAction.Builder().partsRequired(StandPart.ARMS));
    }

    protected StandEntityBlock(StandEntityAction.Builder builder) {
        super(builder.standAutoSummonMode(AutoSummonMode.ARMS).holdType().standPose(StandPose.BLOCK)
                .standOffsetFront().standUserWalkSpeed(0.3F).standOffsetFromUser(0, 0.3));
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return stand.canStartBlocking() || stand.isStandBlocking() ? ActionConditionResult.POSITIVE : ActionConditionResult.NEGATIVE;
    }
    
    @Override
    public boolean transfersPreviousOffset(IStandPower standPower, StandEntity standEntity, StandEntityTask previousTask) {
        return false;
    }
}
