package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.TimeUtil;

import net.minecraft.entity.LivingEntity;
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
        return super.checkSpecificConditions(user, power, target);
    }

    @Override
    public Action<IStandPower> getVisibleAction(IStandPower power) {
        LivingEntity user = power.getUser();
        return user != null && TimeResume.userTimeStopInstance(user.level, user, null)
                ? ModActions.TIME_RESUME.get() : super.getVisibleAction(power);
    }
    
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        SoundEvent sound = blinkSound.get();
        if (sound != null) {
            JojoModUtil.playSound(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, user.getX(), user.getY(), user.getZ(), 
                    sound, SoundCategory.AMBIENT, 5.0F, 1.0F, TimeUtil::canPlayerSeeInStoppedTime);
        }
        
        int timeStopTicks = TimeStop.getTimeStopTicks(power, this);
        if (!StandUtil.standIgnoresStaminaDebuff(user)) {
            timeStopTicks = Math.min(timeStopTicks, MathHelper.floor(power.getStamina() / getStaminaCostTicking(power)));
        }

        Vector3d blinkPos = null;
        double speed = user.getSpeed() * 2.1585;
        if (target.getType() == TargetType.EMPTY) {
            RayTraceResult rayTrace = JojoModUtil.rayTrace(user, Math.min(speed * timeStopTicks, 192), null);
            if (rayTrace.getType() == RayTraceResult.Type.MISS) {
                blinkPos = rayTrace.getLocation();
            }
            target = ActionTarget.fromRayTraceResult(rayTrace);
        }
        switch (target.getType()) {
        case ENTITY:
            blinkPos = target.getTargetPos(false);
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
        // FIXME (!!) (ts tp) clear user's movement input before tick skipping
        skipTicksForStandAndUser(power, impliedTicks);
        
        if (!world.isClientSide()) {
            power.consumeStamina(impliedTicks * getStaminaCostTicking(power));
            user.teleportTo(blinkPos.x, blinkPos.y, blinkPos.z);
            if (baseTimeStop.get() != null && power.hasPower()
                    && power.getType().getStats() instanceof TimeStopperStandStats) {
                TimeStopperStandStats stats = (TimeStopperStandStats) power.getType().getStats();
                float learning = stats.timeStopLearningPerTick * impliedTicks;
                power.addLearningProgressPoints(baseTimeStop.get(), learning);
            }
        }
    }
    
    @Override
    public float getStaminaCost(IStandPower stand) {
        return baseTimeStop.get() != null ? baseTimeStop.get().getStaminaCost(stand) : super.getStaminaCost(stand);
    }
    
    @Override
    public float getStaminaCostTicking(IStandPower stand) {
        return baseTimeStop.get() != null ? baseTimeStop.get().getStaminaCostTicking(stand) : super.getStaminaCostTicking(stand);
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
        // FIXME (!!) (ts tp) ts skip entity ticks
        // FIXME (!!) (ts tp) doesn't work on server thread
        if (entity.canUpdate()) {
            for (int i = 0; i < ticks; i++) {
                entity.tickCount++;
                entity.tick();
            }
        }
        else {
            entity.tickCount += ticks;
        }
    }
    
    private static void skipStandTicks(StandEntity entity, int ticks) {
        skipTicks(entity, ticks);
        // FIXME (!!) (ts tp) ts skip entity ticks
        
    }
}
