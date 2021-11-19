package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.SilverChariotEntity;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;

public class SilverChariotRapierLaunch extends StandEntityRangedAttack {

    public SilverChariotRapierLaunch(Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        if (performer instanceof SilverChariotEntity && !((SilverChariotEntity) performer).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkConditions(user, performer, power, target);
    }

}
