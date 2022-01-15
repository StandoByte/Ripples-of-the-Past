package com.github.standobyte.jojo.action.actions;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class StandEntityAction extends StandAction {
    protected final int standWindupDuration;
    protected final int standPerformDuration;
    protected final int standRecoveryDuration;
    protected final AutoSummonMode autoSummonMode;
    protected final boolean standTakesCrosshairTarget;
    private final boolean isCancelable;
    public final float userMovementFactor;
    public final StandPose standPose;
    protected final UserOffset userOffset;
    protected final UserOffset userOffsetArmsOnly;
    private final Supplier<SoundEvent> standSoundSupplier;
    
    public StandEntityAction(StandEntityAction.AbstractBuilder<?> builder) {
        super(builder);
        this.standWindupDuration = builder.standWindupDuration;
        this.standPerformDuration = builder.standPerformDuration;
        this.standRecoveryDuration = builder.standRecoveryDuration;
        this.autoSummonMode = builder.autoSummonMode;
        this.standTakesCrosshairTarget = builder.standTakesCrosshairTarget;
        this.isCancelable = builder.isCancelable;
        this.userMovementFactor = builder.userMovementFactor;
        this.standPose = builder.standPose;
        this.userOffset = builder.userOffset;
        this.userOffsetArmsOnly = builder.userOffsetArmsOnly;
        this.standSoundSupplier = builder.standSoundSupplier;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        if (power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            StandEntityAction currentAction = stand.getCurrentTaskAction();
            if (currentAction != null && !currentAction.isCancelable(power, stand)) {
                return ActionConditionResult.NEGATIVE;
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public LivingEntity getPerformer(LivingEntity user, IStandPower power) {
        return power.isActive() ? (StandEntity) power.getStandManifestation() : user;
    }
    
    public void standTickButtonHold(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {}
    
    public void standTickWindup(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {}
    
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {}
    
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {}
    
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
    public final void startedHolding(World world, LivingEntity user, IStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            invokeForStand(power, stand -> setAction(power, stand, 
                    !holdOnly() ? getHoldDurationToFire(power) : getHoldDurationMax(power), 
                    !holdOnly() ? Phase.BUTTON_HOLD : Phase.PERFORM, 
                    target));
        }
    }
    
    @Override
    protected final void holdTick(World world, LivingEntity user, IStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {}
    
    @Override
    public final void stoppedHolding(World world, LivingEntity user, IStandPower power, int ticksHeld) {
        if (!world.isClientSide()) {
            invokeForStand(power, stand -> stand.stopTask());
        }
    }

    @Override
    protected final void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            invokeForStand(power, stand -> {
                int windupTicks = getStandWindupTicks(power, stand, stand.getAttackSpeed());
                int ticks = windupTicks > 0 ? windupTicks : getStandActionTicks(power, stand);
                Phase phase = windupTicks > 0 ? Phase.WINDUP : Phase.PERFORM;
                setAction(power, stand, ticks, phase, target);
            });
        }
    }
    
    private void setAction(IStandPower standPower, StandEntity standEntity, int ticks, Phase phase, ActionTarget target) {
        if (standEntity.setTask(this, ticks, phase)) {
            if (standTakesCrosshairTarget) {
                standEntity.setTaskTarget(target);
            }
            setRelativePos(standEntity);
        }
    }
    
    public void playSound(StandEntity standEntity, IStandPower standPower) {
        SoundEvent sound = getSound(standEntity, standPower);
        if (sound != null) {
            playSoundAtStand(standEntity.level, standEntity, sound, standPower);
        }
    }
    
    @Nullable
    protected SoundEvent getSound(StandEntity standEntity, IStandPower standPower) {
        if (standSoundSupplier == null) {
            return null;
        }
        return standSoundSupplier.get();
    }
    
    protected void playSoundAtStand(World world, StandEntity standEntity, SoundEvent sound, IStandPower standPower) {
        if (world.isClientSide()) {
            if (isCancelable(standPower, standEntity)) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, sound, this, 1.0F, 1.0F);
            }
            else {
                standEntity.playSound(sound, 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    private void setRelativePos(StandEntity stand) {
        UserOffset offset = stand.isArmsOnlyMode() ? userOffsetArmsOnly : userOffset;
        if (offset.offsetXZ) {
            stand.setRelativePos(offset.left, offset.forward);
        }
        if (offset.offsetY) {
            stand.setRelativeY(offset.y);
        }
    }
    
    protected final void invokeForStand(IStandPower power, Consumer<StandEntity> consumer) {
        if (power.isActive()) {
            consumer.accept(((StandEntity) power.getStandManifestation()));
        }
    }
    
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity, double attackSpeed) {
        return standWindupDuration;
    }
    
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return standPerformDuration;
    }
    
    protected int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity, double attackSpeed) {
        return standRecoveryDuration;
    }
    
    public boolean isCancelable(IStandPower standPower, StandEntity standEntity) {
        return isCancelable || getHoldDurationMax(standPower) > 0;
    }
    
    public void onClear(IStandPower standPower, StandEntity standEntity) {}
    
    public float getStandAlpha(StandEntity standEntity, int ticksLeft, float partialTick) {
        return 1F;
    }
    
    @Override
    protected ActionTarget aim(World world, LivingEntity user, IStandPower power, double range) { // FIXME (!!!) only used in Magician's Red's fireball shoot, so not a big deal if it needs to be moved
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
        PERFORM
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityAction.Builder> {

        @Override
        protected StandEntityAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends StandEntityAction.AbstractBuilder<T>> extends StandAction.AbstractBuilder<T> {
        protected int standWindupDuration = 0;
        protected int standPerformDuration = 1;
        protected int standRecoveryDuration = 0;
        protected AutoSummonMode autoSummonMode = AutoSummonMode.FULL;
        protected boolean standTakesCrosshairTarget = false;
        protected boolean isCancelable = false;
        protected float userMovementFactor = 1.0F;
        protected StandPose standPose = StandPose.NONE;
        protected final UserOffset userOffset = new UserOffset();
        protected final UserOffset userOffsetArmsOnly = new UserOffset();
        private Supplier<SoundEvent> standSoundSupplier = () -> null;
        
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
            this.standTakesCrosshairTarget = true;
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
            userOffset.offsetXZ(0, 0.5);
            userOffsetArmsOnly.offsetXZ(0, 0.15).offsetY(0);
            return getThis();
        }
        
        public T standOffsetFromUser(double left, double forward) {
            return standOffsetFromUser(left, forward, false);
        }
        
        public T standOffsetFromUser(double left, double forward, boolean armsOnlyMode) {
            UserOffset offset = armsOnlyMode ? userOffsetArmsOnly : userOffset;
            offset.offsetXZ(left, forward);
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
    
    private static class UserOffset {
        private boolean offsetXZ = false;
        private double left;
        private double forward;
        private boolean offsetY = false;
        private double y;
        
        private UserOffset offsetXZ(double left, double forward) {
            this.offsetXZ = true;
            this.left = left;
            this.forward = forward;
            return this;
        }
        
        private UserOffset offsetY(double y) {
            this.offsetY = true;
            this.y = y;
            return this;
        }
    }
}
