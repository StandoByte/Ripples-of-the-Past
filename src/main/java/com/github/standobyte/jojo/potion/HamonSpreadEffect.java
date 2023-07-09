package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;

public class HamonSpreadEffect extends UncurableEffect implements IApplicableEffect {

    public HamonSpreadEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    // FIXME !!! (hamon) hamon spread perk rework
    // reduce healing
    // do smth to mobs too
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
//        DamageUtil.dealHamonDamage(livingEntity, 1F, null, null);
        if (!livingEntity.level.isClientSide() && livingEntity instanceof FlyingEntity) {
            double gravity = livingEntity.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
            Vector3d deltaMovement = livingEntity.getDeltaMovement();
            livingEntity.setDeltaMovement(new Vector3d(deltaMovement.x, -gravity * (amplifier + 1) * 0.5, deltaMovement.z));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
//        int j = 25 >> amplifier;
//        if (j > 0) {
//            return duration % j == 0;
//        } else {
//            return true;
//        }
        return true;
    }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        return JojoModUtil.isUndead(entity);
    }
}
