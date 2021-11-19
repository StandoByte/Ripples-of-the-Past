package com.github.standobyte.jojo.action;

public class HeldActionData {
    public final Action action;
    private int ticks = 0;
    private ActionTarget target = ActionTarget.EMPTY;
    private boolean tickWentOff = false;

    public HeldActionData(Action action) {
        this.action = action;
    }
    
    public int getTicks() {
        return ticks;
    }
    
    public int incTicks() {
        return ++ticks;
    }
    
    public void setActionTarget(ActionTarget target) {
        this.target = target;
    }
    
    public ActionTarget getActionTarget() {
        return target;
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
