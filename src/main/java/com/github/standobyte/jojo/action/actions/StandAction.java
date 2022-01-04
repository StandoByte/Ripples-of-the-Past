package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.stand.IStandPower;

public abstract class StandAction extends Action<IStandPower> {
    protected final int xpRequirement;
    
    public StandAction(StandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.xpRequirement = builder.xpRequirement;
    }
    
    public int getXpRequirement() {
        return xpRequirement;
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return true || power.getXp() >= getXpRequirement(); // FIXME
    }
    
    
    
    public static class Builder extends StandAction.AbstractBuilder<StandAction.Builder> {

        @Override
        protected StandAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends StandAction.AbstractBuilder<T>> extends Action.AbstractBuilder<T> {
        protected int xpRequirement;
        
        public T xpRequirement(int xpRequirement) {
            this.xpRequirement = xpRequirement;
            return getThis();
        }
    }
}
