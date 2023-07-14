package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.bowcharge.BowChargeEffectInstance;
import com.github.standobyte.jojo.power.bowcharge.IBowChargeEffect;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;

import net.minecraft.entity.LivingEntity;

public class HamonBowChargeEffect implements IBowChargeEffect<INonStandPower, NonStandPowerType<?>> {

    @Override
    public BowChargeEffectInstance<INonStandPower, NonStandPowerType<?>> createInstance(
            LivingEntity user, INonStandPower power, NonStandPowerType<?> type) {
     // FIXME !!!!!!!! specific instance?
        return new BowChargeEffectInstance<>(user, power, type);
    }
    
    @Override
    public boolean canStart(INonStandPower power) {
        return power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
            return hamon.isSkillLearned(ModHamonSkills.ARROW_INFUSION.get()); // FIXME !!!!!!!! energy condition
        }).orElse(false);
    }

}
