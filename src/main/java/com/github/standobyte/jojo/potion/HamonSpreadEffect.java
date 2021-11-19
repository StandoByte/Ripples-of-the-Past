package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.damage.ModDamageSources;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;

public class HamonSpreadEffect extends UncurableEffect {

    public HamonSpreadEffect() {
        super(EffectType.HARMFUL, 0xFDF34B);
    }
    
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        ModDamageSources.dealHamonDamage(livingEntity, 1F, null, null);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int j = 25 >> amplifier;
        if (j > 0) {
            return duration % j == 0;
        } else {
            return true;
        }
    }
}
