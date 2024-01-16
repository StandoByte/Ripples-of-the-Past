package com.github.standobyte.jojo.power.impl.stand.stats;

import com.github.standobyte.jojo.action.stand.TimeStop;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;

public class TimeStopperStandStats extends StandStats {
    private final int timeStopMaxTicks;
    private final int timeStopMaxTicksVampire;
    public final float timeStopLearningPerTick;
    public final float timeStopDecayPerDay;
    public final float timeStopCooldownPerTick;

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
    
    @Override
    public void onNewDay(LivingEntity user, IStandPower power) {
        if (!user.level.isClientSide()) {
            LivingUtilCap cap = user.getCapability(LivingUtilCapProvider.CAPABILITY).resolve().get();
            if (!cap.hasUsedTimeStopToday && timeStopDecayPerDay > 0) {
                power.getAllUnlockedActions().forEach(ability -> {
                    if (ability instanceof TimeStop) {
                        power.addLearningProgressPoints((TimeStop) ability, -timeStopDecayPerDay);
                    }
                });
            }
            cap.hasUsedTimeStopToday = false;
        }
    }
    
    static {
        registerFactory(TimeStopperStandStats.class, TimeStopperStandStats::new);
    }
    
    

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
