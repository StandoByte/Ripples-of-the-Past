package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
        this.staminaCost = builder.staminaCost;
        this.staminaCostTick = builder.staminaCostTick;
        this.partsRequired = builder.partsRequired;
        this.extraUnlockables = builder.extraUnlockables;
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
    public boolean isTrained() {
        return isTrained;
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
    public LivingEntity getPerformer(LivingEntity user, IStandPower power) {
        return power.isActive() && (power.getStandManifestation() instanceof StandEntity) ? (StandEntity) power.getStandManifestation() : user;
    }
    
    public static LivingEntity getControlledEntity(LivingEntity user, IStandPower power) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            if (stand.isManuallyControlled()) {
                return stand;
            }
        }
        
        return user;
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
    public void afterPerform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        super.afterPerform(world, user, power, target);
        consumeStamina(world, power);
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
    
    protected boolean autoSummonStand(IStandPower power) {
        return autoSummonStand;
    }
    
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
    
    
    // TODO use this for CrazyDiamondBlockBullet (save the reference to the blood drops effect in StandEntityTask)
    protected static void clWriteTargetedStandEffect(PacketBuffer buf, StandEffectType<?> type, double maxRange) {
        buf.writeVarInt(clGetTargetedStandEffect(type, maxRange).map(effect -> effect.getId()).orElse(-1));
    }
    
    protected static Optional<StandEffectInstance> clGetTargetedStandEffect(StandEffectType<?> type, double maxRange) {
        PlayerEntity user = ClientUtil.getClientPlayer();
        return IStandPower.getStandPowerOptional(user).resolve().flatMap(
                power -> StandEffectsTracker.getTargetLookedAt(power, type, maxRange, user));
    }
    
    protected static Optional<StandEffectInstance> readTargetedStandEffect(PacketBuffer buf, IStandPower power, StandEffectType<?> type) {
        int effectId = buf.readVarInt();
        if (effectId > 0) {
            StandEffectInstance effect = power.getContinuousEffects().getById(effectId);
            if (effect != null && effect.effectType == type
                    && power.getUser() == effect.getStandUser()) {
                return Optional.of(effect);
            }
        }
        
        return Optional.empty();
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
        private final List<Supplier<? extends StandAction>> extraUnlockables = new ArrayList<>();

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
