package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public abstract class StandAction extends Action<IStandPower> {
    @Deprecated
    protected final int xpRequirement;
    private final float resolveLevelToUnlock;
    private final boolean isTrained;
    private final boolean autoSummonStand;
    
    public StandAction(StandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.xpRequirement = builder.xpRequirement;
        this.resolveLevelToUnlock = builder.resolveLevelToUnlock;
        this.isTrained = builder.isTrained;
        this.autoSummonStand = builder.autoSummonStand;
    }

    @Deprecated
    public int getXpRequirement() {
        return xpRequirement;
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return true || power.getLearningProgress(this) >= 0;
    }
    
    public boolean canBeUnlocked(IStandPower power) {
        return isUnlockedByDefault() || power.getResolveLevel() >= resolveLevelToUnlock;
    }
    
    private boolean isUnlockedByDefault() {
        return resolveLevelToUnlock == 0;
    }
    
    public boolean isTrained() {
        return isTrained;
    }
    
    public float getStaminaCost(IStandPower stand) {
        return 0;
    }
    
    @Override
    public void onClick(World world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide() && !power.isActive() && autoSummonStand) {
            power.getType().summon(user, power, true);
        }
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
        private int resolveLevelToUnlock = 0;
        private boolean isTrained = false;
        private boolean autoSummonStand = false;

        @Deprecated
        public T xpRequirement(int xpRequirement) {
            this.xpRequirement = xpRequirement;
            return getThis();
        }
        
        public T resolveLevelToUnlock(int level) {
            this.resolveLevelToUnlock = Math.max(0, level);
            return getThis();
        }
        
        public T isTrained() {
            this.isTrained = true;
            return getThis();
        }
        
        public T autoSummonStand() {
            this.autoSummonStand = true;
            return getThis();
        }
    }
}
