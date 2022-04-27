package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeUtil;

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

    public TimeStopInstant(StandAction.Builder builder, @Nonnull Supplier<SoundEvent> blinkSound) {
        super(builder);
        this.blinkSound = blinkSound;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (TimeUtil.isTimeStopped(user.level, user.blockPosition())) {
            return ActionConditionResult.NEGATIVE;
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        int timeStopTicks = Math.min(TimeStop.getTimeStopTicks(power, this, user, INonStandPower.getNonStandPowerOptional(user)), 
                MathHelper.floor(power.getStamina() / getStaminaCostTicking(power)));
        SoundEvent sound = blinkSound.get();
        if (sound != null) {
            JojoModUtil.playSound(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, user.getX(), user.getY(), user.getZ(), 
                    sound, SoundCategory.AMBIENT, 5.0F, 1.0F, TimeUtil::canPlayerSeeInStoppedTime);
        }
        if (!world.isClientSide()) {
            Vector3d blinkPos = null;
            double speed = user.getSpeed() * 2.1585;
            if (target.getType() == TargetType.EMPTY) {
                RayTraceResult rayTrace = JojoModUtil.rayTrace(user, speed * timeStopTicks, null);
                if (rayTrace.getType() == RayTraceResult.Type.MISS) {
                    blinkPos = rayTrace.getLocation();
                }
                target = ActionTarget.fromRayTraceResult(rayTrace);
            }
            switch (target.getType()) {
            case ENTITY:
                blinkPos = target.getTargetPos();
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
            power.consumeStamina(impliedTicks * getStaminaCostTicking(power));
            user.teleportTo(blinkPos.x, blinkPos.y, blinkPos.z);
            power.addLearningProgressPoints(this, 5);
            if (isShiftVariation()) {
                power.addLearningProgressPoints(getBaseVariation(), 5);
            }
            if (power.isActive()) {
                IStandManifestation stand = power.getStandManifestation();
                if (stand instanceof StandEntity) {
                    StandEntity standEntity = (StandEntity) stand;
                    standEntity.setStandPose(StandPose.IDLE);
                    standEntity.summonLockTicks = Math.min(standEntity.summonLockTicks - timeStopTicks, 0);
                    standEntity.gradualSummonWeaknessTicks = Math.min(standEntity.gradualSummonWeaknessTicks - timeStopTicks, 0);
                }
            }
        }
    }

    @Override
    public float getMaxTrainingPoints(IStandPower power) {
        return TimeStop.getMaxTimeStopTicks(power) - TimeStop.MIN_TIME_STOP_TICKS;
    }
    
    @Override
    public boolean canUserSeeInStoppedTime(LivingEntity user, IStandPower power) {
        return true;
    }
}
