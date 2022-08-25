package com.github.standobyte.jojo.action.stand;

import java.util.HashMap;
import java.util.Map;
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
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class StandEntityAction extends StandAction {
    protected final int standWindupDuration;
    protected final int standPerformDuration;
    protected final int standRecoveryDuration;
    private final AutoSummonMode autoSummonMode;
    private final float userMovementFactor;
    private final StandPose standPose;
    @Nullable
    protected final StandRelativeOffset userOffset;
    @Nullable
    protected final StandRelativeOffset userOffsetArmsOnly;
    public final boolean enablePhysics;
    private final Map<Phase, Supplier<SoundEvent>> standSounds;
    
    public StandEntityAction(StandEntityAction.AbstractBuilder<?> builder) {
        super(builder);
        this.standWindupDuration = builder.standWindupDuration;
        this.standPerformDuration = builder.standPerformDuration;
        this.standRecoveryDuration = builder.standRecoveryDuration;
        this.autoSummonMode = builder.autoSummonMode;
        this.userMovementFactor = builder.userMovementFactor;
        this.standPose = builder.standPose;
        this.userOffset = builder.userOffset;
        this.userOffsetArmsOnly = builder.userOffsetArmsOnly;
        this.enablePhysics = builder.enablePhysics;
        this.standSounds = builder.standSounds;
    }
    
    public void standTickButtonHold(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    public void standTickWindup(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    public boolean standCanTick(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) { return true; }
    
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    public boolean standCanPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) { return true; }
    
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
    public void standTickRecovery(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {}
    
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
    public LivingEntity getPerformer(LivingEntity user, IStandPower power) {
        return power.isActive() ? (StandEntity) power.getStandManifestation() : user;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, IStandPower power, ActionTarget target) {
    	StandEntity stand = power.isActive() ? (StandEntity) power.getStandManifestation() : null;
        if (stand != null) {
            ActionConditionResult checkStand = checkStandConditions(stand, power, target);
            if (!checkStand.isPositive()) {
                return checkStand;
            }
        }
        
        ActionConditionResult checkGeneral = super.checkConditions(user, power, target);
        if (!checkGeneral.isPositive()) {
            return checkGeneral;
        }
        
        if (stand != null) {
            ActionConditionResult checkTask = checkTaskCancelling(stand, power);
            if (!checkTask.isPositive()) {
                return checkTask;
            }
        }

        return ActionConditionResult.POSITIVE;
    }
    
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return ActionConditionResult.POSITIVE;
    }
    
    private ActionConditionResult checkTaskCancelling(StandEntity standEntity, IStandPower standPower) {
    	if (standEntity.getCurrentTask().isPresent() && standPower.getHeldAction() != this) {
    		StandEntityTask task = standEntity.getCurrentTask().get();
    		if (!task.getAction().canClickDuringTask(this, standPower, standEntity, task)) {
    			return ActionConditionResult.NEGATIVE;
    		}
    		if (!task.getAction().canBeCanceled(standPower, standEntity, task.getPhase(), this)) {
    			if (this.canBeQueued(standPower, standEntity)) {
    				if (!standEntity.level.isClientSide()) {
    					standEntity.queueNextAction(this);
    				}
    				return ActionConditionResult.NEGATIVE_HIGHLIGHTED;
    			}
            	return ActionConditionResult.NEGATIVE;
    		}
    	}
    	return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public ActionConditionResult checkRangeAndTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        ActionConditionResult result = super.checkRangeAndTarget(target, user, power);
        if (result.isPositive() && power.isActive()) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            return checkStandTarget(target, stand, power);
        }
        return result;
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        if (target.getType() == TargetType.BLOCK) {
            return ActionConditionResult.noMessage(getTargetRequirement() != TargetRequirement.NONE && getTargetRequirement().checkTargetType(TargetType.BLOCK));
        }
        return super.checkTarget(target, user, power);
    }
    
    public ActionConditionResult checkStandTarget(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
        switch (target.getType()) {
        case ENTITY:
            Entity targetEntity = target.getEntity();
            return ActionConditionResult.noMessage(targetEntity instanceof LivingEntity && standEntity.canAttack((LivingEntity) targetEntity));
        default:
            return ActionConditionResult.POSITIVE;
        }
    }
    
    @Override
    public void onClick(World world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide()) {
            if (!power.isActive()) {
                // FIXME !!!!!!!! only summon in arms-only mode if the task can actually be set
                switch (autoSummonMode) {
                case FULL:
                    power.getType().summon(user, power, true);
                    break;
                case ARMS:
                    ((EntityStandType<?>) power.getType()).summon(user, power, entity -> entity.setArmsOnlyMode(), true);
                    break;
                case MAIN_ARM:
                    ((EntityStandType<?>) power.getType()).summon(user, power, entity -> entity.setArmsOnlyMode(true, false), true);
                    break;
                case OFF_ARM:
                    ((EntityStandType<?>) power.getType()).summon(user, power, entity -> entity.setArmsOnlyMode(false, true), true);
                    break;
                default:
                    break;
                }
            }
            else {
                StandEntity stand = (StandEntity) power.getStandManifestation();
                if (stand.isArmsOnlyMode()) {
                    switch (autoSummonMode) {
                    case ARMS:
                        stand.setArmsOnlyMode();
                        break;
                    case MAIN_ARM:
                        stand.addToArmsOnly(Hand.MAIN_HAND);
                        break;
                    case OFF_ARM:
                        stand.addToArmsOnly(Hand.OFF_HAND);
                        break;
                    case FULL:
                        stand.fullSummonFromArms();
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public boolean staminaConsumedDifferently(IStandPower power) {
        return true;
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, IStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            invokeForStand(power, stand -> {
	    		preTaskInit(world, power, stand, target);
	    		if (!world.isClientSide()) {
		    		setAction(power, stand, 
	                    !holdOnly() ? getHoldDurationToFire(power) : getHoldDurationMax(power), 
	                    !holdOnly() ? Phase.BUTTON_HOLD : Phase.PERFORM, 
	                    target);
	    		}
            });
        }
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, IStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {}

    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }

    @Override
    public void stoppedHolding(World world, LivingEntity user, IStandPower power, int ticksHeld, boolean willFire) {
    	if (!willFire) {
	        invokeForStand(power, stand -> {
	            if (stand.getCurrentTaskAction() == this) {
	                stand.stopTaskWithRecovery();
	            }
	        });
    	}
    }

    @Override
    protected final void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
    	invokeForStand(power, stand -> {
    		if (stand.getCurrentTask().map(task -> {
    			if (task.getPhase() == Phase.BUTTON_HOLD) {
    				task.moveToPhase(Phase.WINDUP, power, stand);
    				return false;
    			}
    			return true;
    		}).orElse(true)) {
	    		preTaskInit(world, power, stand, target);
	    		if (!world.isClientSide()) {
	    			int windupTicks = getStandWindupTicks(power, stand);
	    			int ticks = windupTicks > 0 ? windupTicks : getStandActionTicks(power, stand);
	    			Phase phase = windupTicks > 0 ? Phase.WINDUP : Phase.PERFORM;
	    			setAction(power, stand, ticks, phase, target);
	    		}
    		}
    	});
    }
    
    protected void preTaskInit(World world, IStandPower standPower, StandEntity standEntity, ActionTarget target) {}
    
    protected boolean allowArmsOnly() {
        return autoSummonMode == AutoSummonMode.ARMS || autoSummonMode == AutoSummonMode.MAIN_ARM || autoSummonMode == AutoSummonMode.OFF_ARM;
    }
    
    protected void setAction(IStandPower standPower, StandEntity standEntity, int ticks, Phase phase, ActionTarget target) {
        standEntity.setTask(this, ticks, phase, target);
    }
    
    public boolean canStaminaRegen(IStandPower standPower, StandEntity standEntity) {
        return false;
    }
    
    public boolean noAdheringToUserOffset(IStandPower standPower, StandEntity standEntity) {
        return standMovesByItself(standPower, standEntity);
    }
    
    public boolean lockStandManualMovement(IStandPower standPower, StandEntity standEntity) {
        return standMovesByItself(standPower, standEntity);
    }
    
    protected boolean standMovesByItself(IStandPower standPower, StandEntity standEntity) {
        return false;
    }
    
    public boolean standRetractsAfterTask(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {}
    
    public void onPhaseSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {}
    
    public void playSound(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        SoundEvent sound = getSound(standEntity, standPower, phase, task);
        if (sound != null) {
            playSoundAtStand(standEntity.level, standEntity, sound, standPower, phase);
        }
    }
    
    @Nullable
    public SoundEvent getSound(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        Supplier<SoundEvent> standSoundSupplier = standSounds.get(phase);
        if (standSoundSupplier == null) {
            return null;
        }
        return standSoundSupplier.get();
    }
    
    protected void playSoundAtStand(World world, StandEntity standEntity, SoundEvent sound, IStandPower standPower, Phase phase) {
        if (world.isClientSide()) {
            if (canBeCanceled(standPower, standEntity, phase, null)) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, sound, this, phase, 1.0F, 1.0F);
            }
            else {
                standEntity.playSound(sound, 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    @Nullable
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, ActionTarget aimTarget) {
        return standEntity.isArmsOnlyMode() ? userOffsetArmsOnly : userOffset;
    }
    
    public boolean transfersPreviousOffset(IStandPower standPower, StandEntity standEntity, StandEntityTask previousTask) {
    	return true;
    }
    
    protected final void invokeForStand(IStandPower power, Consumer<StandEntity> consumer) {
        if (power.isActive()) {
            consumer.accept(((StandEntity) power.getStandManifestation()));
        }
    }
    
    protected boolean canBeQueued(IStandPower standPower, StandEntity standEntity) {
        return getHoldDurationMax(standPower) == 0;
    }
    
    protected boolean canClickDuringTask(StandEntityAction clickedAction, IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return clickedAction != this || isChainable(standPower, standEntity) || isChainable(standPower, standEntity);
    }
    
    protected boolean isChainable(IStandPower standPower, StandEntity standEntity) {
    	return false;
    }
    
    protected boolean isFreeRecovery(IStandPower standPower, StandEntity standEntity) {
    	return false;
    }
    
    public final boolean canBeCanceled(IStandPower standPower, StandEntity standEntity, Phase phase, @Nullable StandEntityAction newAction) {
    	return isCancelable(standPower, standEntity, newAction, phase)
    			|| newAction != null && newAction.cancels(this, standPower, standEntity, phase)
    			|| phase == Phase.RECOVERY && (newAction == this && isChainable(standPower, standEntity)
    			|| isFreeRecovery(standPower, standEntity));
    }
    
    protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
        return getHoldDurationMax(standPower) > 0 && phase != Phase.RECOVERY
                && (newAction == null || getStandRecoveryTicks(standPower, standEntity) == 0);
    }
    
    protected boolean cancels(StandEntityAction currentAction, IStandPower standPower, StandEntity standEntity, Phase currentPhase) {
        return false;
    }
    
    @Override
    public boolean heldAllowsOtherActions(IStandPower standPower) {
        return getHoldDurationToFire(standPower) == 0;
    }
    
    public boolean noComboDecay() {
        return false;
    }
    
    public boolean canFollowUpBarrage() {
        return false;
    }
    
    public void onClear(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction) {
    }
    
    public float getStandAlpha(StandEntity standEntity, int ticksLeft, float partialTick) {
        return 1F;
    }
    
    public float getUserMovementFactor(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return task.getPhase() == Phase.RECOVERY && isFreeRecovery(standPower, standEntity) ? 1F : userMovementFactor;
    }
    
    public StandPose getStandPose(IStandPower standPower, StandEntity standEntity) {
        return standPose;
    }
    
    public enum Phase {
        BUTTON_HOLD,
        WINDUP,
        PERFORM,
        RECOVERY;
        
        @Nullable
        public Phase getNextPhase() {
            int num = ordinal() + 1;
            if (num == values().length) {
                return null;
            }
            return values()[num];
        }
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityAction.Builder> {

        @Override
        protected StandEntityAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends StandEntityAction.AbstractBuilder<T>> extends StandAction.AbstractBuilder<T> {
        private int standWindupDuration = 0;
        private int standPerformDuration = 1;
        private int standRecoveryDuration = 0;
        private AutoSummonMode autoSummonMode = AutoSummonMode.FULL;
        private float userMovementFactor = 0.5F;
        private StandPose standPose = StandPose.IDLE;
        @Nullable
        private StandRelativeOffset userOffset = null;
        @Nullable
        private StandRelativeOffset userOffsetArmsOnly = null;
        private boolean enablePhysics = true;
        private final Map<Phase, Supplier<SoundEvent>> standSounds = new HashMap<>();

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
        
        public T standWindupDuration(int ticks) {
            this.standWindupDuration = Math.max(ticks, 0);
            return getThis();
        }
        
        public T standPerformDuration(int ticks) {
            this.standPerformDuration = Math.max(ticks, 1);
            return getThis();
        }
        
        public T standRecoveryTicks(int ticks) {
            this.standRecoveryDuration = Math.max(ticks, 0);
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

        public T standOffsetFront() {
        	// FIXME (!) barrage-like offset
        	setStandOffset(StandRelativeOffset.noYOffset(0, 0.5), false);
            return getThis();
        }

        public T standOffsetFromUser(double left, double forward) {
            return standOffsetFromUser(left, forward, false);
        }

        public T standOffsetFromUser(double left, double forward, double y) {
            return standOffsetFromUser(left, forward, y, false);
        }

        public T standOffsetFromUser(double left, double forward, boolean armsOnlyMode) {
            setStandOffset(StandRelativeOffset.noYOffset(left, forward), armsOnlyMode);
            return getThis();
        }

        public T standOffsetFromUser(double left, double y, double forward, boolean armsOnlyMode) {
            setStandOffset(StandRelativeOffset.withYOffset(left, y, forward), armsOnlyMode);
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
            return standSound(Phase.PERFORM, soundSupplier);
        }
        
        public T standSound(Phase phase, Supplier<SoundEvent> soundSupplier) {
            if (phase != null) {
                this.standSounds.put(phase, soundSupplier);
            }
            return getThis();
        }
    }
    
    public enum AutoSummonMode {
        FULL,
        ARMS,
        MAIN_ARM,
        OFF_ARM,
        DISABLED
    }
}
