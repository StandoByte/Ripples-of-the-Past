package com.github.standobyte.jojo.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public interface IPassengerMixinReposition {

    @Nullable Vector3d repositionPassenger(@Nonnull Entity vehicle);
}
