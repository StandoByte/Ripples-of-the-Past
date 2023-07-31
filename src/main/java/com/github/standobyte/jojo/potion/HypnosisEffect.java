package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;

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
}
