package com.github.standobyte.jojo.action.stand;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack.HeavyPunchInstance;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrDirectEntityPosPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.general.LazySupplier;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class TheWorldTSHeavyAttack extends StandEntityAction implements IHasStandPunch {
    public static final StandPose TS_PUNCH_POSE = new StandPose("TS_PUNCH");
    private final Supplier<TimeStopInstant> theWorldTimeStopBlink;
    
    @Deprecated
    public TheWorldTSHeavyAttack(StandEntityAction.Builder builder, 
            Supplier<StandEntityHeavyAttack> theWorldHeavyAttack, Supplier<TimeStopInstant> theWorldTimeStopBlink) {
        this(builder, theWorldTimeStopBlink);
    }

    public TheWorldTSHeavyAttack(StandEntityAction.Builder builder, Supplier<TimeStopInstant> theWorldTimeStopBlink) {
        super(builder);
        this.theWorldTimeStopBlink = theWorldTimeStopBlink;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (TimeStopHandler.isTimeStopped(user.level, user.blockPosition())) {
            return ActionConditionResult.NEGATIVE;
        }
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() || stand.isBeingRetracted()
                ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    @Override
    protected boolean canBeQueued(IStandPower standPower, StandEntity standEntity) {
        return false;
    }

    @Override
    public ActionTarget targetBeforePerform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
            StandEntity stand = (StandEntity) power.getStandManifestation();
            return ActionTarget.fromRayTraceResult(
                    stand.precisionRayTrace(stand.isManuallyControlled() ? stand : user, stand.getMaxRange(),
                            stand.getPrecision() / 16F));
        }
        return super.targetBeforePerform(world, user, power, target);
    }
    
    @Override
    protected void preTaskInit(World world, IStandPower standPower, StandEntity standEntity, ActionTarget target) {
        standEntity.summonLockTicks = 0;
        if (!world.isClientSide() || standEntity.isManuallyControlled()) {
            LivingEntity aimingEntity = standEntity.isManuallyControlled() ? standEntity : standPower.getUser();
            if (aimingEntity != null) {
                TimeStopInstant blink = theWorldTimeStopBlink.get();
                float staminaCostTS = blink.getStaminaCost(standPower) * 0.5F;
                float staminaCostTicking = blink.getStaminaCostTicking(standPower);
                TimeStop timeStop = blink.getBaseTimeStop();

                int timeStopTicks = TimeStop.getTimeStopTicks(standPower, timeStop);
                if (!StandUtil.standIgnoresStaminaDebuff(standPower) && blink != null && staminaCostTicking > 0) {
                    timeStopTicks = MathHelper.clamp(MathHelper.floor(
                            (standPower.getStamina() - staminaCostTS) / staminaCostTicking
                            ), 0, timeStopTicks);
                }

                int ticksForWindup = 10;
                if (standEntity.getCurrentTask().isPresent()) {
                    ticksForWindup += 20;
                }
                if (standEntity.getAttributeValue(Attributes.MOVEMENT_SPEED) > 0) {
                    Vector3d pos = target.getTargetPos(true);
                    if (pos != null) {
                        double offset = 0.5 + standEntity.getBbWidth();
                        if (target.getType() == TargetType.ENTITY) {
                            offset += target.getEntity().getBoundingBox().getXsize() / 2;
                        }
                        boolean backshot = doesBackshot(standPower);
                        Vector3d offsetFromTarget = aimingEntity.getEyePosition(1.0F).subtract(pos).normalize().scale(offset);
                        if (backshot) {
                            offsetFromTarget = offsetFromTarget.reverse();
                        }
                        pos = pos.add(offsetFromTarget);
                        pos = pos.subtract(0, standEntity.getEyeHeight(), 0);
                    }
                    else {
                        pos = aimingEntity.position().add(standEntity.getLookAngle().scale(standEntity.getMaxRange()));
                    }
                    
                    double ticksForDistance = pos.subtract(standEntity.position()).length() / TimeStopInstant.getDistancePerTick(standEntity);
                    
                    if (timeStopTicks < ticksForDistance + ticksForWindup) {
                        pos = timeStopTicks > ticksForWindup ? pos.subtract(standEntity.position()).scale((double) timeStopTicks - ticksForWindup / ticksForDistance).add(standEntity.position()) : standEntity.position();
                    }
                    else {
                        timeStopTicks = MathHelper.ceil(ticksForDistance) + ticksForWindup;
                    }
                    
                    pos = standEntity.collideNextPos(pos);
                    standEntity.moveTo(pos.x, pos.y, pos.z);
                    if (!world.isClientSide()) {
                        PacketManager.sendToClientsTracking(new TrDirectEntityPosPacket(standEntity.getId(), pos), standEntity);
                    }
                }
                else {
                    timeStopTicks = ticksForWindup;
                }

                TimeStopInstant.skipTicksForStandAndUser(standPower, timeStopTicks);
                if (!world.isClientSide()) {
                    MCUtil.playEitherSound(world, null, standEntity.getX(), standEntity.getY(), standEntity.getZ(), 
                            TimeStopHandler::canPlayerSeeInStoppedTime, blink.blinkSound.get(), ModSounds.THE_WORLD_TIME_STOP_UNREVEALED.get(), 
                            SoundCategory.AMBIENT, 1.0F, 1.0F);
                    standPower.consumeStamina(staminaCostTS + timeStopTicks * staminaCostTicking);
                    if (standPower.hasPower()) {
                        StandStats stats = standPower.getType().getStats();
                        if (stats instanceof TimeStopperStandStats) {
                            standPower.addLearningProgressPoints(theWorldTimeStopBlink.get().getBaseTimeStop(), 
                                    (int) (((TimeStopperStandStats) stats).timeStopLearningPerTick * timeStopTicks));
                        }
                    }
                }
            }
        }
    }
    
    private boolean doesBackshot(IStandPower standPower) {
        return JojoModUtil.useShiftVar(standPower.getUser());
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackRecovery(standEntity.getAttackSpeed(), 0);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        standEntity.punch(task, this, task.getTarget());
        userPower.getUser().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.hasUsedTimeStopToday = true);
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        double strength = stand.getAttackDamage();
        return new TheWorldTSHeavyPunch(stand, target, dmgSource)
                .setVoiceLineOnKill(ModSounds.DIO_THIS_IS_THE_WORLD)
                .damage(StandStatFormulas.getHeavyAttackDamage(strength))
                .addKnockback(4)
                .disableBlocking(1.0F)
                .setStandInvulTime(10)
                .impactSound(ModSounds.THE_WORLD_PUNCH_HEAVY);
    }
    
    @Override
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state) {
        return IHasStandPunch.super.punchBlock(stand, pos, state)
                .impactSound(ModSounds.THE_WORLD_PUNCH_HEAVY);
    }
    
    @Override
    public SoundEvent getPunchSwingSound() {
        return ModSounds.STAND_PUNCH_HEAVY_SWING.get();
    }
    
    @Override
    public void standTickWindup(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        IHasStandPunch.playPunchSwingSound(task, Phase.WINDUP, 3, this, standEntity);
    }
    
    @Override
    public void clPlayPunchSwingSound(StandEntity standEntity, SoundEvent sound) {
        standEntity.playSound(sound, 1.0F, 0.65F + standEntity.getRandom().nextFloat() * 0.2F, ClientUtil.getClientPlayer());
    }
    
    @Override
    public boolean noAdheringToUserOffset(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    @Override
    public boolean noFinisherDecay() {
        return true;
    }
    
    @Override
    protected boolean cancels(StandEntityAction currentAction, IStandPower standPower, StandEntity standEntity, Phase currentPhase) {
        return currentAction != this && currentPhase == Phase.RECOVERY;
    }
    
    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return true;
    }
    
    
    private final LazySupplier<ResourceLocation> backshotTex = 
            new LazySupplier<>(() -> makeIconVariant(this, "_back"));
    @Override
    public ResourceLocation getIconTexturePath(@Nullable IStandPower power) {
        if (power != null && doesBackshot(power)) {
            return backshotTex.get();
        }
        else {
            return super.getIconTexturePath(power);
        }
    }
    
    
    
    
    public static class TheWorldTSHeavyPunch extends HeavyPunchInstance {
        private Supplier<SoundEvent> voiceLineOnKill = () -> null;

        public TheWorldTSHeavyPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
            super(stand, target, dmgSource);
        }
        
        public TheWorldTSHeavyPunch setVoiceLineOnKill(Supplier<SoundEvent> sound) {
            this.voiceLineOnKill = sound != null ? sound : () -> null;
            return this;
        }

        @Override
        protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {
            if (killed) {
                SoundEvent voiceLine = voiceLineOnKill.get();
                if (voiceLine != null) {
                    LivingEntity user = stand.getUser();
                    if (user != null && stand.distanceToSqr(user) > 16) {
                        JojoModUtil.sayVoiceLine(user, voiceLine);
                    }
                }
            }
            super.afterAttack(stand, target, dmgSource, task, hurt, killed);
        }
    }
}
