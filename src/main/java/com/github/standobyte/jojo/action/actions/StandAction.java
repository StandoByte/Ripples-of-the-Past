package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class StandAction extends Action<IStandPower> {
    @Deprecated
    protected final int xpRequirement;
    private final float resolveLevelToUnlock;
    private final float resolveCooldownMultiplier;
    private final boolean isTrained;
    private final boolean autoSummonStand;
    private final float staminaCost;
    private final float staminaCostTick;
    
    public StandAction(StandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.xpRequirement = builder.xpRequirement;
        this.resolveLevelToUnlock = builder.resolveLevelToUnlock;
        this.resolveCooldownMultiplier = builder.resolveCooldownMultiplier;
        this.isTrained = builder.isTrained;
        this.autoSummonStand = builder.autoSummonStand;
        this.staminaCost = builder.staminaCost;
        this.staminaCostTick = builder.staminaCostTick;
    }

    @Deprecated
    public int getXpRequirement() {
        return xpRequirement;
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return power.getLearningProgressPoints(this) >= 0;
    }
    
    @Override
    protected int getCooldownAdditional(IStandPower power, int ticksHeld) {
        int cooldown = super.getCooldownAdditional(power, ticksHeld);
        if (cooldown > 0 && power.getUser().hasEffect(ModEffects.RESOLVE.get())) {
            cooldown = (int) ((float) cooldown * this.resolveCooldownMultiplier);
        }
        return cooldown;
    }
    
    public boolean canBeUnlocked(IStandPower power) {
        return isUnlockedByDefault() || power.isUserCreative() || 
                resolveLevelToUnlock > -1 && power.getResolveLevel() >= resolveLevelToUnlock;
    }
    
    private boolean isUnlockedByDefault() {
        return resolveLevelToUnlock == 0;
    }
    
    public boolean isTrained() {
        return isTrained;
    }
    
    public float getStaminaCost(IStandPower stand) {
        return staminaCost;
    }
    
    // FIXME (!!!!!!!!) drain stamina each tick (either in StandEntity or stuff like TimeStopInstance)
    public float getStaminaCostTicking(IStandPower stand) {
        return staminaCostTick;
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
        private float resolveCooldownMultiplier = 0;
        private boolean isTrained = false;
        private boolean autoSummonStand = false;
        private float staminaCost = 0;
        private float staminaCostTick = 0;

        @Deprecated
        public T xpRequirement(int xpRequirement) {
            this.xpRequirement = xpRequirement;
            return getThis();
        }
        
        public T resolveLevelToUnlock(int level) {
            this.resolveLevelToUnlock = level;
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

        public T staminaCost(float staminaCost) {
            this.staminaCost = staminaCost;
            return getThis();
        }

        public T staminaCostTick(float staminaCostTick) {
            this.staminaCostTick = staminaCostTick;
            return getThis();
        }
        
        public T cooldown(int technical, int additional, float resolveCooldownMultiplier) {
            this.resolveCooldownMultiplier = MathHelper.clamp(resolveCooldownMultiplier, 0, 1);
            return super.cooldown(technical, additional);
        }
    }
}
