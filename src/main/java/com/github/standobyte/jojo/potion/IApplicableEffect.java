package com.github.standobyte.jojo.potion;

import net.minecraft.entity.LivingEntity;

public interface IApplicableEffect {
    boolean isApplicable(LivingEntity entity);
}
