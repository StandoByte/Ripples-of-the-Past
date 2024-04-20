package com.github.standobyte.jojo.action;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.util.general.LazySupplier;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class Action<P extends IPower<P, ?>> extends ForgeRegistryEntry<Action<?>> {
    private static final Map<Supplier<? extends Action<?>>, Supplier<? extends Action<?>>> SHIFT_VARIATIONS = new HashMap<>(); 
    
    private final int holdDurationToFire;
    private final int holdDurationMax;
    protected final boolean continueHolding;
    private final float heldWalkSpeed;
    private final int cooldownTechnical;
    private final int cooldownAdditional;
    private final boolean needsFreeMainHand;
    private final boolean ignoresPerformerStun;
    private final boolean swingHand;
    private final boolean withUserPunch;
    private final Supplier<SoundEvent> shoutSupplier;
    private String translationKey;
    private Action<P> shiftVariation;
    private Action<P> baseVariation;
    
    public Action(Action.AbstractBuilder<?> builder) {
        this.holdDurationMax = builder.holdDurationMax;
        this.holdDurationToFire = builder.holdDurationToFire;
        this.continueHolding = builder.continueHolding;
        this.heldWalkSpeed = builder.heldWalkSpeed;
        this.cooldownTechnical = builder.cooldownTechnical;
        this.cooldownAdditional = builder.cooldownAdditional;
        this.needsFreeMainHand = builder.needsFreeMainHand;
        this.ignoresPerformerStun = builder.ignoresPerformerStun;
        this.swingHand = builder.swingHand;
        this.withUserPunch = builder.withUserPunch;
        this.shoutSupplier = builder.shoutSupplier;
        for (Supplier<? extends Action<?>> action : builder.shiftVariationOf) {
            SHIFT_VARIATIONS.put(action, () -> this);
        }
        for (Supplier<? extends Action<?>> action : builder.addShiftVariation) {
            SHIFT_VARIATIONS.put(() -> this, action);
        }
    }
    
    public abstract PowerClassification getPowerClassification();
    
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
        if (!user.canUpdate() && !canBeUsedByStoppedInTime(user, power)) {
            return ActionConditionResult.NEGATIVE;
        }
        return checkSpecificConditions(user, power, target);
    }
    
    public boolean sendsConditionMessage() {
        return true;
    }
    
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.NONE;
    }
    
    public ActionConditionResult checkRangeAndTarget(ActionTarget target, LivingEntity user, P power) {
        boolean continueHold = holdOnly(power);
        
        LivingEntity performer = getPerformer(power.getUser(), power);
        boolean targetTooFar = false;
        switch (target.getType()) {
        case ENTITY:
            Entity targetEntity = target.getEntity();
            double rangeSq = getMaxRangeSqEntityTarget();
            if (!performer.canSee(targetEntity)) {
                rangeSq /= 4.0D;
            }
            double distanceToEntity = JojoModUtil.getDistance(performer, targetEntity.getBoundingBox());
            if (distanceToEntity * distanceToEntity > rangeSq) {
                targetTooFar = true;
            }
            break;
        case BLOCK:
            BlockPos targetPos = target.getBlockPos();
            int buildLimit = 256;
            if (targetPos.getY() < buildLimit - 1 || target.getFace() != Direction.UP && targetPos.getY() < buildLimit) {
                double maxDistSq = getMaxRangeSqBlockTarget();
                if (user.level.getBlockState(targetPos).getBlock() == Blocks.AIR) {
                    return ActionConditionResult.NEGATIVE.setContinueHold(continueHold);
                }
                targetTooFar = target.getBoundingBox(performer.level).map(aabb -> {
                    double distance = JojoModUtil.getDistance(performer, aabb);
                    return distance * distance > maxDistSq;
                }).orElse(true);
            }
            break;
        default:
            break;
        }

        if (targetTooFar) {
            return conditionMessage("target_too_far").setContinueHold(continueHold);
        }
        
        ActionConditionResult targetCheck = checkTarget(target, user, power).setContinueHold(continueHold);
        return targetCheck;
    }
    
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, P power) {
        return ActionConditionResult.POSITIVE;
    }
    
    public double getMaxRangeSqEntityTarget() {
        return 64;
    }
    
    public double getMaxRangeSqBlockTarget() {
        return 100;
    }
    
    public abstract float getCostToRender(P power, ActionTarget target);
    
    protected ActionConditionResult checkHeldItems(LivingEntity user, P power) {
        if (needsFreeMainHand && !MCUtil.isHandFree(user, Hand.MAIN_HAND)) {
            return conditionMessage("hand");
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
    
    public boolean isTrained() {
        return false;
    }
    
    @Nullable
    public Action<P> getVisibleAction(P power, ActionTarget target) {
        if (isUnlocked(power)) {
            Action<P> replacingVariation = replaceAction(power, target);
            return replacingVariation == null || replacingVariation.isUnlocked(power) ? replacingVariation : this;
        }
        return null;
    }
    
    @Nullable
    protected Action<P> replaceAction(P power, ActionTarget target) {
        return this;
    }
    
    public boolean enabledInHudDefault() {
        return true;
    }
    
    public boolean isLegalInHud(P power) {
        return true;
    }
    
    public static ActionConditionResult conditionMessage(String postfix) {
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
    
    public void onClick(World world, LivingEntity user, P power) {}
    
    public void afterClick(World world, LivingEntity user, P power, boolean passedRequirements) {}
    
    public void overrideVanillaMouseTarget(ObjectWrapper<ActionTarget> targetContainer, World world, LivingEntity user, P power) {}
    
    public ActionTarget targetBeforePerform(World world, LivingEntity user, P power, ActionTarget target) {
        return target;
    }
    
    public void onPerform(World world, LivingEntity user, P power, ActionTarget target) {
        perform(world, user, power, target);
        if (swingHand() && withUserPunch() && user instanceof PlayerEntity) {
            ((PlayerEntity) user).resetAttackStrengthTicker();
        }
    }
    
    protected void perform(World world, LivingEntity user, P power, ActionTarget target) {}
    
    public void startedHolding(World world, LivingEntity user, P power, ActionTarget target, boolean requirementsFulfilled) {}
    
    public void onHoldTick(World world, LivingEntity user, P power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        holdTick(world, user, power, ticksHeld, target, requirementsFulfilled);
    }
    
    protected void holdTick(World world, LivingEntity user, P power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {}
    
    public void stoppedHolding(World world, LivingEntity user, P power, int ticksHeld, boolean willFire) {}
    
    public boolean isHeldSentToTracking() {
        return false;
    }
    
    public void onHoldTickClientEffect(LivingEntity user, P power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {}
    
    public LivingEntity getPerformer(LivingEntity user, P power) {
        return user;
    }
    
    public int getCooldownTechnical(P power) {
        return cooldownTechnical;
    }
    
    protected int getCooldownAdditional(P power, int ticksHeld) {
        return power.isUserCreative() ? 0 : cooldownAdditional;
    }
    
    protected final int cooldownFromHoldDuration(int cooldown, P power, int ticksHeld) {
        if (getHoldDurationMax(power) > 0) {
            cooldown = (int) ((float) (cooldown * ticksHeld) / (float) getHoldDurationMax(power));
        }
        return cooldown;
    }
    
    public int getCooldown(P power, int ticksHeld) {
        return getCooldownTechnical(power) + getCooldownAdditional(power, ticksHeld);
    }

    public boolean swingHand() {
        return swingHand;
    }

    public boolean withUserPunch() {
        return withUserPunch;
    }
    
    @Nullable
    protected SoundEvent getShout(LivingEntity user, P power, ActionTarget target, boolean wasActive) {
        return shoutSupplier.get();
    }
    
    public void playVoiceLine(LivingEntity user, P power, ActionTarget target, boolean wasActive, boolean sneak) {
        if (!sneak || playsVoiceLineOnSneak()) {
            SoundEvent shout = getShout(user, power, target, wasActive);
            if (shout != null) {
                JojoModUtil.sayVoiceLine(user, shout);
            }
        }
    }
    
    protected boolean playsVoiceLineOnSneak() {
        return isShiftVariation();
    }
    
    public float getHeldWalkSpeed() {
        return heldWalkSpeed;
    }
    
    public int getHoldDurationToFire(P power) { 
        return holdDurationToFire;
    }
    
    public int getHoldDurationMax(P power) {
        return holdOnly(power) || continueHolding ? holdDurationMax : getHoldDurationToFire(power);
    }
    
    public boolean holdOnly(P power) {
        return holdDurationToFire == 0 && holdDurationMax > 0;
    }
    
    public boolean cancelHeldOnGettingAttacked(P power, DamageSource dmgSource, float dmgAmount) {
        return false;
    }
    
    public boolean heldAllowsOtherAction(P power, Action<P> action) {
        return false;
    }
    
    public void appendWarnings(List<ITextComponent> warnings, P power, PlayerEntity clientPlayerUser) {}
    
    public boolean greenSelection(P power, ActionConditionResult conditionCheck) {
        return false;
    }
    
    public String getTranslationKey(P power, ActionTarget target) {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("action", this.getRegistryName());
        }
        return this.translationKey;
    }
    
    public IFormattableTextComponent getTranslatedName(P power, String key) {
        return new TranslationTextComponent(key);
    }
    
    public IFormattableTextComponent getNameShortened(P power, String key) {
        return getTranslatedName(power, ClientUtil.getShortenedTranslationKey(key));
    }
    
    public IFormattableTextComponent getNameLocked(P power) {
        return new TranslationTextComponent("jojo.layout_edit.locked");
    }
    
    @Deprecated
    public ResourceLocation getTexture(P power) {
        return getRegistryName();
    }

    @Deprecated
    public Stream<ResourceLocation> getTexLocationstoLoad() {
        return Stream.of(getRegistryName());
    }

    private final LazySupplier<ResourceLocation> iconTexture = 
            new LazySupplier<>(() -> makeIconVariant(this, ""));
    public ResourceLocation getIconTexture(@Nullable P power) {
        return getIconTexturePath(power);
    }
    
    protected ResourceLocation getIconTexturePath(@Nullable P power) {
        return iconTexture.get();
    }
    
    protected static final ResourceLocation makeIconVariant(Action<?> action, @Nullable String postfix) {
        return JojoModUtil.makeTextureLocation("action", 
                action.getRegistryName().getNamespace(), 
                action.getRegistryName().getPath() + postfix);
    }
    
    public boolean canUserSeeInStoppedTime(LivingEntity user, P power) {
        return false;
    }
    
    public boolean canBeUsedByStoppedInTime(LivingEntity user, P power) {
        return canUserSeeInStoppedTime(user, power);
    }
    
    public void onCommonSetup() {}
    
    
    
    public static void initShiftVariations() {
        if (SHIFT_VARIATIONS != null) {
            Iterator<Map.Entry<Supplier<? extends Action<?>>, Supplier<? extends Action<?>>>> it = SHIFT_VARIATIONS.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Supplier<? extends Action<?>>, Supplier<? extends Action<?>>> entry = it.next();
                entry.getKey().get().setShiftVariation(entry.getValue().get());
                it.remove();
            }
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
        private boolean continueHolding = false;
        private float heldWalkSpeed = 1.0F;
        
        private int cooldownTechnical;
        private int cooldownAdditional;
        
//        private static final double MAX_RANGE_ENTITY_TARGET = 6.0D;
//        private static final double MAX_RANGE_BLOCK_TARGET = 8.0D;
//        private double maxRangeSqEntityTarget = MAX_RANGE_ENTITY_TARGET * MAX_RANGE_ENTITY_TARGET;
//        private double maxRangeSqBlockTarget = MAX_RANGE_BLOCK_TARGET * MAX_RANGE_BLOCK_TARGET;
        
        private boolean needsFreeMainHand = false;
        private boolean ignoresPerformerStun = false;
        private boolean swingHand = false;
        private boolean withUserPunch = false;
        private Supplier<SoundEvent> shoutSupplier = () -> null;
        protected List<Supplier<? extends Action<?>>> shiftVariationOf = new ArrayList<>();
        protected List<Supplier<? extends Action<?>>> addShiftVariation = new ArrayList<>();
        
        public T cooldown(int cooldown) {
            return cooldown(0, cooldown);
        }
        
        public T cooldown(int technical, int additional) {
            this.cooldownTechnical = technical;
            this.cooldownAdditional = additional;
            return getThis();
        }
        
        public T needsFreeMainHand() {
            this.needsFreeMainHand = true;
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
        
        @Deprecated
        public T doNotCancelClick() {
            return withUserPunch();
        }
        
        public T withUserPunch() {
            this.withUserPunch = true;
            return getThis();
        }
        
        public T shout(Supplier<SoundEvent> shoutSupplier) {
            this.shoutSupplier = shoutSupplier;
            return getThis();
        }
        
        public T heldWalkSpeed(float walkSpeed) {
            this.heldWalkSpeed = MathHelper.clamp(walkSpeed, 0, 1);
            return getThis();
        }
        
        public T holdType(int maxHoldTicks) {
            this.holdDurationMax = maxHoldTicks;
            return getThis();
        }
        
        public T holdType() {
            return holdType(Integer.MAX_VALUE);
        }
        
        public T holdToFire(int ticksToFire, boolean continueHolding) {
            if (ticksToFire > 0) {
                this.holdDurationToFire = ticksToFire;
                this.holdDurationMax = Integer.MAX_VALUE;
                this.continueHolding = continueHolding;
            }
            else {
                this.holdDurationToFire = 0;
                this.holdDurationMax = 0;
                this.continueHolding = false;
            }
            return getThis();
        }
        
        public T shiftVariationOf(Supplier<? extends Action<?>> base) {
            this.shiftVariationOf.add(base);
            return getThis();
        }
        
        public T addShiftVariation(Supplier<? extends Action<?>> shift) {
            this.addShiftVariation.add(shift);
            return getThis();
        }
        
        protected abstract T getThis();
    }
    
    
    
    public static class JsonSerialization implements JsonSerializer<Action<?>>, JsonDeserializer<Action<?>> {
        public static final JsonSerialization INSTANCE = new JsonSerialization();
        
        protected JsonSerialization() {}

        @Override
        public Action<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonPrimitive() && ((JsonPrimitive) json).isString()) {
                return JojoCustomRegistries.ACTIONS.fromId(new ResourceLocation(json.getAsString()));
            }
            
            throw new JsonParseException("An action is defined by its string id");
        }

        @Override
        public JsonElement serialize(Action<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getRegistryName().toString());
        }
        
    }
}
