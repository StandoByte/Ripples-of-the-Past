package com.github.standobyte.jojo.action;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class Action extends ForgeRegistryEntry<Action> {
    private static Map<Supplier<? extends Action>, Supplier<? extends Action>> SHIFT_VARIATIONS = new HashMap<>();
    
    private final float manaCost;
    private final float heldTickManaCost;
    private final int holdDurationToFire;
    private final int holdDurationMax;
    private final float heldSlowDownFactor;
    private final int cooldown;
    private final TargetRequirement targetRequirement;
    private final double maxRangeSqEntityTarget;
    private final double maxRangeSqBlockTarget;
    private final boolean ignoresPerformerStun;
    private final boolean swingHand;
    private final boolean cancelsVanillaClick;
    private final Supplier<SoundEvent> shoutSupplier;
    private String translationKey;
    private Action shiftVariation;
    private Action baseVariation;
    
    public Action(Action.AbstractBuilder<?> builder) {
        this.manaCost = builder.manaCost;
        this.heldTickManaCost = builder.heldTickManaCost;
        this.holdDurationMax = builder.holdDurationMax;
        this.holdDurationToFire = builder.holdDurationToFire;
        this.heldSlowDownFactor = builder.heldSlowDownFactor;
        this.cooldown = builder.cooldown;
        this.targetRequirement = builder.needsBlockTarget ? builder.needsEntityTarget ? TargetRequirement.ANY : TargetRequirement.BLOCK : builder.needsEntityTarget ? TargetRequirement.ENTITY : TargetRequirement.NONE;
        this.maxRangeSqEntityTarget = builder.maxRangeSqEntityTarget;
        this.maxRangeSqBlockTarget = builder.maxRangeSqBlockTarget;
        this.ignoresPerformerStun = builder.ignoresPerformerStun;
        this.swingHand = builder.swingHand;
        this.cancelsVanillaClick = builder.cancelsVanillaClick;
        this.shoutSupplier = builder.shoutSupplier;
        if (builder.shiftVariationOf != null) {
            SHIFT_VARIATIONS.put(builder.shiftVariationOf, () -> this);
        }
    }
    
    protected void setShiftVariation(Action newShiftVariation) {
        if (newShiftVariation != this) {
            if (newShiftVariation.shiftVariation != null) {
                newShiftVariation.shiftVariation.baseVariation = null;
                newShiftVariation.shiftVariation = null;
            }
            if (this.shiftVariation != null) {
                this.shiftVariation.baseVariation = null;
            }
            if (this.baseVariation != null) {
                baseVariation.shiftVariation = null;
                baseVariation = null;
            }
            if (newShiftVariation.baseVariation != null) {
                newShiftVariation.baseVariation.shiftVariation = null;
            }
            this.shiftVariation = newShiftVariation;
            newShiftVariation.baseVariation = this;
        }
    }
    
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        return ActionConditionResult.POSITIVE;
    }
    
    protected static ActionConditionResult conditionMessage(String postfix) {
        return ActionConditionResult.createNegative(new TranslationTextComponent("jojo.message.action_condition." + postfix));
    }
    
    public Action getShiftVariationIfPresent() {
        return hasShiftVariation() ? shiftVariation : this;
    }
    
    @Nullable
    protected Action getBaseVariation() {
        return baseVariation;
    }
    
    public boolean hasShiftVariation() {
        return shiftVariation != null;
    }
    
    protected boolean isShiftVariation() {
        return baseVariation != null;
    }
    
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {}
    
    public void onStartedHolding(World world, LivingEntity user, IPower<?> power, ActionTarget target, boolean requirementsFulfilled) {}
    
    public void onHoldTickUser(World world, LivingEntity user, IPower<?> power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {}
    
    public void onStoppedHolding(World world, LivingEntity user, IPower<?> power, int ticksHeld) {}
    
    public boolean isHeldSentToTracking() {
        return false;
    }
    
    public void onHoldTickClientEffect(LivingEntity user, IPower<?> power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {}
    
    public LivingEntity getPerformer(LivingEntity user, IPower<?> power) {
        return user;
    }

    public void updatePerformer(World world, LivingEntity user, IPower<?> power) {}
    
    protected ActionTarget aim(World world, LivingEntity user, IPower<?> power, double range) {
        return ActionTarget.fromRayTraceResult(JojoModUtil.rayTrace(user, range, entity -> 
        entity instanceof LivingEntity && user.canAttack((LivingEntity) entity)));
    }
    
    public float getManaNeeded(int ticksHeld, IPower<?> power) {
        if (getHoldDurationMax() > 0) {
            return getManaCost() + getHeldTickManaCost() * Math.max((getHoldDurationToFire(power) - ticksHeld), 1);
        }
        return getManaCost();
    }
    
    public float getManaCost() {
        return manaCost;
    }
    
    public int getCooldownValue() {
        return cooldown;
    }
    
    public int getCooldown(IPower<?> power, int ticksHeld) {
        return getCooldownValue();
    }
    
    public TargetRequirement getTargetRequirement() {
        return targetRequirement;
    }
    
    public boolean appropriateTarget(ActionTarget.TargetType targetType) {
        if (targetRequirement == TargetRequirement.NONE) {
            return true;
        }
        switch (targetType) {
        case EMPTY:
            return false;
        case BLOCK:
            return targetRequirement == TargetRequirement.ANY || targetRequirement == TargetRequirement.BLOCK;
        case ENTITY:
            return targetRequirement == TargetRequirement.ANY || targetRequirement == TargetRequirement.ENTITY;
        }
        return false;
    }
    
    public double getMaxRangeSqEntityTarget() {
        return maxRangeSqEntityTarget;
    }
    
    public double getMaxRangeSqBlockTarget() {
        return maxRangeSqBlockTarget;
    }

    public boolean ignoresPerformerStun() {
        return ignoresPerformerStun;
    }

    public boolean swingHand() {
        return swingHand;
    }

    public boolean cancelsVanillaClick() {
        return cancelsVanillaClick;
    }
    
    @Nullable
    protected SoundEvent getShout(LivingEntity user, IPower<?> power, ActionTarget target, boolean wasActive) {
        return shoutSupplier.get();
    }
    
    public void playVoiceLine(LivingEntity user, IPower<?> power, ActionTarget target, boolean wasActive, boolean shift) {
        if (!shift || isShiftVariation()) {
            SoundEvent shout = getShout(user, power, target, wasActive);
            if (shout != null) {
                JojoModUtil.sayVoiceLine(user, shout);
            }
        }
    }
    
    public float getHeldTickManaCost() {
        return heldTickManaCost;
    }
    
    public float getHeldSlowDownFactor() {
        return heldSlowDownFactor;
    }
    
    public int getHoldDurationToFire(IPower<?> power) { 
        return holdDurationToFire;
    }
    
    public int getHoldDurationMax() {
        return holdDurationMax;
    }
    
    public boolean holdOnly() {
        return holdDurationToFire == 0 && holdDurationMax > 0;
    }
    
    protected String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("action", this.getRegistryName());
        }
        return this.translationKey;
    }
    
    public ITextComponent getName(IPower<?> power) {
        return getTranslatedName(power, getTranslationKey());
    }
    
    public ITextComponent getNameShortened(IPower<?> power) {
        return ClientUtil.shortenedTranslationExists(getTranslationKey()) ? 
                getTranslatedName(power, ClientUtil.getShortenedTranslationKey(getTranslationKey()))
                : getName(power);
    }
    
    protected TranslationTextComponent getTranslatedName(IPower<?> power, String key) {
        return new TranslationTextComponent(key);
    }
    
    
    
    public static void initShiftVariations() {
        if (SHIFT_VARIATIONS != null) {
            for (Map.Entry<Supplier<? extends Action>, Supplier<? extends Action>> entry : SHIFT_VARIATIONS.entrySet()) {
                entry.getKey().get().setShiftVariation(entry.getValue().get());
            }
            SHIFT_VARIATIONS = null;
        }
    }
    
    public static enum TargetRequirement {
        NONE,
        BLOCK,
        ENTITY,
        ANY
    }
    
    
    
    public static class Builder extends AbstractBuilder<Builder> {

        @Override
        protected Action.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends Action.AbstractBuilder<T>> {
        private float manaCost = 0;
        private float heldTickManaCost = 0;
        private int holdDurationToFire = 0;
        private int holdDurationMax = 0;
        private float heldSlowDownFactor = 1.0F;
        private int cooldown = 0;
        private boolean needsEntityTarget = false;
        private boolean needsBlockTarget = false;
        private static final double MAX_RANGE_ENTITY_TARGET = 6.0D;
        private static final double MAX_RANGE_BLOCK_TARGET = 8.0D;
        private double maxRangeSqEntityTarget = MAX_RANGE_ENTITY_TARGET * MAX_RANGE_ENTITY_TARGET;
        private double maxRangeSqBlockTarget = MAX_RANGE_BLOCK_TARGET * MAX_RANGE_BLOCK_TARGET;
        private boolean ignoresPerformerStun = false;
        private boolean swingHand = false;
        private boolean cancelsVanillaClick = true;
        private Supplier<SoundEvent> shoutSupplier = () -> null;
        protected Supplier<? extends Action> shiftVariationOf = null;
        
        public T manaCost(float manaCost) {
            this.manaCost = manaCost;
            return getThis();
        }
        
        public T cooldown(int cooldown) {
            this.cooldown = cooldown;
            return getThis();
        }
        
        public T needsEntityTarget() {
            this.needsEntityTarget = true;
            return getThis();
        }
        
        public T needsBlockTarget() {
            this.needsBlockTarget = true;
            return getThis();
        }
        
        public T maxRangeEntityTarget(double maxRangeEntityTarget) {
            maxRangeEntityTarget = MathHelper.clamp(maxRangeEntityTarget, 0, MAX_RANGE_ENTITY_TARGET);
            this.maxRangeSqEntityTarget = maxRangeEntityTarget * maxRangeEntityTarget;
            return getThis();
        }
        
        public T maxRangeBlockTarget(double maxRangeBlockTarget) {
            maxRangeBlockTarget = MathHelper.clamp(maxRangeBlockTarget, 0, MAX_RANGE_BLOCK_TARGET);
            this.maxRangeSqBlockTarget = maxRangeBlockTarget * maxRangeBlockTarget;
            return getThis();
        }
        
        public T ignoresPerformerStun() {
            this.ignoresPerformerStun = true;
            return getThis();
        }
        
        public T swingHand() {
            this.swingHand = true;
            return getThis();
        }
        
        public T doNotCancelClick() {
            this.cancelsVanillaClick = false;
            return getThis();
        }
        
        public T shout(Supplier<SoundEvent> shoutSupplier) {
            this.shoutSupplier = shoutSupplier;
            return getThis();
        }
        
        public T heldSlowDownFactor(float slowDownFactor) {
            this.heldSlowDownFactor = MathHelper.clamp(slowDownFactor, 0, 1);
            return getThis();
        }
        
        public T holdType(float tickManaCost, int maxHoldTicks) {
            this.holdDurationToFire = 0;
            this.holdDurationMax = maxHoldTicks;
            this.heldTickManaCost = tickManaCost;
            return getThis();
        }
        
        public T holdType(float tickManaCost) {
            return holdType(tickManaCost, Integer.MAX_VALUE);
        }
        
        public T holdToFire(int ticksToFire, boolean continueHolding, float tickManaCost) {
            this.holdDurationToFire = ticksToFire;
            this.holdDurationMax = continueHolding ? Integer.MAX_VALUE : ticksToFire;
            this.heldTickManaCost = tickManaCost;
            return getThis();
        }
        
        public T shiftVariationOf(Supplier<? extends Action> action) {
            this.shiftVariationOf = action;
            return getThis();
        }
        
        protected abstract T getThis();
    }
}
