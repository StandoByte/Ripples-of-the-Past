package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.math.MathHelper;

public abstract class StandAction extends Action<IStandPower> {
    @Deprecated
    protected final int xpRequirement;
    private final float resolveRatioToUnlock;
    private final boolean unlockedByDefault;
    
    public StandAction(StandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.xpRequirement = builder.xpRequirement;
        this.resolveRatioToUnlock = builder.resolveRatioToUnlock;
        this.unlockedByDefault = this.resolveRatioToUnlock == 0;
    }

    @Deprecated
    public int getXpRequirement() {
        return xpRequirement;
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return unlockedByDefault || power.getLearningProgress(this) >= 0;
    }
    
    
    
    public static class Builder extends StandAction.AbstractBuilder<StandAction.Builder> {

        @Override
        protected StandAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends StandAction.AbstractBuilder<T>> extends Action.AbstractBuilder<T> {
        @Deprecated
        private int xpRequirement;
        private float resolveRatioToUnlock = 0;

        @Deprecated
        public T xpRequirement(int xpRequirement) {
            this.xpRequirement = xpRequirement;
            return getThis();
        }
        
        public T resolveToUnlock(float resolveRatio) {
            this.resolveRatioToUnlock = MathHelper.clamp(resolveRatio, 0, 1);
            return getThis();
        }
    }
}
