package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.general.ObjectWrapper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public abstract class StandAction extends Action<IStandPower> {
    protected final int resolveLevelToUnlock;
    private final float resolveCooldownMultiplier;
    private final boolean isTrained;
    private final boolean autoSummonStand;
    protected StandPose standPose;
    private final float staminaCost;
    private final float staminaCostTick;
    private final Set<StandPart> partsRequired;
    private final List<Supplier<? extends StandAction>> extraUnlockables;
    
    public StandAction(StandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.resolveLevelToUnlock = builder.resolveLevelToUnlock;
        this.resolveCooldownMultiplier = builder.resolveCooldownMultiplier;
        this.isTrained = builder.isTrained;
        this.autoSummonStand = builder.autoSummonStand;
        this.standPose = builder.standPose;
        this.staminaCost = builder.staminaCost;
        this.staminaCostTick = builder.staminaCostTick;
        this.partsRequired = builder.partsRequired;
        this.extraUnlockables = builder.extraUnlockables;
        this._recoveryFollowUpPreInit = builder.recoveryFollowUp;
    }
    
    
    private Map<Supplier<? extends StandAction>, List<Supplier<? extends StandAction>>> _recoveryFollowUpPreInit;
    private Map<? extends StandAction, List<? extends StandAction>> recoveryFollowUp;
    
    protected void initRecoveryFollowUp() {
        if (_recoveryFollowUpPreInit != null) {
            recoveryFollowUp = _recoveryFollowUpPreInit.entrySet().stream().collect(Collectors.toMap(
                    entry -> entry.getKey() != null ? entry.getKey().get() : this, 
                    entry -> entry.getValue().stream().map(Supplier::get).collect(Collectors.toList())));
        }
    }
    
    @Override
    protected Action<IStandPower> replaceActionKostyl(IStandPower power, ActionTarget target) {
        if (power.getStandManifestation() instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) power.getStandManifestation();
            Optional<StandEntityTask> curTask = standEntity.getCurrentTask();
            
            StandAction oldFollowUp = getRecoveryFollowup(power, standEntity);
            StandAction attackFollowUp = oldFollowUp;
            
            if (oldFollowUp != null && !curTask.filter(task -> {
                return task.getAction() == this && canSetFollowUp(task, oldFollowUp, power);
            }).isPresent()) {
                attackFollowUp = null;
            }
            
            if (attackFollowUp == null && recoveryFollowUp != null) {
                attackFollowUp = curTask.map(task -> {
                    List<? extends StandAction> availableFollowUps = recoveryFollowUp.get(task.getAction());
                    if (availableFollowUps != null) {
                        return availableFollowUps.stream()
                                .filter(action -> canSetFollowUp(task, action, power))
                                .findFirst()
                                .orElse(null);
                    }
                    return null;
                }).orElse(null);
            }
            
            if (attackFollowUp != null) {
                return attackFollowUp;
            }
        }
        return super.replaceActionKostyl(power, target);
    }
    
    protected static boolean canSetFollowUp(StandEntityTask task, StandAction followUp, IStandPower power) {
        return !task.hasModifierAction(followUp) && power.checkRequirements(followUp, new ObjectWrapper<>(task.getTarget()), true).isPositive();
    }
    
    @Deprecated
    @Nullable
    protected StandEntityActionModifier getRecoveryFollowup(IStandPower standPower, StandEntity standEntity) {
        return null;
    }
    
    
    @Override
    public PowerClassification getPowerClassification() {
        return PowerClassification.STAND;
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return power.getLearningProgressPoints(this) >= 0;
    }
    
    @Override
    protected boolean canBeUsedDuringPlayerAction(ContinuousActionInstance<?, ?> curPlayerAction) {
        return true;
    }
    
    @Override
    public boolean isTrained() {
        return isTrained;
    }
    
    public float resolveLearningMultiplier(IStandPower power) {
        return 4;
    }
    
    public Collection<StandAction> getExtraUnlockables() {
        List<StandAction> actions = extraUnlockables.stream()
                .map(Supplier::get)
                .collect(Collectors.toCollection(ArrayList::new));
        for (StandAction action : getExtraUnlockable()) {
            actions.add(action);
        }
        return actions;
    }
    
    private static final StandAction[] NO_EXTRA_ACTIONS = new StandAction[0];
    /**
     * @deprecated Use {@link StandAction.AbstractBuilder#addExtraUnlockable(Supplier)} when initializing the action
     */
    @Deprecated
    public StandAction[] getExtraUnlockable() {
        return NO_EXTRA_ACTIONS;
    }
    
    public float getMaxTrainingPoints(IStandPower power) {
        return 1F;
    }
    
    public void onTrainingPoints(IStandPower power, float points) {}
    
    public void onMaxTraining(IStandPower power) {}
    
    @Override
    protected int getCooldownAdditional(IStandPower power, int ticksHeld) {
        int cooldown = super.getCooldownAdditional(power, ticksHeld);
        if (cooldown > 0 && power.getUser().hasEffect(ModStatusEffects.RESOLVE.get())) {
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
        return !isUnlocked(power) && (
                power.isUserCreative() || 
                resolveLevelToUnlock > -1 && power.getResolveLevel() >= resolveLevelToUnlock || 
                isUnlockedByDefault());
    }
    
    public boolean isUnlockedByDefault() {
        return resolveLevelToUnlock == 0;
    }
    
    public float getStaminaCost(IStandPower stand) {
        return staminaCost;
    }
    
    public float getStaminaCostTicking(IStandPower stand) {
        return staminaCostTick;
    }
    
    @Override
    public float getCostToRender(IStandPower power, ActionTarget target) {
        int ticksHeld = power.getHeldAction() == this ? power.getHeldActionTicks() : 0;
        if (getHoldDurationMax(power) > 0) {
            return getStaminaCost(power) + getStaminaCostTicking(power) * Math.max((getHoldDurationToFire(power) - ticksHeld), 1);
        }
        return getStaminaCost(power);
    }
    
    @Override
    public void onPerform(World world, LivingEntity user, IStandPower power, ActionTarget target, @Nullable PacketBuffer extraInput) {
        consumeStamina(world, power);
        super.onPerform(world, user, power, target, extraInput);
    }
    
    protected void consumeStamina(World world, IStandPower power) {
        if (!world.isClientSide()) {
            power.consumeStamina(getStaminaCost(power));
        }
    }
    
    
    @Override
    public void onClick(World world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide() && !power.isActive() && autoSummonStand(power)) {
            power.getType().summon(user, power, true);
        }
    }
    
    public StandPose getStandPose(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityTask task) {
        return standPose;
    }
    
    protected boolean autoSummonStand(IStandPower power) {
        return autoSummonStand;
    }
    
    @Deprecated
    public boolean staminaConsumedDifferently(IStandPower power) {
        return false;
    }
    
    public void passivelyOnNewDay(LivingEntity user, IStandPower power, long prevDay, long day) {}
    
    @Override
    public IFormattableTextComponent getNameLocked(IStandPower power) {
        if (resolveLevelToUnlock > power.getResolveLevel()) {
            return new TranslationTextComponent("jojo.layout_edit.locked.stand", 
                    new TranslationTextComponent("jojo.layout_edit.locked.stand.resolve").withStyle(ClientUtil.textColor(ModStatusEffects.RESOLVE.get().getColor())), 
                    (int) resolveLevelToUnlock);
        }
        return super.getNameLocked(power);
    }
    
    @Override
    public ResourceLocation getIconTexture(@Nullable IStandPower power) {
        ResourceLocation path = getIconTexturePath(power);
        if (power != null && power.hasPower()) {
            path = StandSkinsManager.getInstance().getRemappedResPath(manager -> manager
                    .getStandSkin(power.getStandInstance().get()), path);
        }
        return path;
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
        protected StandPose standPose = StandPose.IDLE;
        private float staminaCost = 0;
        private float staminaCostTick = 0;
        private final Set<StandPart> partsRequired = EnumSet.noneOf(StandPart.class);
        private final List<Supplier<? extends StandAction>> extraUnlockables = new ArrayList<>();
        protected Map<Supplier<? extends StandAction>, List<Supplier<? extends StandAction>>> recoveryFollowUp;

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
        
        public T addExtraUnlockable(Supplier<? extends StandAction> action) {
            if (action != null) {
                extraUnlockables.add(action);
            }
            return getThis();
        }
        
        public T autoSummonStand() {
            this.autoSummonStand = true;
            return getThis();
        }
        
        public T standPose(StandPose pose) {
            if (pose != null) {
                this.standPose = pose;
            }
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
        
        public T attackRecoveryFollowup(Supplier<? extends StandAction> followUp) {
            return attackRecoveryFollowup(followUp, null);
        }
        
        /**
         * @param attack - if equals to null, the attack is the action being constructed, if not - this action will be replaced when the Stand is performing the attack
         * (made this way because you can't have a supplier of the action that is currently being constructed by the builder)
         */
        public T attackRecoveryFollowup(Supplier<? extends StandAction> followUp, @Nullable Supplier<? extends StandAction> attack) {
            if (recoveryFollowUp == null) {
                recoveryFollowUp = new HashMap<>();
            }
            List<Supplier<? extends StandAction>> followUps = recoveryFollowUp.computeIfAbsent(attack, __ -> new ArrayList<>());
            followUps.add(followUp);
            addExtraUnlockable(followUp);
            return getThis();
        }
    }
}
