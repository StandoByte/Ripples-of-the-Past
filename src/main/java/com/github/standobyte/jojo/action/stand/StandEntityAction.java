package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack.HeavyPunchInstance;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrBarrageHitSoundPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class StandEntityAction extends StandAction implements IStandPhasedAction {
    protected final int standWindupDuration;
    protected final int standPerformDuration;
    protected final int standRecoveryDuration;
    private final AutoSummonMode autoSummonMode;
    private final float userWalkSpeed;
    private final StandPose standPose;
    @Nullable
    protected final StandRelativeOffset userOffset;
    @Nullable
    protected final StandRelativeOffset userOffsetArmsOnly;
    public final boolean enablePhysics;
    protected final Map<Phase, List<StandSound>> standSounds;
    protected final Supplier<StandEntityMeleeBarrage> barrageVisuals;
    protected boolean friendlyFire = false;
    
    public StandEntityAction(StandEntityAction.AbstractBuilder<?> builder) {
        super(builder);
        this.standWindupDuration = builder.standWindupDuration;
        this.standPerformDuration = builder.standPerformDuration;
        this.standRecoveryDuration = builder.standRecoveryDuration;
        this.autoSummonMode = builder.autoSummonMode;
        this.userWalkSpeed = builder.userWalkSpeed;
        this.standPose = builder.standPose;
        this.userOffset = builder.userOffset;
        this.userOffsetArmsOnly = builder.userOffsetArmsOnly;
        this.enablePhysics = builder.enablePhysics;
        this.standSounds = builder.standSounds;
        this.barrageVisuals = builder.barrageVisuals;
    }

    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return standWindupDuration;
    }

    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return standPerformDuration;
    }

    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return standRecoveryDuration;
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
                    return ActionConditionResult.NEGATIVE_QUEUEABLE;
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
        if (target.getType() != TargetType.EMPTY) {
            if (getTargetRequirement() != TargetRequirement.NONE && getTargetRequirement().checkTargetType(target.getType())) {
                return ActionConditionResult.POSITIVE;
            }
            return ActionConditionResult.noMessage(standKeepsTarget(target));
        }
        return super.checkTarget(target, user, power);
    }
    
    protected boolean standKeepsTarget(ActionTarget target) {
        return false;
    }
    
    public ActionConditionResult checkStandTarget(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
        switch (target.getType()) {
        case ENTITY:
            Entity targetEntity = target.getEntity();
            return ActionConditionResult.noMessage(targetEntity instanceof LivingEntity && canStandTargetEntity(standEntity, (LivingEntity) targetEntity, standPower));
        default:
            return ActionConditionResult.POSITIVE;
        }
    }
    
    protected boolean canStandTargetEntity(StandEntity standEntity, LivingEntity target, IStandPower power) {
        return friendlyFire ? true : standEntity.canAttack(target);
    }
    
    @Override
    public void onClick(World world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide()) {
            if (!power.isActive()) {
                switch (getAutoSummonMode(power, user)) {
                case FULL:
                    ((EntityStandType<?>) power.getType()).summon(user, power, entity -> {}, true, false);
                    break;
                case ARMS:
                    ((EntityStandType<?>) power.getType()).summon(user, power, entity -> entity.setArmsOnlyMode(), true, false);
                    break;
                case MAIN_ARM:
                    ((EntityStandType<?>) power.getType()).summon(user, power, entity -> entity.setArmsOnlyMode(true, false), true, false);
                    break;
                case OFF_ARM:
                    ((EntityStandType<?>) power.getType()).summon(user, power, entity -> entity.setArmsOnlyMode(false, true), true, false);
                    break;
                default:
                    break;
                }
            }
            else {
                StandEntity stand = (StandEntity) power.getStandManifestation();
                if (stand.isArmsOnlyMode()) {
                    switch (getAutoSummonMode(power, user)) {
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
    public void afterClick(World world, LivingEntity user, IStandPower power, boolean passedRequirements) {
        if (!world.isClientSide() && power.isActive()) {
            StandEntity standEntity = (StandEntity) power.getStandManifestation();
            if (!standEntity.isAddedToWorld()) {
                ((EntityStandType<?>) power.getType()).finalizeStandSummonFromAction(user, power, standEntity, passedRequirements);
            }
        }
    }
    
    @Override
    protected void consumeStamina(World world, IStandPower power) {} // consumed from StandEntity's task instead
    
    @Override
    public void startedHolding(World world, LivingEntity user, IStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            invokeForStand(power, stand -> {
                preTaskInit(world, power, stand, target);
                if (!world.isClientSide()) {
                    setAction(power, stand, 
                        holdOnly(power) || continueHolding  ? Integer.MAX_VALUE : getHoldDurationToFire(power), 
                        holdOnly(power) && !continueHolding ? Phase.PERFORM : Phase.BUTTON_HOLD, 
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
    protected
    final void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
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
    
    protected AutoSummonMode getAutoSummonMode(IStandPower standPower, LivingEntity user) {
        return autoSummonMode;
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
    
    public void taskWriteAdditional(StandEntityTask task, PacketBuffer buffer) {}
    public void taskReadAdditional(StandEntityTask task, PacketBuffer buffer) {}
    public void taskCopyAdditional(StandEntityTask task, StandEntityTask sourceTask) {}
    
    public void playSound(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        Stream<SoundEvent> sounds = getSounds(standEntity, standPower, phase, task);
        if (sounds != null) {
            sounds.forEach(sound -> {
                if (sound != null) {
                    playSoundAtStand(standEntity.level, standEntity, sound, standPower, phase);
                }
            });
        }
    }
    
    @Nullable
    public Stream<SoundEvent> getSounds(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        if (barrageVisuals(standEntity, standPower, task)) {
            return barrageVisuals.get().getSounds(standEntity, standPower, phase, task);
        }
        
        return getPhaseStandSounds(phase, standEntity);
    }
    
    protected final Stream<SoundEvent> getPhaseStandSounds(Phase phase, StandEntity standEntity) {
        boolean armsOnly = standEntity.isArmsOnlyMode();
        return Optional.ofNullable(standSounds.get(phase))
                .map(list -> list.stream()
                        .filter(sound -> sound.playInArmsOnly || !armsOnly)
                        .map(sound -> sound.sound.get()))
                .orElse(null);
    }
    
    protected void playSoundAtStand(World world, StandEntity standEntity, SoundEvent sound, IStandPower standPower, Phase phase) {
        if (world.isClientSide()) {
            if (canBeCanceled(standPower, standEntity, phase, null)) {
                ClientTickingSoundsHelper.playStandEntityCancelableActionSound(standEntity, sound, this, phase, 1.0F, 1.0F, false);
            }
            else {
                standEntity.playSound(sound, 1.0F, 1.0F, ClientUtil.getClientPlayer());
            }
        }
    }
    
    
    protected boolean barrageVisuals(StandEntity standEntity, IStandPower standPower, StandEntityTask task) {
        return barrageVisuals.get() != null;
    }
    
    public void barrageVisualsPhaseTransition(World world, StandEntity standEntity, IStandPower standPower, @Nullable Phase to, StandEntityTask task) {
        if (world.isClientSide()) {
            standEntity.getBarrageHitSoundsHandler().setIsBarraging(to == Phase.PERFORM && barrageVisuals(standEntity, standPower, task));
        }
    }
    
    protected void barrageVisualsTick(StandEntity stand, boolean playSound, Vector3d soundPos) {
        if (!stand.level.isClientSide()) {
            SoundEvent hitSound = barrageVisuals.get() != null ? barrageVisuals.get().getHitSound() : null;
            if (hitSound != null) {
                PacketManager.sendToClientsTracking(playSound && soundPos != null ? 
                        new TrBarrageHitSoundPacket(stand.getId(), hitSound, soundPos)
                        : TrBarrageHitSoundPacket.noSound(stand.getId()), stand);
            }
        }
    }
    
    public final void taskStopped(World world, StandEntity standEntity, IStandPower standPower, StandEntityTask task, @Nullable StandEntityAction newAction) {
        barrageVisualsPhaseTransition(world, standEntity, standPower, null, task);
        onTaskStopped(world, standEntity, standPower, task, newAction);
    }
    
    protected void onTaskStopped(World world, StandEntity standEntity, IStandPower standPower, StandEntityTask task, @Nullable StandEntityAction newAction) {}
    
    @Nullable
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return standEntity.isArmsOnlyMode() ? userOffsetArmsOnly : userOffset;
    }
    
    public float yRotForOffset(LivingEntity user, StandEntityTask task) {
        return user.yRot;
    }
    
    public void rotateStand(StandEntity standEntity, StandEntityTask task) {
        if (standEntity.isManuallyControlled() || !noAdheringToUserOffset(standEntity.getUserPower(), standEntity)) {
            standEntity.defaultRotation();
        }
    }
    
    protected Optional<StandRelativeOffset> offsetToTarget(IStandPower standPower, StandEntity standEntity, StandEntityTask task, 
            double minOffset, double maxOffset, @Nullable Supplier<ActionTarget> noTaskTarget) {
        if (standEntity.isArmsOnlyMode()) {
            return Optional.empty();
        }
        LivingEntity user = standEntity.getUser();
        ActionTarget target = task.getTarget();
        
        if (target.getType() == TargetType.EMPTY && noTaskTarget != null) {
            target = noTaskTarget.get();
        }
        
        Vector3d targetPos = target.getTargetPos(true);
        if (targetPos == null) {
            return Optional.empty();
        }
        else {
            double backAway = 1.0 + (target.getType() == TargetType.ENTITY ? 
                    target.getEntity().getBoundingBox().getXsize() / 2
                    : 0.5);
            double offsetToTarget = targetPos.subtract(user.position()).multiply(1, 0, 1).length() - backAway;
            return Optional.of(StandRelativeOffset.withXRot(0, MathHelper.clamp(offsetToTarget, minOffset, maxOffset)));
        }
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
    
    public boolean stopOnHeavyAttack(HeavyPunchInstance punch) {
        return false;
    }
    
    @Override
    public boolean heldAllowsOtherAction(IStandPower standPower, Action<IStandPower> action) {
        return getHoldDurationToFire(standPower) == 0;
    }
    
    public boolean noFinisherDecay() {
        return false;
    }
    
    public boolean canFollowUpBarrage() {
        return false;
    }
    
    public float getStandAlpha(StandEntity standEntity, int ticksLeft, float partialTick) {
        return 1F;
    }
    
    public float getUserWalkSpeed(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return task.getPhase() == Phase.RECOVERY ? 1F : userWalkSpeed;
    }
    
    public StandPose getStandPose(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        if (barrageVisuals(standEntity, standPower, task)) {
            return barrageVisuals.get().getStandPose(standPower, standEntity, task);
        }
        return standPose;
    }
    
    public void rotateStandTowardsTarget(StandEntity standEntity, ActionTarget target, StandEntityTask task) {
        Vector3d targetPos = target.getTargetPos(true);
        if (targetPos != null) {
            MCUtil.rotateTowards(standEntity, targetPos, 360F);
        }
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
        protected int standWindupDuration = 0;
        protected int standPerformDuration = 1;
        protected int standRecoveryDuration = 0;
        protected AutoSummonMode autoSummonMode = AutoSummonMode.FULL;
        protected float userWalkSpeed = 0.5F;
        protected StandPose standPose = StandPose.IDLE;
        @Nullable
        protected StandRelativeOffset userOffset = null;
        @Nullable
        protected StandRelativeOffset userOffsetArmsOnly = null;
        protected boolean enablePhysics = true;
        protected final Map<Phase, List<StandSound>> standSounds = new EnumMap<>(Phase.class);
        protected Supplier<StandEntityMeleeBarrage> barrageVisuals = () -> null;

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
        
        public T standUserWalkSpeed(float factor) {
            this.userWalkSpeed = MathHelper.clamp(factor, 0F, 1F);
            return getThis();
        }
        
        public T standPose(StandPose pose) {
            if (pose != null) {
                this.standPose = pose;
            }
            return getThis();
        }

        public T standOffsetFront() {
            // FIXME barrage-like offset
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

        public T standOffsetFromUser(double left, double forward, double y, boolean armsOnlyMode) {
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
        
        @SafeVarargs
        public final T standSound(Supplier<SoundEvent>... soundSuppliers) {
            return standSound(Phase.PERFORM, soundSuppliers);
        }

        @SafeVarargs
        public final T standSound(Phase phase, Supplier<SoundEvent>... soundSuppliers) {
            return standSound(phase, true, soundSuppliers);
        }

        @SafeVarargs
        public final T standSound(Phase phase, boolean playInArmsOnly, Supplier<SoundEvent>... soundSuppliers) {
            if (phase != null) {
                Arrays.stream(soundSuppliers)
                .map(sound -> new StandSound(sound, playInArmsOnly))
                .collect(Collectors.toCollection(() -> standSounds.computeIfAbsent(phase, p -> new ArrayList<>())));
            }
            return getThis();
        }
        
        public T barrageVisuals(Supplier<StandEntityMeleeBarrage> barrageAttack) {
            this.barrageVisuals = barrageAttack != null ? barrageAttack : () -> null;
            return getThis();
        }
    }
    
    protected static class StandSound {
        public final Supplier<SoundEvent> sound;
        public final boolean playInArmsOnly;
        
        public StandSound(Supplier<SoundEvent> sound, boolean playInArmsOnly) {
            this.sound = sound;
            this.playInArmsOnly = playInArmsOnly;
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
