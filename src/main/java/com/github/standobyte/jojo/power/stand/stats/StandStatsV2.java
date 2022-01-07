package com.github.standobyte.jojo.power.stand.stats;

import com.github.standobyte.jojo.power.stand.IStandPower;

public class StandStatsV2 {
    private final UpgradeableStats statsBase;
    private final UpgradeableStats statsMax;
    private final double rangeEffective;
    private final double rangeMax;

    protected StandStatsV2(AbstractBuilder<?> builder) {
        this.statsBase = new UpgradeableStats(builder.powerBase, builder.speedBase, builder.durabilityBase, builder.precisionBase);
        this.statsMax = new UpgradeableStats(builder.powerMax, builder.speedMax, builder.durabilityMax, builder.precisionMax);
        this.rangeEffective = builder.rangeEffective;
        this.rangeMax = builder.rangeMax;
    }
    
    // quickAttackDamage
    // strongAttackDamage
    // barrageHitDamage
    // strongAttackArmorPenetration
    // leapStrength
    
    // quickAttackSpeed
    // strongAttackSpeed
    // barrageSpeed
    // summonSpeed
    // unsummonedAttackDeflectSpeed
    
    // effectiveRange
    // maxRange
    // rangeStrengthFactor
    
    // resistance
    // staminaCostModifier
    // blockDamageReduction
    
    // hitboxExpansion
    // barrageHitMultiplier
    // projectileAccuracy
    
    // parry?
    
    private double getPower(IStandPower stand) {
        return statsBase.power + (statsMax.power - statsBase.power) * stand.getAchievedResolveRatio();
    }
    
    private double getSpeed(IStandPower stand) {
        return statsBase.speed + (statsMax.speed - statsBase.speed) * stand.getAchievedResolveRatio();
    }
    
    private double getDurability(IStandPower stand) {
        return statsBase.durability + (statsMax.durability - statsBase.durability) * stand.getAchievedResolveRatio();
    }
    
    private double getPrecision(IStandPower stand) {
        return statsBase.precision + (statsMax.precision - statsBase.precision) * stand.getAchievedResolveRatio();
    }

    public static class Builder extends AbstractBuilder<Builder> {

        @Override
        protected Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends AbstractBuilder<T>> { // FIXME default C-level values
        private double powerBase = 0;
        private double powerMax = 0;
        private double speedBase = 0;
        private double speedMax = 0;
        private double rangeEffective = 1;
        private double rangeMax = 2;
        private double durabilityBase = 0;
        private double durabilityMax = 0;
        private double precisionBase = 0;
        private double precisionMax = 0;
        
        public T power(double power) {
            return power(power, power);
        }
        
        public T power(double powerBase, double powerMax) {
            powerBase = Math.max(powerBase, 0);
            this.powerBase = powerBase;
            this.powerMax = Math.max(powerBase, powerMax);
            return getThis();
        }
        
        public T speed(double speed) {
            return speed(speed, speed);
        }
        
        public T speed(double speedBase, double speedMax) {
            speedBase = Math.max(speedBase, 0);
            this.speedBase = speedBase;
            this.speedMax = Math.max(speedBase, speedMax);
            return getThis();
        }
        
        public T durability(double durability) {
            return durability(durability, durability);
        }
        
        public T durability(double durabilityBase, double durabilityMax) {
            durabilityBase = Math.max(durabilityBase, 0);
            this.durabilityBase = durabilityBase;
            this.durabilityMax = Math.max(durabilityBase, durabilityMax);
            return getThis();
        }
        
        public T precision(double precision) {
            return precision(precision, precision);
        }
        
        public T precision(double precisionBase, double precisionMax) {
            precisionBase = Math.max(precisionBase, 0);
            this.precisionBase = precisionBase;
            this.precisionMax = Math.max(precisionBase, precisionMax);
            return getThis();
        }
        
        public T range(double range) {
            return range(range, range * 2);
        }
        
        public T range(double rangeEffective, double rangeMax) {
            rangeEffective = Math.max(rangeEffective, 1.0D);
            this.rangeEffective = rangeEffective;
            this.rangeMax = Math.max(rangeEffective, rangeMax);
            return getThis();
        }
        
        protected abstract T getThis();
        
        public StandStatsV2 build() {
            return new StandStatsV2(this);
        }
    }
    
    private static class UpgradeableStats {
        private final double power;
        private final double speed;
        private final double durability;
        private final double precision;
        
        private UpgradeableStats(double power, double speed, double durability, double precision) {
            this.power = power;
            this.speed = speed;
            this.durability = durability;
            this.precision = precision;
        }
    }
}
