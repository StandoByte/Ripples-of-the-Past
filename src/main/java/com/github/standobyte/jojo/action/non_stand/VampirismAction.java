package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;

public abstract class VampirismAction extends NonStandAction {
    
    public VampirismAction(NonStandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).get().getCuringStage() > maxCuringStage()) {
            return ActionConditionResult.NEGATIVE;
        }
        return super.checkConditions(user, power, target);
    }
    
    protected int maxCuringStage() {
        return Integer.MAX_VALUE;
    }
}
