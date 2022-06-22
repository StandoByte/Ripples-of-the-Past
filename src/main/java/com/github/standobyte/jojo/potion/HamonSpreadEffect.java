package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;

public class HamonSpreadEffect extends UncurableEffect implements IApplicableEffect {

    public HamonSpreadEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }
    
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        DamageUtil.dealHamonDamage(livingEntity, 1F, null, null);
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

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return JojoModUtil.isUndead(entity);
    }
}
