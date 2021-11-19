package com.github.standobyte.jojo.entity.stand.task;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.entity.stand.StandEntity;

public abstract class StandEntityTask {
    protected final int ticks;
    protected int ticksLeft;
    protected final boolean shift;
    @Nonnull protected final StandEntity standEntity;

    public StandEntityTask(int ticks, boolean shift, StandEntity standEntity) {
        this.ticks = ticks;
        this.ticksLeft = ticks;
        this.shift = shift;
        this.standEntity = standEntity;
    }
    
    public void afterInit() {}
    
    public final void onEntityTick() {
        tick();
        ticksLeft--;
        if (ticksLeft <= 0) {
            standEntity.clearTask();
        }
    }

    protected void tick() {}
    
    public int getTicks() {
        return ticks;
    }
    
    public final boolean clear() {
        boolean clear = ticksLeft == 0 || canClearMidway();
        if (clear) {
            onClear();
        }
        return clear;
    }
    
    public boolean canClearMidway() {
        return true;
    }
    
    public boolean resetPoseOnClear() {
        return true;
    }
    
    protected void onClear() {}
}
