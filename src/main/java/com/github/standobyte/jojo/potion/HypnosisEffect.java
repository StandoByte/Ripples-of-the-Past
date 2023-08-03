package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.EffectType;

public class HypnosisEffect extends UncurableEffect {
    
    public HypnosisEffect(int liquidColor) {
        super(EffectType.HARMFUL, liquidColor);
    }
    
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager modifiers, int amplifier) {
        super.removeAttributeModifiers(entity, modifiers, amplifier);
        entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.relieveHypnosis());
    }
    
    public static void hypnotizeEntity(LivingEntity target, LivingEntity hypnotizer, int duration) {
        target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.hypnotizeEntity(hypnotizer, duration));
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level.isClientSide() && entity.getRandom().nextFloat() < 0.05F) {
            HamonSparksLoopSound.playSparkSound(entity, entity.getBoundingBox().getCenter(), 1.0F, true);
            CustomParticlesHelper.createHamonSparkParticles(entity, entity.getRandomX(0.5), entity.getY(Math.random()), entity.getRandomZ(0.5), 1);
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
