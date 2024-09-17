package com.github.standobyte.jojo.power.impl.stand.stats;

import com.github.standobyte.jojo.action.stand.TimeStop;

import net.minecraft.network.PacketBuffer;

/**
 * @deprecated Config parameters of the time stop are now inside TimeStop action objects instead.
 */
@Deprecated
public class TimeStopperStandStats extends StandStats {
    transient private final int timeStopMaxTicks;
    transient private final int timeStopMaxTicksVampire;
    transient public final float timeStopLearningPerTick;
    transient public final float timeStopDecayPerDay;
    transient public final float timeStopCooldownPerTick;

    protected TimeStopperStandStats(Builder builder) {
        super(builder);
        this.timeStopMaxTicks = builder.timeStopMaxTicks;
        this.timeStopMaxTicksVampire = builder.timeStopMaxTicksVampire;
        this.timeStopLearningPerTick = builder.timeStopLearningPerTick;
        this.timeStopDecayPerDay = builder.timeStopDecayPerDay;
        this.timeStopCooldownPerTick = builder.timeStopCooldownPerTick;
    }
    
    protected TimeStopperStandStats(PacketBuffer buf) {
        super(buf);
        this.timeStopMaxTicks = buf.readInt();
        this.timeStopMaxTicksVampire = buf.readInt();
        this.timeStopLearningPerTick = buf.readFloat();
        this.timeStopDecayPerDay = buf.readFloat();
        this.timeStopCooldownPerTick = buf.readFloat();
    }
    
    public int getMaxTimeStopTicks(boolean vampire) {
        return vampire ? timeStopMaxTicksVampire : timeStopMaxTicks;
    }
    
    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeInt(timeStopMaxTicks);
        buf.writeInt(timeStopMaxTicksVampire);
        buf.writeFloat(timeStopLearningPerTick);
        buf.writeFloat(timeStopDecayPerDay);
        buf.writeFloat(timeStopCooldownPerTick);
    }
    
    static {
        registerFactory(TimeStopperStandStats.class, TimeStopperStandStats::new);
    }
    
    

    @Deprecated
    public static class Builder extends AbstractBuilder<Builder, TimeStopperStandStats> {
        private int timeStopMaxTicks = 100;
        private int timeStopMaxTicksVampire = 180;
        private float timeStopLearningPerTick = 0.1F;
        private float timeStopDecayPerDay = 0;
        private float timeStopCooldownPerTick = 3;
        
        public Builder timeStopMaxTicks(int forHuman, int forVampire) {
            forHuman = Math.max(TimeStop.MIN_TIME_STOP_TICKS, forHuman);
            forVampire = Math.max(forHuman, forVampire);
            this.timeStopMaxTicks = forHuman;
            this.timeStopMaxTicksVampire = forVampire;
            return getThis();
        }
        
        public Builder timeStopLearningPerTick(float points) {
            this.timeStopLearningPerTick = points;
            return getThis();
        }
        
        public Builder timeStopDecayPerDay(float points) {
            this.timeStopDecayPerDay = points;
            return getThis();
        }
        
        public Builder timeStopCooldownPerTick(float ticks) {
            this.timeStopCooldownPerTick = ticks;
            return getThis();
        }

        @Override
        protected Builder getThis() {
            return this;
        }
        
        @Override
        protected TimeStopperStandStats createStats() {
            return new TimeStopperStandStats(this);
        }
    }
}
