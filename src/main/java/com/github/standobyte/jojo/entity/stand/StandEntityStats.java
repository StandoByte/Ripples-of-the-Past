package com.github.standobyte.jojo.entity.stand;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.math.MathHelper;

public class StandEntityStats {
    private final double damage;
    private final double attackKnockback;
    private final double movementSpeed;
    private final double attackSpeed;
    private final int summonTicks;
    private final double maxRange;
    private final double armor;
    private final double armorToughness;
    private final double knockbackResistance;
    private final float blockDmgFactor;
    private final float blockStaminaCostForDmgPoint;
    private final double precision;

    private StandEntityStats(Builder builder) {
        this.damage = builder.damage;
        this.attackKnockback = builder.attackKnockback;

        this.movementSpeed = builder.movementSpeed;
        this.attackSpeed = builder.attackSpeed;
        this.summonTicks = builder.summonTicks;

        this.maxRange = builder.maxRange;

        this.armor = builder.armor;
        this.armorToughness = builder.armorToughness;
        this.knockbackResistance = builder.knockbackResistance;
        this.blockDmgFactor = builder.blockDmgFactor;
        this.blockStaminaCostForDmgPoint = builder.blockStaminaCostForDmgPoint;

        this.precision = builder.precision;
    }

    public static class Builder {
        private double damage = 5.0;
        private double attackKnockback = 1;
        private double movementSpeed = Attributes.MOVEMENT_SPEED.getDefaultValue() - 0.1;
        private double attackSpeed = 10.0;
        private int summonTicks = 10;
        private double maxRange = 4.0;
        private double armor = Attributes.ARMOR.getDefaultValue();
        private double armorToughness = Attributes.ARMOR_TOUGHNESS.getDefaultValue();
        private double knockbackResistance = Attributes.KNOCKBACK_RESISTANCE.getDefaultValue();
        private float blockDmgFactor = 0.5F;
        private float blockStaminaCostForDmgPoint = 10;
        private double precision = 0.0;

        public StandEntityStats.Builder damage(double damage) {
            this.damage = Attributes.ATTACK_DAMAGE.sanitizeValue(damage);
            return this;
        }

        public StandEntityStats.Builder attackKnockback(double attackKnockback) {
            this.attackKnockback = Attributes.ATTACK_KNOCKBACK.sanitizeValue(attackKnockback);
            return this;
        }

        public StandEntityStats.Builder movementSpeed(double movementSpeed) {
            this.movementSpeed = Attributes.MOVEMENT_SPEED.sanitizeValue(movementSpeed);
            return this;
        }

        public StandEntityStats.Builder attackSpeed(double attackSpeed) {
            this.attackSpeed = Attributes.ATTACK_SPEED.sanitizeValue(attackSpeed);
            return this;
        }

        public StandEntityStats.Builder summonTicks(int summonTicks) {
            this.summonTicks = MathHelper.clamp(summonTicks, 0, 20);
            return this;
        }

        public StandEntityStats.Builder maxRange(double effectiveRange) {
            this.maxRange = MathHelper.clamp(effectiveRange, 4, 1000);
            return this;
        }

        public StandEntityStats.Builder armor(double armor) {
            this.armor = Attributes.ARMOR.sanitizeValue(armor);
            return this;
        }

        public StandEntityStats.Builder armorToughness(double armorToughness) {
            this.armorToughness = Attributes.ARMOR_TOUGHNESS.sanitizeValue(armorToughness);
            return this;
        }

        public StandEntityStats.Builder knockbackResistance(double knockbackResistance) {
            this.knockbackResistance = Attributes.KNOCKBACK_RESISTANCE.sanitizeValue(knockbackResistance);
            return this;
        }

        public StandEntityStats.Builder precision(double precision) {
            this.precision = MathHelper.clamp(precision, 0, 1);
            return this;
        }

        public StandEntityStats.Builder blockDmgFactor(float blockDmgFactor) {
            this.blockDmgFactor = MathHelper.clamp(blockDmgFactor, 0, 1);
            return this;
        }

        public StandEntityStats.Builder blockStaminaCostForDmgPoint(float blockStaminaCostForDmgPoint) {
            this.blockStaminaCostForDmgPoint = MathHelper.clamp(blockStaminaCostForDmgPoint, 0, 100);
            return this;
        }

        public StandEntityStats build() {
            return new StandEntityStats(this);
        }
    }

    public double getDamage() {
        return damage;
    }

    public double getAttackKnockback() {
        return attackKnockback;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public int getSummonTicks() {
        return summonTicks;
    }

    public double getMaxRange() {
        return maxRange;
    }

    public double getArmor() {
        return armor;
    }

    public double getArmorToughness() {
        return armorToughness;
    }

    public double getKnockbackResistance() {
        return knockbackResistance;
    }

    public float getBlockDmgFactor() {
        return blockDmgFactor;
    }

    public float getBlockStaminaCostForDmgPoint() {
        return blockStaminaCostForDmgPoint;
    }

    public double getPrecision() {
        return precision;
    }
}
