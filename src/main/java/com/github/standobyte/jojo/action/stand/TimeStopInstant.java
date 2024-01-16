package com.github.standobyte.jojo.action.stand;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrNoMotionLerpPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class TimeStopInstant extends StandAction {
    final Supplier<SoundEvent> blinkSound;
    private final Supplier<TimeStop> baseTimeStop;
    private final boolean teleportBehindEntity;
    
    public TimeStopInstant(StandAction.Builder builder, 
            @Nonnull Supplier<TimeStop> baseTimeStopAction, @Nullable Supplier<SoundEvent> blinkSound) {
        this(builder, baseTimeStopAction, blinkSound, false);
    }
    
    public TimeStopInstant(StandAction.Builder builder, 
            @Nonnull Supplier<TimeStop> baseTimeStopAction, @Nullable Supplier<SoundEvent> blinkSound,
            boolean teleportBehindEntity) {
        super(builder);
        this.baseTimeStop = baseTimeStopAction;
        this.blinkSound = blinkSound;
        this.teleportBehindEntity = teleportBehindEntity;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (TimeStopHandler.isTimeStopped(user.level, user.blockPosition())) {
            return ActionConditionResult.NEGATIVE;
        }
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity
                && ((StandEntity) power.getStandManifestation()).getCurrentTask().isPresent()) {
            return ActionConditionResult.NEGATIVE;
        }
        return super.checkSpecificConditions(user, power, target);
    }
    
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        playSound(world, user);

        int timeStopTicks = getMaxImpliedTicks(power);
        double speed = getDistancePerTick(user);
        double distance = getMaxDistance(user, power, speed, timeStopTicks);
        ObjectWrapper<ActionTarget> targetMutable = new ObjectWrapper<>(target);
        Vector3d blinkPos = calcBlinkPos(user, targetMutable, distance);
        target = targetMutable.get();
        distance = user.position().subtract(blinkPos).length();
        
        if (target.getType() == ActionTarget.TargetType.ENTITY) {
            Entity targetEntity = target.getEntity();
            Vector3d toTarget = targetEntity.position().subtract(blinkPos);
            user.yRot = MathUtil.yRotDegFromVec(toTarget);
            user.yRotO = user.yRot;
        }

        user.level.getEntitiesOfClass(MobEntity.class, user.getBoundingBox().inflate(8), 
                mob -> mob.getTarget() == user
                && mob.getLookAngle().dot(mob.getEyePosition(1).subtract(blinkPos)) >= 0)
        .forEach(mob -> {
            MCUtil.loseTarget(mob, user);
        });
        
        int impliedTicks = MathHelper.clamp(MathHelper.ceil(distance / speed), 0, timeStopTicks);
        skipTicksForStandAndUser(power, impliedTicks);
        
        if (!world.isClientSide()) {
            power.consumeStamina(impliedTicks * getStaminaCostTicking(power));
            
            user.teleportTo(blinkPos.x, blinkPos.y, blinkPos.z);
            StandStats stats = power.getType().getStats();
            if (power.hasPower() && stats instanceof TimeStopperStandStats) {
                TimeStopperStandStats tsStats = (TimeStopperStandStats) stats;
//                float learning = tsStats.timeStopLearningPerTick * impliedTicks;
//                power.addLearningProgressPoints(baseTimeStop.get(), learning);
                
                int cooldown = (int) (TimeStopInstance.getTimeStopCooldown(power, tsStats, impliedTicks) * COOLDOWN_RATIO);
                power.setCooldownTimer(this, cooldown);
                if (!power.isActionOnCooldown(getBaseTimeStop())) {
                    power.setCooldownTimer(getBaseTimeStop(), cooldown);
                }
            }
        }
        
        user.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.hasUsedTimeStopToday = true);
    }
    
    public int getMaxImpliedTicks(IStandPower power) {
        int timeStopTicks = TimeStop.getTimeStopTicks(power, this);
        if (!StandUtil.standIgnoresStaminaDebuff(power)) {
            timeStopTicks = MathHelper.clamp(MathHelper.floor((power.getStamina() - getStaminaCost(power)) / getStaminaCostTicking(power)), 5, timeStopTicks);
        }
        
        return timeStopTicks;
    }
    
    public double getMaxDistance(LivingEntity user, IStandPower power, double playerSpeed, int timeStopTicks) {
        double distance = Math.min(playerSpeed * timeStopTicks, 192);
        return distance;
    }
    
    public static double getDistancePerTick(LivingEntity entity) {
        return entity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2.1585;
    }
    
    public Vector3d calcBlinkPos(LivingEntity user, IStandPower power, ActionTarget initialTarget) {
        return calcBlinkPos(user, new ObjectWrapper<>(initialTarget), getMaxDistance(user, power, getDistancePerTick(user), getMaxImpliedTicks(power)));
    }
    
    public Vector3d calcBlinkPos(LivingEntity user, ObjectWrapper<ActionTarget> initialTarget, double maxDistance) {
        Vector3d blinkPos = null;
        ActionTarget target = initialTarget.get();
        if (target.getType() == TargetType.EMPTY) {
            RayTraceResult rayTrace = JojoModUtil.rayTrace(user, maxDistance, 
                    entity -> entity instanceof LivingEntity && !(entity instanceof StandEntity && ((StandEntity) entity).getUser() == user));
            if (rayTrace.getType() == RayTraceResult.Type.MISS) {
                blinkPos = rayTrace.getLocation();
            }
            target = ActionTarget.fromRayTraceResult(rayTrace);
        }
        initialTarget.set(target);
        
        switch (target.getType()) {
        case ENTITY:
            blinkPos = getEntityTargetTeleportPos(user, target.getEntity());
            break;
        case BLOCK:
            BlockPos blockPosTargeted = target.getBlockPos();
            blinkPos = Vector3d.atBottomCenterOf(user.level.isEmptyBlock(blockPosTargeted.above()) ? blockPosTargeted.above() : blockPosTargeted.relative(target.getFace()));
            break;
        default:
            Vector3d pos = blinkPos;
            BlockPos blockPos = new BlockPos(pos);
            while (user.level.isEmptyBlock(blockPos.below()) && blockPos.getY() > 0) {
                blockPos = blockPos.below();
            }
            blinkPos = new Vector3d(pos.x, blockPos.getY() > 0 ? blockPos.getY() : user.position().y, pos.z);
            break;
        }
        
        return blinkPos;
    }
    
    public static final float COOLDOWN_RATIO = 1F / 6F;
    
    void playSound(World world, Entity entity) {
        if (blinkSound != null) {
            SoundEvent sound = blinkSound.get();
            if (sound != null) {
                MCUtil.playSound(world, entity instanceof PlayerEntity ? (PlayerEntity) entity : null, entity.getX(), entity.getY(), entity.getZ(), 
                        sound, SoundCategory.AMBIENT, 5.0F, 1.0F, TimeStopHandler::canPlayerSeeInStoppedTime);
            }
        }
    }
    
    protected Vector3d getEntityTargetTeleportPos(Entity user, Entity target) {
        Vector3d pos;
        if (teleportBehindEntity) {
            pos = target.position()
                    .subtract(Vector3d.directionFromRotation(0, target.yRot)
                            .scale(target.getBbWidth() + user.getBbWidth()));
        }
        else {
            double distance = target.getBbWidth() + user.getBbWidth();
            pos = user.distanceToSqr(target) > distance * distance ? target.position().subtract(user.getLookAngle().scale(distance)) : user.position();
        }
        
        return pos;
    }
    
    @Override
    public float getStaminaCost(IStandPower stand) {
        return getBaseTimeStop() != null ? getBaseTimeStop().getStaminaCost(stand) * 0.8F : super.getStaminaCost(stand);
    }
    
    @Override
    public float getStaminaCostTicking(IStandPower stand) {
        return getBaseTimeStop() != null ? getBaseTimeStop().getStaminaCostTicking(stand) * 0.8F : super.getStaminaCostTicking(stand);
    }

    @Override
    public float getMaxTrainingPoints(IStandPower power) {
        return TimeStop.getMaxTimeStopTicks(power) - TimeStop.MIN_TIME_STOP_TICKS;
    }
    
    @Override
    public boolean canUserSeeInStoppedTime(LivingEntity user, IStandPower power) {
        return true;
    }
    
    public static void skipTicksForStandAndUser(IStandPower standPower, int ticks) {
        if (standPower.getUser() != null) {
            skipTicks(standPower.getUser(), ticks);
        }
        if (standPower.getStandManifestation() instanceof StandEntity) {
            skipStandTicks((StandEntity) standPower.getStandManifestation(), ticks);
        }
    }
    
    private static void skipTicks(LivingEntity entity, int ticks) {
        if (!entity.level.isClientSide()) {
            PacketManager.sendToClientsTracking(new TrNoMotionLerpPacket(entity.getId(), 3), entity);
        }
        // also clear user's movement input before tick skipping
//        if (entity.canUpdate()) {
//            for (int i = 0; i < ticks; i++) {
//                entity.tickCount++;
//                entity.tick();
//            }
//        }
//        else {
            entity.tickCount += ticks;
//        }
    }
    
    private static void skipStandTicks(StandEntity entity, int ticks) {
        skipTicks(entity, ticks);
        
        // FIXME skip overlay ticks for tracking too
        entity.overlayTickCount += ticks;
    }
    
    
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return getBaseTimeStop().isUnlocked(power);
    }
    
    TimeStop getBaseTimeStop() {
        return baseTimeStop.get();
    }
    
    @Override
    public void onCommonSetup() {
        getBaseTimeStop().setInstantTSVariation(this);
        if (getBaseTimeStop().getShiftVariationIfPresent() instanceof TimeResume) {
            ((TimeResume) getBaseTimeStop().getShiftVariationIfPresent()).setInstantTSAction(this);
        }
    }
}
