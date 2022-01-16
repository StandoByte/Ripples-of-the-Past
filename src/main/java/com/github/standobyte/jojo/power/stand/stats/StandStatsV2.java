package com.github.standobyte.jojo.power.stand.stats;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;

public class StandStatsV2 {
    private final UpgradeableStats statsBase;
    private final UpgradeableStats statsDevPotential;
    private final double rangeEffective;
    private final double rangeMax;

    protected StandStatsV2(AbstractBuilder<?> builder) {
        this.statsBase = new UpgradeableStats(builder.powerBase, builder.speedBase, builder.durabilityBase, builder.precisionBase);
        this.statsDevPotential = new UpgradeableStats(builder.powerMax, builder.speedMax, builder.durabilityMax, builder.precisionMax);
        this.rangeEffective = builder.rangeEffective;
        this.rangeMax = builder.rangeMax;
    }
    
    protected StandStatsV2(PacketBuffer buf) {
        this.statsBase = new UpgradeableStats(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.statsDevPotential = new UpgradeableStats(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.rangeEffective = buf.readDouble();
        this.rangeMax = buf.readDouble();
    }
    
    public void write(PacketBuffer buf) {
        buf.writeDouble(statsBase.power);
        buf.writeDouble(statsBase.speed);
        buf.writeDouble(statsBase.durability);
        buf.writeDouble(statsBase.precision);
        
        buf.writeDouble(statsDevPotential.power);
        buf.writeDouble(statsDevPotential.speed);
        buf.writeDouble(statsDevPotential.durability);
        buf.writeDouble(statsDevPotential.precision);
        
        buf.writeDouble(rangeEffective);
        buf.writeDouble(rangeMax);
    }
    
    
    
    private static final Map<Class<? extends StandStatsV2>, Factory<? extends StandStatsV2>> FROM_BUFFER = new HashMap<>();
    
    protected static interface Factory<T extends StandStatsV2> {
        T read(PacketBuffer buf);
    }
    
    static {
        registerFactory(StandStatsV2.class, StandStatsV2::new);
    }
    
    protected static final <T extends StandStatsV2> void registerFactory(Class<T> clazz, Factory<T> factory) {
        FROM_BUFFER.put(clazz, factory);
    }
    
    public static StandStatsV2 fromBuffer(Class<? extends StandStatsV2> clazz, PacketBuffer buf) {
        return FROM_BUFFER.get(clazz).read(buf);
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
        return MathHelper.lerp(stand.getAchievedResolveRatio(), statsBase.power, statsDevPotential.power);
    }
    
    private double getSpeed(IStandPower stand) {
        return MathHelper.lerp(stand.getAchievedResolveRatio(), statsBase.speed, statsDevPotential.speed);
    }
    
    private double getDurability(IStandPower stand) {
        return MathHelper.lerp(stand.getAchievedResolveRatio(), statsBase.durability, statsDevPotential.durability);
    }
    
    private double getPrecision(IStandPower stand) {
        return MathHelper.lerp(stand.getAchievedResolveRatio(), statsBase.precision, statsDevPotential.precision);
    }
    
    
    
    public static class Builder extends AbstractBuilder<Builder> {

        @Override
        protected Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends AbstractBuilder<T>> { // FIXME (stats) default C-level values
        private double powerBase = 0;
        private double powerMax = 0;
        private double speedBase = 0;
        private double speedMax = 0;
        private double rangeEffective = 2;
        private double rangeMax = 5;
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
