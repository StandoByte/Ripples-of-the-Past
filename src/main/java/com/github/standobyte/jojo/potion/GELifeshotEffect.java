package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;

public class GELifeshotEffect extends StatusEffect implements IApplicableEffect {

    public GELifeshotEffect(int liquidColor) {
        super(EffectType.HARMFUL, liquidColor);
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; 
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
        return !(entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.instabuild)
                && !(entity instanceof MobEntity && ((MobEntity) entity).isNoAi());
    }
}
