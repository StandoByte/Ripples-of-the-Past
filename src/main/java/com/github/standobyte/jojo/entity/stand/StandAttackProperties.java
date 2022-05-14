package com.github.standobyte.jojo.entity.stand;

import java.util.Optional;

import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class StandAttackProperties {
    private float damage;
    private float addCombo;
    private float knockback = 1.0F;
    private float knockbackYRot = 0;
    private float knockbackXRot = 0;
    private float armorPiercing = 0;
    private float disableBlockingChance = 0;
    private float parryTiming = 0;
    private Vector3d sweepingAabb;
    private float sweepingDamage;
    private int standInvulTime = 0;
    private Optional<BeforeAttackBehavior> beforeAttack = Optional.empty();
    private Optional<AfterAttackBehavior> afterAttack = Optional.empty();
    
    
    
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
    
    public StandAttackProperties knockbackXRot(float knockbackXRot) {
        this.knockbackXRot = MathHelper.clamp(knockbackXRot, -90F, 90F);
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
    
    public StandAttackProperties sweepingAttack(double x, double y, double z, float damage) {
        x = Math.max(x, 0);
        y = Math.max(y, 0);
        z = Math.max(z, 0);
        if ((x > 0 || y > 0 || z > 0) && damage > 0) {
            this.sweepingAabb = new Vector3d(x, y, z);
            this.sweepingDamage = damage;
        }
        return this;
    }
    
    public StandAttackProperties setStandInvulTime(int ticks) {
        this.standInvulTime = ticks;
        return this;
    }
    
    public StandAttackProperties callbackBeforeAttack(BeforeAttackBehavior callback) {
        this.beforeAttack = Optional.ofNullable(callback);
        return this;
    }
    
    public StandAttackProperties callbackAfterAttack(AfterAttackBehavior callback) {
        this.afterAttack = Optional.ofNullable(callback);
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

    public float getKnockbackYRotDeg() {
        return knockbackYRot;
    }
    
    public float getKnockbackXRot() {
        return knockbackXRot;
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
    
    public boolean isSweepingAttack() {
        return sweepingAabb != null && sweepingDamage > 0;
    }
    
    public AxisAlignedBB sweepingAttackAabb(AxisAlignedBB targetAabb) {
        return targetAabb.inflate(sweepingAabb.x, sweepingAabb.y, sweepingAabb.z);
    }
    
    public float getSweepingDamage() {
        return sweepingDamage;
    }
    
    public int getStandInvulTime() {
        return standInvulTime;
    }

    public void beforeAttack(Entity target, StandEntity stand, IStandPower power, LivingEntity user) {
        beforeAttack.ifPresent(behavior -> behavior.beforeAttack(target, stand, power, user));
    }
    
    public void afterAttack(Entity target, StandEntity stand, IStandPower power, LivingEntity user, boolean hurt, boolean killed) {
        afterAttack.ifPresent(behavior -> behavior.afterAttack(target, stand, power, user, hurt, killed));
    }
    
    
    
    public interface BeforeAttackBehavior {
        void beforeAttack(Entity target, StandEntity stand, IStandPower power, LivingEntity user);
    }
    
    public interface AfterAttackBehavior {
        void afterAttack(Entity target, StandEntity stand, IStandPower power, LivingEntity user, boolean hurt, boolean killed);
    }
}
