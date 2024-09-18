package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class PillarmanStoneForm extends PillarmanAction {

    public PillarmanStoneForm(PillarmanAction.Builder builder) {
        super(builder);
        stage = 1;
        canBeUsedInStone = true;
    }

//    @Override
//    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
//        return ActionConditionResult.POSITIVE;
//    }

    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {  
        if (!world.isClientSide()) {
            power.getTypeSpecificData(ModPowers.PILLAR_MAN.get()).get().toggleStoneForm();
        }
    }
    
    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return power.getTypeSpecificData(ModPowers.PILLAR_MAN.get())
                .map(PillarmanData::isStoneFormEnabled).orElse(false);
    }
}
