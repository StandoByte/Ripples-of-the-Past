package com.github.standobyte.jojo.entity.stand;

import net.minecraft.util.math.MathHelper;

public class StandAttackProperties {
    private float damage;
    private float addCombo;
    private float knockback = 1.0F;
    private float knockbackYRot = 0;
    private float armorPiercing = 0;
    private float disableBlockingChance = 0;
    private float parryTiming = 0;
    
    
    
    public StandAttackProperties damage(float damage) {
        this.damage = Math.max(damage, 0);
        return this;
    }
    
    public StandAttackProperties addCombo(float combo) {
        this.addCombo = combo;
        return this;
    }
    
    public StandAttackProperties reduceKnockback(float knockback) {
        this.knockback = MathHelper.clamp(knockback, 0, 1);
        return this;
    }
    
    public StandAttackProperties addKnockback(float knockback) {
        this.knockback = 1 + knockback;
        return this;
    }
    
    public StandAttackProperties knockbackYRotDeg(float knockbackYRot) {
        this.knockbackYRot = knockbackYRot;
        return this;
    }
    
    public StandAttackProperties armorPiercing(float armorPiercing) {
        this.armorPiercing = MathHelper.clamp(armorPiercing, 0, 1);
        return this;
    }
    
    public StandAttackProperties disableBlocking(float chance) {
        this.disableBlockingChance = MathHelper.clamp(chance, 0, 1);
        return this;
    }
    
    public StandAttackProperties parryTiming(float parryTiming) {
        this.parryTiming = MathHelper.clamp(parryTiming, 0, 1);
        return this;
    }

    
    
    public float getDamage() {
        return damage;
    }
    
    public float getAddCombo() {
        return addCombo;
    }
    
    public boolean reducesKnockback() {
        return knockback < 1;
    }

    public float getKnockbackReduction() {
        return Math.min(knockback, 1);
    }
    
    public float getAdditionalKnockback() {
        return Math.max(knockback - 1, 0);
    }

    public float getknockbackYRotDeg() {
        return knockbackYRot;
    }

    public float getArmorPiercing() {
        return armorPiercing;
    }
    
    public boolean disablesBlocking() {
        return disableBlockingChance > 0;
    }
    
    public float getDisableBlockingChance() {
        return disableBlockingChance;
    }
    
    public boolean canParryHeavyAttack() {
        return parryTiming > 0;
    }
    
    public float getHeavyAttackParryTiming() {
        return parryTiming;
    }
    
}
