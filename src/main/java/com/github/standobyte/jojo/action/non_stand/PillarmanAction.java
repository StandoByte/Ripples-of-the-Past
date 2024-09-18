package com.github.standobyte.jojo.action.non_stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;

public abstract class PillarmanAction extends NonStandAction {
    protected PillarmanData.Mode mode;
    protected int stage;
    protected boolean canBeUsedInStone;
    
    public PillarmanAction(NonStandAction.Builder builder) {
        super(builder);
        mode = Mode.NONE;
        stage = -1;
        canBeUsedInStone = false;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!this.canBeUsedInStone && power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                .map(PillarmanData::isStoneFormEnabled).orElse(false)) {
            return conditionMessage("stone_form");
        }
        return super.checkConditions(user, power, target);
    }

    @Override
    public boolean isUnlocked(INonStandPower power) {
        PillarmanData pillarman = power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get();
        return (this.stage == -1 || this.stage <= pillarman.getEvolutionStage())
                && (this.mode == Mode.NONE || this.mode == pillarman.getMode());
    }
    
    
    public int getPillarManStage() {
        return stage;
    }
    
    @Nullable
    public PillarmanData.Mode getPillarManMode() {
        return mode;
    }
    
}
