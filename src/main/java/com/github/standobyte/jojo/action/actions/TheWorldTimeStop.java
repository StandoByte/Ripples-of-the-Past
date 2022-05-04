package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.power.stand.IStandPower;

public class TheWorldTimeStop extends TimeStop {

    public TheWorldTimeStop(Builder builder) {
        super(builder);
    }

    @Override
    protected boolean autoSummonStand(IStandPower power) {
        return super.autoSummonStand(power) || power.getResolveLevel() < 3;
    }
    
    @Override
    public int getHoldDurationToFire(IStandPower power) { 
        return shortedHoldDuration(power, super.getHoldDurationToFire(power));
    }
    
    // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!! delete
    @Override
    public int getHoldDurationMax(IStandPower power) {
        return super.getHoldDurationMax(power);
    }
    
    private int shortedHoldDuration(IStandPower power, int ticks) {
        return power.getResolveLevel() >= 4 ? ticks / 2 : ticks;
    }
    
    @Override
    public boolean cancelHeldOnGettingAttacked(IStandPower power) {
        return true;
    }
}
