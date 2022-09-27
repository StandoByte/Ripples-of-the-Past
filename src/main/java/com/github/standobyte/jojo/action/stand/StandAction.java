package com.github.standobyte.jojo.action.stand;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public abstract class StandAction extends Action<IStandPower> {
    private final float resolveLevelToUnlock;
    private final float resolveCooldownMultiplier;
    private final boolean isTrained;
    private final boolean autoSummonStand;
    private final float staminaCost;
    private final float staminaCostTick;
    private final Set<StandPart> partsRequired;
    
    public StandAction(StandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.resolveLevelToUnlock = builder.resolveLevelToUnlock;
        this.resolveCooldownMultiplier = builder.resolveCooldownMultiplier;
        this.isTrained = builder.isTrained;
        this.autoSummonStand = builder.autoSummonStand;
        this.staminaCost = builder.staminaCost;
        this.staminaCostTick = builder.staminaCostTick;
        this.partsRequired = builder.partsRequired;
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return resolveLevelToUnlock == 0 || power.getLearningProgressPoints(this) >= 0;
    }
    
    @Override
    protected int getCooldownAdditional(IStandPower power, int ticksHeld) {
        int cooldown = super.getCooldownAdditional(power, ticksHeld);
        if (cooldown > 0 && power.getUser().hasEffect(ModEffects.RESOLVE.get())) {
            cooldown = (int) ((float) cooldown * this.resolveCooldownMultiplier);
        }
        return cooldown;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        for (StandPart part : partsRequired) {
            if (power.hasPower() && !power.getStandInstance().get().hasPart(part)) {
                ITextComponent message = new TranslationTextComponent("jojo.message.action_condition.no_stand_part." + part.name().toLowerCase());
                return ActionConditionResult.createNegative(message);
            }
        }
        return super.checkConditions(user, power, target);
    }
    
    protected boolean isPartRequired(StandPart standPart) {
        return partsRequired.contains(standPart);
    }
    
    public boolean canBeUnlocked(IStandPower power) {
        return power.isUserCreative() || 
                resolveLevelToUnlock > -1 && power.getResolveLevel() >= resolveLevelToUnlock || isUnlockedByDefault();
    }
    
    protected boolean isUnlockedByDefault() {
        return resolveLevelToUnlock == 0;
    }
    
    public boolean isTrained() {
        return isTrained;
    }
    
    public float getStaminaCost(IStandPower stand) {
        return staminaCost;
    }
    
    public float getStaminaCostTicking(IStandPower stand) {
        return staminaCostTick;
    }
    
    @Override
    public void onPerform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide() && !staminaConsumedDifferently(power)) {
            power.consumeStamina(getStaminaCost(power));
        }
        super.onPerform(world, user, power, target);
    }
    
    @Override
    public void onClick(World world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide() && !power.isActive() && autoSummonStand(power)) {
            power.getType().summon(user, power, true);
        }
    }
    
    protected boolean autoSummonStand(IStandPower power) {
        return autoSummonStand;
    }
    
    public boolean staminaConsumedDifferently(IStandPower power) {
        return false;
    }
    
    
    
    public static class Builder extends StandAction.AbstractBuilder<StandAction.Builder> {

        @Override
        protected StandAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends StandAction.AbstractBuilder<T>> extends Action.AbstractBuilder<T> {
        private int resolveLevelToUnlock = 0;
        private float resolveCooldownMultiplier = 0;
        private boolean isTrained = false;
        private boolean autoSummonStand = false;
        private float staminaCost = 0;
        private float staminaCostTick = 0;
        private final Set<StandPart> partsRequired = EnumSet.noneOf(StandPart.class);

        public T noResolveUnlock() {
            return resolveLevelToUnlock(-1);
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
        
        public T partsRequired(StandPart... parts) {
            Collections.addAll(partsRequired, parts);
            return getThis();
        }
    }
}
