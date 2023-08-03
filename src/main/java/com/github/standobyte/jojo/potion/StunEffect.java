package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.monster.CreeperEntity;

public class StunEffect extends ImmobilizeEffect implements IApplicableEffect {

    public StunEffect(int liquidColor) {
        super(liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
        if (entity instanceof CreeperEntity) {
            CreeperEntity creeper = (CreeperEntity) entity;
            CommonReflection.setCreeperSwell(creeper, -1);
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager modifiers, int amplifier) {
        super.addAttributeModifiers(entity, modifiers, amplifier);
        if (entity instanceof MobEntity) {
            ((MobEntity) entity).setNoAi(true);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager modifiers, int amplifier) {
        super.removeAttributeModifiers(entity, modifiers, amplifier);
        if (entity instanceof MobEntity) {
            ((MobEntity) entity).setNoAi(false);
        }
    }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return super.isApplicable(entity) && !(entity instanceof MobEntity && ((MobEntity) entity).isNoAi());
    }
}
