package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.EffectType;

public class ResolveEffect extends UncurableEffect {

    public ResolveEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        IStandPower.getStandPowerOptional(entity).ifPresent(stand -> {
            if (stand.usesResolve() && stand.getResolve() < stand.getMaxResolve()) {
                stand.setResolveLevel(amplifier + 1);
                stand.setResolve(stand.getMaxResolve(), -999);
            }
        });
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        IStandPower.getStandPowerOptional(entity).ifPresent(stand -> {
            if (stand.usesResolve()) {
                stand.setResolve(0, 0);
            }
        });
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.isInvisible()) {
            entity.level.addParticle(ModParticles.RESOLVE.get(), 
                    entity.getRandomX(2.5D), entity.getY(entity.getRandom().nextDouble() * 1.5), entity.getRandomZ(2.5D), 0, 0, 0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 3 == 0;
    }
}
