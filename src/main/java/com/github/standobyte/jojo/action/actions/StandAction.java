package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;

public abstract class StandAction extends Action {
    protected final int expRequirement;
    
    public StandAction(StandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.expRequirement = builder.expRequirement;
    }
    
    public int getExpRequirement() {
        return expRequirement;
    }
    
    
    
    public static class Builder extends StandAction.AbstractBuilder<StandAction.Builder> {

        @Override
        protected StandAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends StandAction.AbstractBuilder<T>> extends Action.AbstractBuilder<T> {
        protected int expRequirement;
        
        public T expRequirement(int expRequirement) {
            this.expRequirement = expRequirement;
            return getThis();
        }
    }
}
