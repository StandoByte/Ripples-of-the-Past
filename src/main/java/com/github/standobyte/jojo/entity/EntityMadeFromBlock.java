package com.github.standobyte.jojo.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface EntityMadeFromBlock {
    boolean crazyDRestore(BlockPos blockPos);
    default boolean isEntityAlive() {
        return ((Entity) this).isAlive();
    }
}
