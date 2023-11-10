package com.github.standobyte.jojo.power.impl.stand.stats;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;

public class StandStats {
    private final UpgradeableStats statsBase;
    private final UpgradeableStats statsDevPotential;
    private final double rangeEffective;
    private final double rangeMax;
    private final double randomWeight;

    protected StandStats(AbstractBuilder<?, ?> builder) {
        this.statsBase = new UpgradeableStats(builder.powerBase, builder.speedBase, builder.durabilityBase, builder.precisionBase);
        this.statsDevPotential = new UpgradeableStats(builder.powerMax, builder.speedMax, builder.durabilityMax, builder.precisionMax);
        this.rangeEffective = builder.rangeEffective;
        this.rangeMax = builder.rangeMax;
        this.randomWeight = builder.randomWeight;
    }
    
    protected StandStats(PacketBuffer buf) {
        this.statsBase = new UpgradeableStats(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.statsDevPotential = new UpgradeableStats(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.rangeEffective = buf.readDouble();
        this.rangeMax = buf.readDouble();
        this.randomWeight = buf.readDouble();
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
        
        buf.writeDouble(randomWeight);
    }
    
    public void onNewDay(LivingEntity user, IStandPower power) {}
    
    
    
    private static final Map<Class<? extends StandStats>, Factory<? extends StandStats>> FROM_BUFFER = new HashMap<>();
    
    protected static interface Factory<T extends StandStats> {
        T read(PacketBuffer buf);
    }
    
    static {
        registerFactory(StandStats.class, StandStats::new);
    }
    
    protected static final <T extends StandStats> void registerFactory(Class<T> clazz, Factory<T> factory) {
        FROM_BUFFER.put(clazz, factory);
    }
    
    public static StandStats fromBuffer(Class<? extends StandStats> clazz, PacketBuffer buf) {
        return FROM_BUFFER.get(clazz).read(buf);
    }
    
    
    
    public double getBasePower() {
        return statsBase.power;
    }
    
    public double getBaseAttackSpeed() {
        return statsBase.speed;
    }
    
    public double getBaseMovementSpeed() {
        return StandStatFormulas.getMovementSpeed(statsBase.speed);
    }
    
    public double getBaseDurability() {
        return statsBase.durability;
    }
    
    public double getBasePrecision() {
        return statsBase.precision;
    }
    
    public double getDevPower(float devProgress) {
        return devProgress * (statsDevPotential.power - statsBase.power);
    }
    
    public double getDevAttackSpeed(float devProgress) {
        return devProgress * (statsDevPotential.speed - statsBase.speed);
    }
    
    public double getDevMovementSpeed(float devProgress) {
        return devProgress * (StandStatFormulas.getMovementSpeed(statsDevPotential.speed)
                - StandStatFormulas.getMovementSpeed(statsBase.speed));
    }
    
    public double getDevDurability(float devProgress) {
        return devProgress * (statsDevPotential.durability - statsBase.durability);
    }
    
    public double getDevPrecision(float devProgress) {
        return devProgress * (statsDevPotential.precision - statsBase.precision);
    }
    
    public double getEffectiveRange() {
        return rangeEffective;
    }
    
    public double getMaxRange() {
        return rangeMax;
    }
    
    public double getArmor() {
        return 0;
    }
    
    public double getArmorToughness() {
        return 0;
    }
    
    public double getRandomWeight() {
        return randomWeight;
    }
    
    
    
    public static class Builder extends AbstractBuilder<Builder, StandStats> {

        @Override
        protected Builder getThis() {
            return this;
        }
        
        @Override
        protected StandStats createStats() {
            return new StandStats(this);
        }
    }
    
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, S>, S extends StandStats> {
        private double powerBase = 8.0;
        private double powerMax = 8.0;
        
        private double speedBase = 8.0;
        private double speedMax = 8.0;
        
        private double rangeEffective = 2.0;
        private double rangeMax = 5.0;
        
        private double durabilityBase = 8.0;
        private double durabilityMax = 8.0;
        
        private double precisionBase = 8.0;
        private double precisionMax = 8.0;
        
        private double randomWeight = 2;
        
        public B power(double power) {
            return power(power, power);
        }
        
        public B power(double powerBase, double powerMax) {
            powerBase = Math.max(powerBase, 0);
            this.powerBase = powerBase;
            this.powerMax = Math.max(powerBase, powerMax);
            return getThis();
        }
        
        public B speed(double speed) {
            return speed(speed, speed);
        }
        
        public B speed(double speedBase, double speedMax) {
            speedBase = Math.max(speedBase, 0);
            this.speedBase = speedBase;
            this.speedMax = Math.max(speedBase, speedMax);
            return getThis();
        }
        
        public B durability(double durability) {
            return durability(durability, durability);
        }
        
        public B durability(double durabilityBase, double durabilityMax) {
            durabilityBase = Math.max(durabilityBase, 0);
            this.durabilityBase = durabilityBase;
            this.durabilityMax = Math.max(durabilityBase, durabilityMax);
            return getThis();
        }
        
        public B precision(double precision) {
            return precision(precision, precision);
        }
        
        public B precision(double precisionBase, double precisionMax) {
            precisionBase = Math.max(precisionBase, 0);
            this.precisionBase = precisionBase;
            this.precisionMax = Math.max(precisionBase, precisionMax);
            return getThis();
        }
        
        public B range(double range) {
            return range(range, range);
        }
        
        public B range(double rangeEffective, double rangeMax) {
            rangeEffective = Math.max(rangeEffective, 1.0D);
            this.rangeEffective = rangeEffective;
            this.rangeMax = Math.max(rangeEffective, rangeMax);
            return getThis();
        }
        
        @Deprecated
        public B tier(int tier) {
            return getThis();
        }
        
        public B randomWeight(double randomWeight) {
            this.randomWeight = randomWeight;
            return getThis();
        }
        
        protected abstract B getThis();
        
        @Deprecated
        public S build(String logName) {
            return build();
        }
        
        public S build() {
            return createStats();
        }
        
        protected abstract S createStats();
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
