package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.power.stand.IStandPower;

public class StarPlatinumBarrage extends StandEntityMeleeBarrage {

    public StarPlatinumBarrage(Builder builder) {
        super(builder);
    }

    @Override
    protected boolean standTakesCrosshairTarget(ActionTarget target, IStandPower standPower) {
        if (target.getType() == TargetType.ENTITY) {
            return standPower.getResolveLevel() >= 3;
        }
        return super.standTakesCrosshairTarget(target, standPower);
    }
}
