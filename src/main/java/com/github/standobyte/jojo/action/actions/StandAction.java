package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class StandAction extends Action<IStandPower> {
    @Deprecated
    protected final int xpRequirement;
    private final float resolveRatioToUnlock;
    private final boolean unlockedByDefault;
    private final boolean autoSummonStand;
    
    public StandAction(StandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.xpRequirement = builder.xpRequirement;
        this.resolveRatioToUnlock = builder.resolveRatioToUnlock;
        this.unlockedByDefault = this.resolveRatioToUnlock == 0;
        this.autoSummonStand = builder.autoSummonStand;
    }

    @Deprecated
    public int getXpRequirement() {
        return xpRequirement;
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return true || unlockedByDefault || power.getLearningProgress(this) >= 0; // FIXME stand progression
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
        private float resolveRatioToUnlock = 0;
        private boolean autoSummonStand = false;

        @Deprecated
        public T xpRequirement(int xpRequirement) {
            this.xpRequirement = xpRequirement;
            return getThis();
        }
        
        // FIXME (resolve) actions and resolve level
        public T resolveToUnlock(float resolveRatio) {
            this.resolveRatioToUnlock = MathHelper.clamp(resolveRatio, 0, 1);
            return getThis();
        }
        
        public T autoSummonStand() {
            this.autoSummonStand = true;
            return getThis();
        }
    }
}
