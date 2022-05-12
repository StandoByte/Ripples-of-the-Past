package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

public class StarPlatinumBarrage extends StandEntityMeleeBarrage {

    public StarPlatinumBarrage(Builder builder) {
        super(builder);
    }

    @Override
    public boolean standTakesCrosshairTarget(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
        if (target.getType() == TargetType.ENTITY) {
            return !standEntity.isArmsOnlyMode() && standPower.getResolveLevel() >= 3;
        }
        return super.standTakesCrosshairTarget(target, standEntity, standPower);
    }
}
