package com.github.standobyte.jojo.capability.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ProjectileHamonChargeCap {
    public final Entity projectile;
    public float hamonBaseDmg;
    public int maxChargeTicks;
    public boolean water;
    public float spentEnergy;
    
    public ProjectileHamonChargeCap(Entity projectile) {
        this.projectile = projectile;
    }

    public float getHamonDamage() {
        return hamonBaseDmg * MathHelper.clamp((float) (maxChargeTicks - projectile.tickCount) / (float) maxChargeTicks, 0F, 1F);
    }
}
