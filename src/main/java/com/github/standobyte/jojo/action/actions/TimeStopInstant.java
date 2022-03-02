package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

//FIXME (!) time stop learning progress
public class TimeStopInstant extends StandAction {

    public TimeStopInstant(StandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        if (TimeHandler.isTimeStopped(user.level, user.blockPosition())) {
            return ActionConditionResult.NEGATIVE;
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        int timeStopTicks = TimeHandler.getTimeStopTicks(power, this, user, INonStandPower.getNonStandPowerOptional(user));
        JojoModUtil.playSound(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, user.getX(), user.getY(), user.getZ(), 
                ModSounds.TIME_STOP_BLINK.get(), SoundCategory.AMBIENT, 5.0F, 1.0F, TimeHandler::canPlayerSeeInStoppedTime);
        if (!world.isClientSide()) {
            Vector3d blinkPos = null;
            if (target.getType() == TargetType.EMPTY) {
                double speed = user.getSpeed() * 2.1585;
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
            
            user.teleportTo(blinkPos.x, blinkPos.y, blinkPos.z);
            // FIXME (!) add progress points (depending on the distance)
            power.addLearningProgressPoints(this, 1F);
            power.addLearningProgressPoints(this.getBaseVariation(), 1F);
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
        return TimeHandler.getMaxTimeStopTicks(power, INonStandPower.getNonStandPowerOptional(power.getUser()));
    }
}
