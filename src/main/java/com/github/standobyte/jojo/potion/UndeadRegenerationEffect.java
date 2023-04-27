package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class UndeadRegenerationEffect extends Effect implements IApplicableEffect {

    public UndeadRegenerationEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int k = 50 >> amplifier;
        if (k > 0) {
            return duration % k == 0;
        }
        else {
            return true;
        }
    }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return JojoModUtil.isUndead(entity);
    }
}
