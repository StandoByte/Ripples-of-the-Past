package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class FreezeEffect extends Effect implements IApplicableEffect {
    
    public FreezeEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "e30ee41c-6ea2-468c-99ab-fd0a7d6be8c3", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL).
        addAttributeModifier(Attributes.ATTACK_SPEED, "e4d278d8-a38b-434f-9c65-20c944abcff9", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level.isClientSide() && (livingEntity.getRemainingFireTicks() > 0 || livingEntity.level.dimensionType().ultraWarm())) {
            livingEntity.removeEffect(this);
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return true; }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return !DamageUtil.isImmuneToCold(entity);
    }
}
