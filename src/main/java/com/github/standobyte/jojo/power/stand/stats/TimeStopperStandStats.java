package com.github.standobyte.jojo.power.stand.stats;

import com.github.standobyte.jojo.action.actions.TimeStop;

import net.minecraft.network.PacketBuffer;

public class TimeStopperStandStats extends StandStats {
    private final int maxTimeStopTicks;
    private final int maxTimeStopTicksVampire;
    public final float maxDurationGrowthPerTick;
    // FIXME (!!!!!!!!) ts decay
    public final float maxDurationDecayPerDay;
    public final float cooldownPerTick;

    protected TimeStopperStandStats(Builder builder) {
        super(builder);
        this.maxTimeStopTicks = builder.maxTimeStopTicks;
        this.maxTimeStopTicksVampire = builder.maxTimeStopTicksVampire;
        this.maxDurationGrowthPerTick = builder.maxDurationGrowthPerTick;
        this.maxDurationDecayPerDay = builder.maxDurationDecayPerDay;
        this.cooldownPerTick = builder.cooldownPerTick;
    }
    
    protected TimeStopperStandStats(PacketBuffer buf) {
        super(buf);
        this.maxTimeStopTicks = buf.readInt();
        this.maxTimeStopTicksVampire = buf.readInt();
        this.maxDurationGrowthPerTick = buf.readFloat();
        this.maxDurationDecayPerDay = buf.readFloat();
        this.cooldownPerTick = buf.readFloat();
    }
    
    public int getMaxTimeStopTicks(boolean vampire) {
        return vampire ? maxTimeStopTicksVampire : maxTimeStopTicks;
    }
    
    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeInt(maxTimeStopTicks);
        buf.writeInt(maxTimeStopTicksVampire);
        buf.writeFloat(maxDurationGrowthPerTick);
        buf.writeFloat(maxDurationDecayPerDay);
        buf.writeFloat(cooldownPerTick);
    }
    
    static {
        registerFactory(TimeStopperStandStats.class, TimeStopperStandStats::new);
    }
    
    

    public static class Builder extends AbstractBuilder<Builder> {
        private int maxTimeStopTicks = 100;
        private int maxTimeStopTicksVampire = 180;
        private float maxDurationGrowthPerTick = 0.1F;
        private float maxDurationDecayPerDay = 0;
        private float cooldownPerTick = 3;
        
        public Builder maxTimeStopTicks(int forHuman, int forVampire) {
            forHuman = Math.max(TimeStop.MIN_TIME_STOP_TICKS, forHuman);
            forVampire = Math.max(forHuman, forVampire);
            this.maxTimeStopTicks = forHuman;
            this.maxTimeStopTicksVampire = forVampire;
            return getThis();
        }
        
        public Builder maxDurationGrowthPerTick(float points) {
            this.maxDurationGrowthPerTick = points;
            return this;
        }
        
        public Builder maxDurationDecayPerDay(float points) {
            this.maxDurationDecayPerDay = points;
            return this;
        }
        
        public Builder cooldownPerTick(float ticks) {
            this.cooldownPerTick = ticks;
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
        
        public TimeStopperStandStats build() {
            return new TimeStopperStandStats(this);
        }
    }
}
