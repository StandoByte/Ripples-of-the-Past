package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class StaminaRegenEffect extends Effect {

    public StaminaRegenEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        IStandPower.getStandPowerOptional(entity).ifPresent(power -> {
            power.addStamina((amplifier + 1) * power.getMaxStamina() / 1000F, false);
        });
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

}
