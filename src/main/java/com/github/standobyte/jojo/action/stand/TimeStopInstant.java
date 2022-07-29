package com.github.standobyte.jojo.action.stand;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrNoMotionLerpPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.power.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.MathUtil;
import com.github.standobyte.jojo.util.utils.TimeUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
    private final Supplier<SoundEvent> blinkSound;
    private final Supplier<TimeStop> baseTimeStop;

    public TimeStopInstant(StandAction.Builder builder, @Nonnull Supplier<TimeStop> baseTimeStop, @Nonnull Supplier<SoundEvent> blinkSound) {
        super(builder);
        this.blinkSound = blinkSound;
        this.baseTimeStop = baseTimeStop;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (TimeUtil.isTimeStopped(user.level, user.blockPosition())) {
            return ActionConditionResult.NEGATIVE;
        }
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity
        		&& ((StandEntity) power.getStandManifestation()).getCurrentTask().isPresent()) {
            return ActionConditionResult.NEGATIVE;
        }
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    protected Action<IStandPower> replaceAction(IStandPower power) {
        LivingEntity user = power.getUser();
        return user != null && TimeResume.userTimeStopInstance(user.level, user, null)
                ? ModActions.TIME_RESUME.get() : this;
    }
    
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
    	playSound(world, user);
        
        int timeStopTicks = TimeStop.getTimeStopTicks(power, this);
        if (!StandUtil.standIgnoresStaminaDebuff(power)) {
            timeStopTicks = MathHelper.clamp(MathHelper.floor((power.getStamina() - getStaminaCost(power)) / getStaminaCostTicking(power)), 5, timeStopTicks);
        }

        Vector3d blinkPos = null;
        double speed = getDistancePerTick(user);
        if (target.getType() == TargetType.EMPTY) {
            RayTraceResult rayTrace = JojoModUtil.rayTrace(user, Math.min(speed * timeStopTicks, 192), null);
            if (rayTrace.getType() == RayTraceResult.Type.MISS) {
                blinkPos = rayTrace.getLocation();
            }
            target = ActionTarget.fromRayTraceResult(rayTrace);
        }
        switch (target.getType()) {
        case ENTITY:
            blinkPos = getEntityTargetTeleportPos(user, target.getEntity());
    		Vector3d toTarget = target.getEntity().position().subtract(blinkPos);
    		user.yRot = MathUtil.yRotDegFromVec(toTarget);
    		user.yRotO = user.yRot;
            break;
        case BLOCK:
            BlockPos blockPosTargeted = target.getBlockPos();
            blinkPos = Vector3d.atBottomCenterOf(world.isEmptyBlock(blockPosTargeted.above()) ? blockPosTargeted.above() : blockPosTargeted.relative(target.getFace()));
            break;
        default:
            Vector3d pos = blinkPos;
            BlockPos blockPos = new BlockPos(pos);
            while (world.isEmptyBlock(blockPos.below()) && blockPos.getY() > 0) {
                blockPos = blockPos.below();
            }
            blinkPos = new Vector3d(pos.x, blockPos.getY() > 0 ? blockPos.getY() : user.position().y, pos.z);
            break;
        }

        int impliedTicks = MathHelper.ceil(user.position().subtract(blinkPos).length() / speed);
        skipTicksForStandAndUser(power, impliedTicks);
        
        if (!world.isClientSide()) {
            power.consumeStamina(impliedTicks * getStaminaCostTicking(power));

            user.teleportTo(blinkPos.x, blinkPos.y, blinkPos.z);
            StandStats stats = power.getType().getStats();
            if (baseTimeStop.get() != null && power.hasPower()
                    && stats instanceof TimeStopperStandStats) {
                TimeStopperStandStats tsStats = (TimeStopperStandStats) stats;
                float learning = tsStats.timeStopLearningPerTick * impliedTicks;
                power.addLearningProgressPoints(baseTimeStop.get(), learning);
                
                int cooldown = (int) (TimeStopInstance.getTimeStopCooldown(power, tsStats, impliedTicks) * COOLDOWN_RATIO);
            	power.setCooldownTimer(this, cooldown);
            	if (!power.isActionOnCooldown(baseTimeStop.get())) {
            		power.setCooldownTimer(baseTimeStop.get(), cooldown);
            	}
            }
        }
        
        user.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.hasUsedTimeStopToday = true);
    }
    
    public static final float COOLDOWN_RATIO = 1F / 6F;
    
    public static double getDistancePerTick(LivingEntity entity) {
    	return entity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 2.1585;
    }
    
    void playSound(World world, Entity entity) {
        SoundEvent sound = blinkSound.get();
        if (sound != null) {
            JojoModUtil.playSound(world, entity instanceof PlayerEntity ? (PlayerEntity) entity : null, entity.getX(), entity.getY(), entity.getZ(), 
                    sound, SoundCategory.AMBIENT, 5.0F, 1.0F, TimeUtil::canPlayerSeeInStoppedTime);
        }
    }
    
    TimeStop getBaseTimeStop() {
    	return baseTimeStop.get();
    }
    
    protected Vector3d getEntityTargetTeleportPos(Entity user, Entity target) {
    	double distance = target.getBbWidth() + user.getBbWidth();
    	return user.distanceToSqr(target) > distance * distance ? target.position().subtract(user.getLookAngle().scale(distance)) : user.position();
    }
    
    @Override
    public float getStaminaCost(IStandPower stand) {
        return baseTimeStop.get() != null ? baseTimeStop.get().getStaminaCost(stand) * 0.8F : super.getStaminaCost(stand);
    }
    
    @Override
    public float getStaminaCostTicking(IStandPower stand) {
        return baseTimeStop.get() != null ? baseTimeStop.get().getStaminaCostTicking(stand) * 0.8F : super.getStaminaCostTicking(stand);
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
//    	if (entity.canUpdate()) {
//    		for (int i = 0; i < ticks; i++) {
//    			entity.tickCount++;
//    			entity.tick();
//    		}
//    	}
//        else {
            entity.tickCount += ticks;
//        }
    }
    
    private static void skipStandTicks(StandEntity entity, int ticks) {
        skipTicks(entity, ticks);
        
        // FIXME skip overlay ticks for tracking too
        entity.overlayTickCount += ticks;
    }
}
