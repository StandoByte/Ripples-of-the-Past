package com.github.standobyte.jojo.action.actions;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class StandEntityAction extends StandAction {
    private final float staminaCost;
    protected final int standWindupDuration;
    protected final int standPerformDuration;
    protected final int standRecoveryDuration;
    private final AutoSummonMode autoSummonMode;
    private final TargetRequirement crosshairTargetForStand;
    private final boolean isCancelable;
    public final float userMovementFactor;
    public final StandPose standPose;
    @Nullable
    protected final StandRelativeOffset userOffset;
    @Nullable
    protected final StandRelativeOffset userOffsetArmsOnly;
    public final boolean enablePhysics;
    private final Supplier<SoundEvent> standSoundSupplier;
    
    public StandEntityAction(StandEntityAction.AbstractBuilder<?> builder) {
        super(builder);
        this.staminaCost = builder.staminaCost;
        this.standWindupDuration = builder.standWindupDuration;
        this.standPerformDuration = builder.standPerformDuration;
        this.standRecoveryDuration = builder.standRecoveryDuration;
        this.autoSummonMode = builder.autoSummonMode;
        this.crosshairTargetForStand = builder.crosshairTargetForStand;
        this.isCancelable = builder.isCancelable;
        this.userMovementFactor = builder.userMovementFactor;
        this.standPose = builder.standPose;
        this.userOffset = builder.userOffset;
        this.userOffsetArmsOnly = builder.userOffsetArmsOnly;
        this.enablePhysics = builder.enablePhysics;
        this.standSoundSupplier = builder.standSoundSupplier;
    }
    
    @Override
    public LivingEntity getPerformer(LivingEntity user, IStandPower power) {
        return power.isActive() ? (StandEntity) power.getStandManifestation() : user;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            ActionConditionResult checkStand = checkStandConditions(stand, power, target);
            if (stand.isArmsOnlyMode() && !allowArmsOnly()) {
                return ActionConditionResult.NEGATIVE;
            }
            if (!checkStand.isPositive()) {
                return checkStand;
            }
        }
        return super.checkConditions(user, power, target);
    }
    
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return ActionConditionResult.POSITIVE;
    }
    
    public void standTickButtonHold(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {}
    
    public void standTickWindup(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {}
    
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {}
    
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {}
    
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return standWindupDuration;
    }
    
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return standPerformDuration;
    }
    
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return standRecoveryDuration;
    }
    
    @Override
    public void onClick(World world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide() && !power.isActive()) {
            switch (autoSummonMode) {
            case FULL:
                power.getType().summon(user, power, true);
                break;
            case ARMS:
                ((EntityStandType<?>) power.getType()).summon(user, power, entity -> entity.setArmsOnlyMode(), true);
                break;
            case ONE_ARM:
                ((EntityStandType<?>) power.getType()).summon(user, power, entity -> entity.setArmsOnlyMode(true, false), true);
                break;
            default:
                break;
            }
        }
    }
    
    @Override
    public float getStaminaCost(IStandPower stand) {
        return staminaCost;
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, IStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            invokeForStand(power, stand -> setAction(power, stand, 
                    !holdOnly() ? getHoldDurationToFire(power) : getHoldDurationMax(power), 
                    !holdOnly() ? Phase.BUTTON_HOLD : Phase.PERFORM, 
                    target));
        }
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, IStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {}
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, IStandPower power, int ticksHeld) {
        if (!world.isClientSide()) {
            invokeForStand(power, stand -> stand.stopTask());
        }
    }

    @Override
    protected final void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            invokeForStand(power, stand -> {
                int windupTicks = getStandWindupTicks(power, stand);
                int ticks = windupTicks > 0 ? windupTicks : getStandActionTicks(power, stand);
                Phase phase = windupTicks > 0 ? Phase.WINDUP : Phase.PERFORM;
                setAction(power, stand, ticks, phase, target);
            });
        }
    }
    
    protected boolean allowArmsOnly() {
        return autoSummonMode == AutoSummonMode.ARMS || autoSummonMode == AutoSummonMode.ONE_ARM;
    }
    
    private void setAction(IStandPower standPower, StandEntity standEntity, int ticks, Phase phase, ActionTarget target) {
        if (standEntity.setTask(this, ticks, phase)) {
            if (standTakesCrosshairTarget(target)) {
                standEntity.setTaskTarget(target);
            }
        }
    }
    
    public boolean canBeScheduled(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase) {}
    
    public void playSound(StandEntity standEntity, IStandPower standPower, Phase phase) {
        SoundEvent sound = getSound(standEntity, standPower);
        if (sound != null) {
            playSoundAtStand(standEntity.level, standEntity, sound, standPower, phase);
        }
    }
    
    @Nullable
    protected SoundEvent getSound(StandEntity standEntity, IStandPower standPower) {
        if (standSoundSupplier == null) {
            return null;
        }
        return standSoundSupplier.get();
    }
    
    protected void playSoundAtStand(World world, StandEntity standEntity, SoundEvent sound, IStandPower standPower, Phase phase) {
        if (world.isClientSide()) {
            if (isCancelable(standPower, standEntity, phase, null)) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, sound, this, 1.0F, 1.0F);
            }
            else {
                standEntity.playSound(sound, 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    @Nullable
    public StandRelativeOffset getOffsetFromUser(boolean armsOnlyMode) {
        return armsOnlyMode ? userOffsetArmsOnly : userOffset;
    }
    
    protected boolean standTakesCrosshairTarget(ActionTarget target) {
        if (getTargetRequirement() != null && !getTargetRequirement().checkTargetType(TargetType.EMPTY)
                && getTargetRequirement().checkTargetType(target.getType())) {
            return true;
        }
        if (crosshairTargetForStand != null) {
            return crosshairTargetForStand.checkTargetType(target.getType());
        }
        return false;
    }
    
    protected final void invokeForStand(IStandPower power, Consumer<StandEntity> consumer) {
        if (power.isActive()) {
            consumer.accept(((StandEntity) power.getStandManifestation()));
        }
    }
    
    public boolean isCancelable(IStandPower standPower, StandEntity standEntity, Phase phase, @Nullable StandEntityAction newAction) {
        return phase != Phase.RECOVERY && (isCancelable || getHoldDurationMax(standPower) > 0);
    }
    
    public boolean isCombatAction() {
        return false;
    }
    
    public void onClear(IStandPower standPower, StandEntity standEntity) {}
    
    public float getStandAlpha(StandEntity standEntity, int ticksLeft, float partialTick) {
        return 1F;
    }
    
    @Override
    protected ActionTarget aim(World world, LivingEntity user, IStandPower power, double range) {
        LivingEntity aimingEntity = user;
        if (power.isActive()) {
            IStandManifestation stand = power.getStandManifestation();
            if (stand instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) stand;
                if (standEntity.isManuallyControlled()) {
                    aimingEntity = standEntity;
                }
            }
        }
        return super.aim(world, aimingEntity, power, range);
    }
    
    public enum Phase {
        BUTTON_HOLD,
        WINDUP,
        PERFORM,
        RECOVERY
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityAction.Builder> {

        @Override
        protected StandEntityAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends StandEntityAction.AbstractBuilder<T>> extends StandAction.AbstractBuilder<T> {
        private float staminaCost;
        private int standWindupDuration = 0;
        private int standPerformDuration = 1;
        private int standRecoveryDuration = 0;
        private AutoSummonMode autoSummonMode = AutoSummonMode.FULL;
        private TargetRequirement crosshairTargetForStand = null;
        private boolean isCancelable = false;
        private float userMovementFactor = 0.5F;
        private StandPose standPose = StandPose.NONE;
        @Nullable
        private StandRelativeOffset userOffset = null;
        @Nullable
        private StandRelativeOffset userOffsetArmsOnly = null;
        private boolean enablePhysics = true;
        private Supplier<SoundEvent> standSoundSupplier = () -> null;

        public T staminaCost(float staminaCost) {
            this.staminaCost = staminaCost;
            return getThis();
        }

        @Deprecated
        @Override
        public T autoSummonStand() {
            return standAutoSummonMode(AutoSummonMode.FULL);
        }
        
        public T standAutoSummonMode(AutoSummonMode mode) {
            if (mode != null) {
                this.autoSummonMode = mode;
            }
            return getThis();
        }
        
        public T standWindupPerformDuration(int windupTicks, int performTicks) {
            this.standWindupDuration = Math.max(windupTicks, 0);
            this.standPerformDuration = Math.max(performTicks, 1);
            return getThis();
        }
        
        public T standWindupDuration(int ticks) {
            return standWindupPerformDuration(ticks, 1);
        }
        
        public T standPerformDuration(int ticks) {
            return standWindupPerformDuration(0, ticks);
        }
        
        public T standRecoveryTicks(int ticks) {
            this.standRecoveryDuration = Math.max(ticks, 0);
            return getThis();
        }
        
        public T standTakesCrosshairTarget() {
            this.crosshairTargetForStand = TargetRequirement.ANY;
            return getThis();
        }
        
        public T standTakesCrosshairTarget(TargetType targetType) {
            switch (targetType) {
            case BLOCK:
                this.crosshairTargetForStand = TargetRequirement.BLOCK;
                break;
            case ENTITY:
                this.crosshairTargetForStand = TargetRequirement.ENTITY;
                break;
            default:
                this.crosshairTargetForStand = null;
                break;
            }
            return getThis();
        }
        
        public T isCancelable() {
            this.isCancelable = true;
            return getThis();
        }
        
        public T standUserSlowDownFactor(float factor) {
            this.userMovementFactor = MathHelper.clamp(factor, 0F, 1F);
            return getThis();
        }
        
        public T standPose(StandPose pose) {
            if (pose != null) {
                this.standPose = pose;
            }
            return getThis();
        }
        
        public T defaultStandOffsetFromUser() {
            this.userOffset = new StandRelativeOffset(0, 0.5);
            return getThis();
        }
        
        public T standOffsetFromUser(double left, double forward) {
            return standOffsetFromUser(left, forward, false);
        }
        
        public T standOffsetFromUser(double left, double forward, double y) {
            return standOffsetFromUser(left, forward, y, false);
        }
        
        public T standOffsetFromUser(double left, double forward, boolean armsOnlyMode) {
            setStandOffset(new StandRelativeOffset(left, forward), armsOnlyMode);
            return getThis();
        }
        
        public T standOffsetFromUser(double left, double forward, double y, boolean armsOnlyMode) {
            setStandOffset(new StandRelativeOffset(left, forward, y), armsOnlyMode);
            return getThis();
        }
        
        private void setStandOffset(StandRelativeOffset offset, boolean armsOnlyMode) {
            if (armsOnlyMode) {
                userOffsetArmsOnly = offset;
            }
            else {
                userOffset = offset;
            }
        }
        
        public T stayInNoPhysics() {
            this.enablePhysics = false;
            return getThis();
        }
        
        public T standSound(Supplier<SoundEvent> soundSupplier) {
            this.standSoundSupplier = soundSupplier;
            return getThis();
        }
    }
    
    public enum AutoSummonMode {
        FULL,
        ARMS,
        ONE_ARM,
        DISABLED
    }
}
