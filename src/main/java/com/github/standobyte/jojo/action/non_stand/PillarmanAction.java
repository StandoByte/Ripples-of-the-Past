package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;

public abstract class PillarmanAction extends NonStandAction {
     PillarmanData.Mode mode;
     int stage;
     boolean canBeUsedInStone;
    public PillarmanAction(NonStandAction.Builder builder) {
        super(builder);
        mode = Mode.NONE;
        stage = -1;
        canBeUsedInStone = false;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if(this.canBeUsedInStone == false && power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                .map(PillarmanData::isStoneFormEnabled).orElse(false)) {
            return conditionMessage("stone_form");
        }
        return super.checkConditions(user, power, target);
    }
    
    @Override
    public boolean isUnlocked(INonStandPower power) {
        PillarmanData pillarman = power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get();
        if (this.mode != Mode.NONE && this.mode != pillarman.getMode()) {
            return false;
        }
        if (this.stage != -1 && this.stage > pillarman.getEvolutionStage()) {
            return false;
        }
        return true;
    }
    
}
