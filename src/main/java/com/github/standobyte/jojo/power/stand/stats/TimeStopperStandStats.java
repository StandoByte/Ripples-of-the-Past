package com.github.standobyte.jojo.power.stand.stats;

import com.github.standobyte.jojo.action.actions.TimeStop;

import net.minecraft.network.PacketBuffer;

public class TimeStopperStandStats extends StandStats {
    private final int maxTimeStopTicks;
    private final int maxTimeStopTicksVampire;

    protected TimeStopperStandStats(Builder builder) {
        super(builder);
        this.maxTimeStopTicks = builder.maxTimeStopTicks;
        this.maxTimeStopTicksVampire = builder.maxTimeStopTicksVampire;
    }
    
    protected TimeStopperStandStats(PacketBuffer buf) {
        super(buf);
        this.maxTimeStopTicks = buf.readInt();
        this.maxTimeStopTicksVampire = buf.readInt();
    }
    
    public int getMaxTimeStopTicks(boolean vampire) {
        return vampire ? maxTimeStopTicksVampire : maxTimeStopTicks;
    }
    
    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeInt(maxTimeStopTicks);
        buf.writeInt(maxTimeStopTicksVampire);
    }
    
    static {
        registerFactory(TimeStopperStandStats.class, TimeStopperStandStats::new);
    }
    
    

    public static class Builder extends AbstractBuilder<Builder> {
        private int maxTimeStopTicks = 100;
        private int maxTimeStopTicksVampire = 180;
        
        public Builder maxTimeStopTicks(int forHuman, int forVampire) {
            forHuman = Math.max(TimeStop.MIN_TIME_STOP_TICKS, forHuman);
            forVampire = Math.max(forHuman, forVampire);
            this.maxTimeStopTicks = forHuman;
            this.maxTimeStopTicksVampire = forVampire;
            return getThis();
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
