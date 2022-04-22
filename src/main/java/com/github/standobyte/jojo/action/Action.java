package com.github.standobyte.jojo.action;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class Action<P extends IPower<P, ?>> extends ForgeRegistryEntry<Action<?>> {
    private static Map<Supplier<? extends Action<?>>, Supplier<? extends Action<?>>> SHIFT_VARIATIONS; 
    
    private final int holdDurationToFire;
    private final int holdDurationMax;
    private final float heldSlowDownFactor;
    private final int cooldownTechnical;
    private final int cooldownAdditional;
    private final TargetRequirement targetRequirement;
    private final double maxRangeSqEntityTarget;
    private final double maxRangeSqBlockTarget;
    private final Map<Hand, Function<ItemStack, String>> itemChecks;
    private final boolean ignoresPerformerStun;
    private final boolean swingHand;
    private final boolean cancelsVanillaClick;
    private final Supplier<SoundEvent> shoutSupplier;
    private String translationKey;
    private Action<P> shiftVariation;
    private Action<P> baseVariation;
    
    public Action(Action.AbstractBuilder<?> builder) {
        this.holdDurationMax = builder.holdDurationMax;
        this.holdDurationToFire = builder.holdDurationToFire;
        this.heldSlowDownFactor = builder.heldSlowDownFactor;
        this.cooldownTechnical = builder.cooldownTechnical;
        this.cooldownAdditional = builder.cooldownAdditional;
        this.targetRequirement = builder.needsBlockTarget ? builder.needsEntityTarget ? TargetRequirement.ANY : TargetRequirement.BLOCK : builder.needsEntityTarget ? TargetRequirement.ENTITY : TargetRequirement.NONE;
        this.maxRangeSqEntityTarget = builder.maxRangeSqEntityTarget;
        this.maxRangeSqBlockTarget = builder.maxRangeSqBlockTarget;
        this.itemChecks = builder.itemChecks;
        this.ignoresPerformerStun = builder.ignoresPerformerStun;
        this.swingHand = builder.swingHand;
        this.cancelsVanillaClick = builder.cancelsVanillaClick;
        this.shoutSupplier = builder.shoutSupplier;
        if (builder.shiftVariationOf != null) {
            for (Supplier<? extends Action<?>> action : builder.shiftVariationOf) {
                SHIFT_VARIATIONS.put(action, () -> this);
            }
        }
    }
    
    void setShiftVariation(Action<?> action) {
        Action<P> newShiftVariation = (Action<P>) action;
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
//            if (newShiftVariation.baseVariation != null) {
//                newShiftVariation.baseVariation.shiftVariation = null;
//            }
            this.shiftVariation = newShiftVariation;
            if (newShiftVariation.baseVariation == null) {
                newShiftVariation.baseVariation = this;
            }
        }
    }
    
    public ActionConditionResult checkConditions(LivingEntity user, P power, ActionTarget target) {
        ActionConditionResult itemCheck = checkHeldItems(user, power);
        if (!itemCheck.isPositive()) {
            return itemCheck;
        }
        return checkSpecificConditions(user, power, target);
    }
    
    public boolean sendsConditionMessage() {
        return true;
    }
    
    public TargetRequirement getTargetRequirement() {
        return targetRequirement;
    }
    
    public boolean appropriateTarget(ActionTarget.TargetType targetType) {
        return targetRequirement.checkTargetType(targetType);
    }
    
    public double getMaxRangeSqEntityTarget() {
        return maxRangeSqEntityTarget;
    }
    
    public double getMaxRangeSqBlockTarget() {
        return maxRangeSqBlockTarget;
    }
    
    protected ActionConditionResult checkHeldItems(LivingEntity user, P power) {
        for (Map.Entry<Hand, Function<ItemStack, String>> check : itemChecks.entrySet()) {
            String message = check.getValue().apply(user.getItemInHand(check.getKey()));
            if (message != null) {
                return conditionMessage(message);
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    public boolean ignoresPerformerStun() {
        return ignoresPerformerStun;
    }
    
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, P power, ActionTarget target) {
        return ActionConditionResult.POSITIVE;
    }
    
    public abstract boolean isUnlocked(P power);
    
    protected static ActionConditionResult conditionMessage(String postfix) {
        return ActionConditionResult.createNegative(new TranslationTextComponent("jojo.message.action_condition." + postfix));
    }
    
    public Action<P> getShiftVariationIfPresent() {
        return hasShiftVariation() ? shiftVariation : this;
    }
    
    @Nullable
    public Action<P> getBaseVariation() {
        return baseVariation;
    }
    
    public boolean hasShiftVariation() {
        return shiftVariation != null;
    }
    
    public boolean isShiftVariation() {
        return baseVariation != null;
    }
    
    public void onPerform(World world, LivingEntity user, P power, ActionTarget target) {
        if (user instanceof ServerPlayerEntity) {
            ModCriteriaTriggers.ACTION_PERFORM.get().trigger((ServerPlayerEntity) user, this);
        }
        perform(world, user, power, target);
    }
    
    protected void perform(World world, LivingEntity user, P power, ActionTarget target) {}
    
    public void startedHolding(World world, LivingEntity user, P power, ActionTarget target, boolean requirementsFulfilled) {}
    
    public void onHoldTick(World world, LivingEntity user, P power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        holdTick(world, user, power, ticksHeld, target, requirementsFulfilled);
    }
    
    protected void holdTick(World world, LivingEntity user, P power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {}
    
    public void stoppedHolding(World world, LivingEntity user, P power, int ticksHeld) {}
    
    public boolean isHeldSentToTracking() {
        return false;
    }
    
    public void onHoldTickClientEffect(LivingEntity user, P power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {}

    public LivingEntity getPerformer(LivingEntity user, P power) {
        return user;
    }
    
    public void onClick(World world, LivingEntity user, P power) {}
    
    public int getCooldownTechnical(P power) {
        return cooldownTechnical;
    }
    
    protected int getCooldownAdditional(P power, int ticksHeld) {
        return power.isUserCreative() ? 0 : cooldownAdditional * 0;
    }
    
    public int getCooldown(P power, int ticksHeld) {
        return getCooldownTechnical(power) + getCooldownAdditional(power, ticksHeld);
    }

    public boolean swingHand() {
        return swingHand;
    }

    public boolean cancelsVanillaClick() {
        return cancelsVanillaClick;
    }
    
    @Nullable
    protected SoundEvent getShout(LivingEntity user, P power, ActionTarget target, boolean wasActive) {
        return shoutSupplier.get();
    }
    
    public void playVoiceLine(LivingEntity user, P power, ActionTarget target, boolean wasActive, boolean shift) {
        if (!shift || isShiftVariation()) {
            SoundEvent shout = getShout(user, power, target, wasActive);
            if (shout != null) {
                JojoModUtil.sayVoiceLine(user, shout);
            }
        }
    }
    
    public float getHeldSlowDownFactor() {
        return heldSlowDownFactor;
    }
    
    public int getHoldDurationToFire(P power) { 
        return holdDurationToFire;
    }
    
    public int getHoldDurationMax(P power) {
        return holdDurationMax;
    }
    
    public boolean holdOnly() {
        return holdDurationToFire == 0 && holdDurationMax > 0;
    }
    
    public boolean cancelHeldOnGettingAttacked(P power) {
        return false;
    }
    
    protected String getTranslationKey(P power, ActionTarget target) {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("action", this.getRegistryName());
        }
        return this.translationKey;
    }
    
    public ITextComponent getName(P power, ActionTarget target) {
        return getTranslatedName(power, getTranslationKey(power, target));
    }
    
    public ITextComponent getNameShortened(P power, ActionTarget target) {
        return ClientUtil.shortenedTranslationExists(getTranslationKey(power, target)) ? 
                getTranslatedName(power, ClientUtil.getShortenedTranslationKey(getTranslationKey(power, target)))
                : getName(power, target);
    }
    
    protected TranslationTextComponent getTranslatedName(P power, String key) {
        return new TranslationTextComponent(key);
    }
    
    @Nullable
    public ActionType getActionType(P power) {
        for (Action<P> attack : power.getAttacks()) {
            if (attack == this || attack.getShiftVariationIfPresent() == this) {
                return ActionType.ATTACK;
            }
        }
        for (Action<P> ability : power.getAbilities()) {
            if (ability == this || ability.getShiftVariationIfPresent() == this) {
                return ActionType.ABILITY;
            }
        }
        return null;
    }
    
    public boolean isTrained() {
        return false;
    }
    
    public float getMaxTrainingPoints(P power) {
        return 1F;
    }
    
    public void onMaxTraining(P power) {}
    
    public boolean canUserSeeInStoppedTime(LivingEntity user, P power) {
        return false;
    }
    
    
    
    public static void initShiftVariationsMap() {
        SHIFT_VARIATIONS = new HashMap<>();
    }
    
    public static void initShiftVariations() {
        if (SHIFT_VARIATIONS != null) {
            for (Map.Entry<Supplier<? extends Action<?>>, Supplier<? extends Action<?>>> entry : SHIFT_VARIATIONS.entrySet()) {
                entry.getKey().get().setShiftVariation(entry.getValue().get());
            }
            SHIFT_VARIATIONS = null;
        }
    }
    
    public static enum TargetRequirement {
        NONE(target -> true),
        BLOCK(target -> target == TargetType.BLOCK),
        ENTITY(target -> target == TargetType.ENTITY),
        ANY(target -> target != TargetType.EMPTY);
        
        private final Predicate<TargetType> targetTypePredicate;
        
        private TargetRequirement(Predicate<TargetType> targetTypePredicate) {
            this.targetTypePredicate = targetTypePredicate;
        }
        
        public boolean checkTargetType(TargetType targetType) {
            return targetTypePredicate.test(targetType);
        }
    }
    
    
    
    public static class Builder extends AbstractBuilder<Builder> {

        @Override
        protected Action.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends Action.AbstractBuilder<T>> {
        private int holdDurationToFire = 0;
        private int holdDurationMax = 0;
        private float heldSlowDownFactor = 1.0F;
        private int cooldownTechnical;
        private int cooldownAdditional;
        private boolean needsEntityTarget = false;
        private boolean needsBlockTarget = false;
        private static final double MAX_RANGE_ENTITY_TARGET = 6.0D;
        private static final double MAX_RANGE_BLOCK_TARGET = 8.0D;
        private double maxRangeSqEntityTarget = MAX_RANGE_ENTITY_TARGET * MAX_RANGE_ENTITY_TARGET;
        private double maxRangeSqBlockTarget = MAX_RANGE_BLOCK_TARGET * MAX_RANGE_BLOCK_TARGET;
        private Map<Hand, Function<ItemStack, String>> itemChecks = new EnumMap<>(Hand.class);
        private boolean ignoresPerformerStun = false;
        private boolean swingHand = false;
        private boolean cancelsVanillaClick = true;
        private Supplier<SoundEvent> shoutSupplier = () -> null;
        protected List<Supplier<? extends Action<?>>> shiftVariationOf = new ArrayList<>();
        
        public T cooldown(int technical, int additional) {
            this.cooldownTechnical = technical;
            this.cooldownAdditional = additional;
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
        
        public T itemCheck(Hand hand, Predicate<ItemStack> itemCheck, String message) {
            itemChecks.put(hand, itemStack -> itemCheck.test(itemStack) ? null : message);
            return getThis();
        }
        
        public T emptyMainHand() {
            return itemCheck(Hand.MAIN_HAND, ItemStack::isEmpty, "hand");
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
        
        public T holdType(int maxHoldTicks) {
            this.holdDurationToFire = 0;
            this.holdDurationMax = maxHoldTicks;
            return getThis();
        }
        
        public T holdType() {
            return holdType(Integer.MAX_VALUE);
        }
        
        public T holdToFire(int ticksToFire, boolean continueHolding) {
            this.holdDurationToFire = ticksToFire;
            this.holdDurationMax = continueHolding ? Integer.MAX_VALUE : ticksToFire;
            return getThis();
        }
        
        public T shiftVariationOf(Supplier<? extends Action<?>> action) {
            this.shiftVariationOf.add(action);
            return getThis();
        }
        
        protected abstract T getThis();
    }
}
