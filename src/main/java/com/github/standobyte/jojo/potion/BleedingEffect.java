package com.github.standobyte.jojo.potion;

import java.util.UUID;

import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class BleedingEffect extends Effect implements IApplicableEffect {
    public static final UUID ATTRIBUTE_MODIFIER_ID = UUID.fromString("1588be77-b81b-4eb0-a745-a8912de51e72");
    
    public BleedingEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
        getAttributeModifiers().put(Attributes.MAX_HEALTH, new AttributeModifier(ATTRIBUTE_MODIFIER_ID, 
                this::getDescriptionId, -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(entity, pAttributeMap, pAmplifier);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
        
        if (entity.level.isClientSide()) {
            IStandPower.getStandPowerOptional(entity).ifPresent(power -> {
                if (ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get().isUnlocked(power)) {
                    power.setCooldownTimer(ModStandsInit.CRAZY_DIAMOND_BLOOD_CUTTER.get(), 0);
                }
            });
        }
    }
    
    @Override
    public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
        return super.getAttributeModifierValue(Math.min(pAmplifier, 3), pModifier);
    }
    
    @Override
    public boolean isApplicable(LivingEntity entity) {
        return JojoModUtil.canBleed(entity);
    }
}
