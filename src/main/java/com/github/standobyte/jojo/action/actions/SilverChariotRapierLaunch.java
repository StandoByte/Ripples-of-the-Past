package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;

public class SilverChariotRapierLaunch extends StandEntityRangedAttack {

    public SilverChariotRapierLaunch(Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        if (performer instanceof SilverChariotEntity && !((SilverChariotEntity) performer).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkSpecificConditions(user, performer, power, target);
    }

}
