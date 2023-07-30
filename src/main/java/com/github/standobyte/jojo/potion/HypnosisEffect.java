package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.init.ModEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public class HypnosisEffect extends UncurableEffect {
    
    public HypnosisEffect(int liquidColor) {
        super(EffectType.HARMFUL, liquidColor);
    }
    
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager modifiers, int amplifier) {
        super.removeAttributeModifiers(entity, modifiers, amplifier);
        relieveHypnosis(entity);
    }
    
    
    
    public static boolean canBeHypnotized(LivingEntity entity, LivingEntity hypnotizer) {
        return hypnotizer instanceof PlayerEntity && (entity instanceof TameableEntity || entity instanceof AbstractHorseEntity);
    }
    
    public static void hypnotizeEntity(LivingEntity target, LivingEntity hypnotizer, int duration) {
        duration = 80;
        
        if (!target.level.isClientSide()) {
            boolean giveEffect = false;
            
            if (hypnotizer instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) hypnotizer;
                if (target instanceof TameableEntity) {
                    TameableEntity tameable = (TameableEntity) target;
                    tameable.tame(player);
                    giveEffect = true;
                }
                
                else if (target instanceof AbstractHorseEntity) {
                    AbstractHorseEntity horse = (AbstractHorseEntity) target;
                    horse.tameWithName(player);
                    giveEffect = true;
                }
                
                target.level.broadcastEntityEvent(target, (byte) 7); // spawn tame heart particles
            }
            
            if (giveEffect) {
                target.addEffect(new EffectInstance(ModEffects.HYPNOSIS.get(), duration, 0, false, false, true));
            }
        }
    }
    
    public static void relieveHypnosis(LivingEntity entity) {
        if (!entity.level.isClientSide()) {
            if (entity instanceof TameableEntity) {
                TameableEntity tameable = (TameableEntity) entity;
                tameable.setTame(false);
                tameable.setOwnerUUID(null);
                tameable.setInSittingPose(false);
            }
            
            else if (entity instanceof AbstractHorseEntity) {
                AbstractHorseEntity horse = (AbstractHorseEntity) entity;
                horse.setTamed(false);
                horse.setOwnerUUID(null);
                horse.makeMad();
            }
            
            entity.level.broadcastEntityEvent(entity, (byte) 6); // spawn smoke particles
        }
    }
}
