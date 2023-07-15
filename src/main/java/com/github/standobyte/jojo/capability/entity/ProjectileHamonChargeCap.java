package com.github.standobyte.jojo.capability.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ProjectileHamonChargeCap {
    public final Entity projectile;
    public float hamonBaseDmg;
    public int maxChargeTicks;
    public float spentEnergy;
    
    public ProjectileHamonChargeCap(Entity projectile) {
        this.projectile = projectile;
    }
    
    public ProjectileHamonChargeCap withBaseDmg(float damage) {
        this.hamonBaseDmg = damage;
        return this;
    }
    
    public ProjectileHamonChargeCap withMaxChargeTicks(int ticks) {
        this.maxChargeTicks = ticks;
        return this;
    }
    
    public ProjectileHamonChargeCap withInfiniteChargeTime() {
        this.maxChargeTicks = -1;
        return this;
    }
    
    public ProjectileHamonChargeCap withSpentEnergy(float spentEnergy) {
        this.spentEnergy = spentEnergy;
        return this;
    }
    
    
    
    public float getHamonDamage() {
        float chargeRatio = maxChargeTicks >= 0 ? MathHelper.clamp((float) (maxChargeTicks - projectile.tickCount) / (float) maxChargeTicks, 0F, 1F) : 1F;
        return hamonBaseDmg * chargeRatio;
    }
}
