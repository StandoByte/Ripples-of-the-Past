package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;

public class HamonSpreadEffect extends UncurableEffect implements IApplicableEffect {

    public HamonSpreadEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level.isClientSide() && livingEntity instanceof FlyingEntity) {
            double gravity = livingEntity.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
            Vector3d deltaMovement = livingEntity.getDeltaMovement();
            livingEntity.setDeltaMovement(new Vector3d(deltaMovement.x, -gravity * (amplifier + 1) * 0.5, deltaMovement.z));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return JojoModUtil.isUndead(entity);
    }
    
    
    
    public static float reduceUndeadHealing(EffectInstance effectInstance, float healAmount) {
        float multiplier = 1 - (float) Math.min(effectInstance.getAmplifier() + 1, 5) * 0.2F;
        return healAmount * multiplier;
    }
    
    public static void giveEffectTo(LivingEntity entity, int duration, int amplifier) {
        entity.addEffect(new EffectInstance(Effects.WEAKNESS, duration, amplifier, false, false, true));
        entity.addEffect(new EffectInstance(ModStatusEffects.HAMON_SPREAD.get(), duration, amplifier, false, false, false));
    }
}
