package com.github.standobyte.jojo.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraftforge.common.ForgeMod;

public class ImmobilizeEffect extends UncurableEffect implements IApplicableEffect {
    
    public ImmobilizeEffect(int liquidColor) {
        super(EffectType.HARMFUL, liquidColor);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "7abfcba6-295c-4310-9952-1c58d0eb58fb", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.FLYING_SPEED, "be6a9866-e25d-4466-9364-521b570c9b81", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, "018041e0-654d-48cf-a392-8dcb47ca48a3", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "f84c1dc3-7b4f-4d5d-8be0-e18c73cb6e59", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            player.abilities.flying = false;
        }
        if (resetsDeltaMovement()) {
            entity.setDeltaMovement(0, entity.getDeltaMovement().y < 0 ? entity.getDeltaMovement().y : 0, 0);
        }
    }
    
    public boolean resetsDeltaMovement() {
        return true;
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; 
    }
    
    @Override
    public boolean isApplicable(LivingEntity entity) {
        return !(entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.instabuild);
    }
    
}
