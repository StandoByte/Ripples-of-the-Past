package com.github.standobyte.jojo.power;

import com.github.standobyte.jojo.action.Action;

public class HeldActionData<P extends IPower<P, ?>> {
    public final Action<P> action;
    private int ticks = 0;
    private boolean tickWentOff = false;

    public HeldActionData(Action<P> action) {
        this.action = action;
    }
    
    public int getTicks() {
        return ticks;
    }
    
    public int incTicks() {
        return ++ticks;
    }
    
    public boolean lastTickWentOff() {
        return tickWentOff;
    }
    
    public boolean refreshConditionCheckTick(boolean tickWentOff) {
        if (this.tickWentOff != tickWentOff) {
            this.tickWentOff = tickWentOff;
            return true;
        }
        return false;
    }
}
