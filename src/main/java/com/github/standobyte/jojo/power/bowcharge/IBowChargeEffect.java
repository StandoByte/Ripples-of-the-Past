package com.github.standobyte.jojo.power.bowcharge;

import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;

import net.minecraft.entity.LivingEntity;

public interface IBowChargeEffect<P extends IPower<P, T>, T extends IPowerType<P, T>> {
    BowChargeEffectInstance<P, T> createInstance(LivingEntity user, P power, T type);
    boolean canStart(P power);
}
