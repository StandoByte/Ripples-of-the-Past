package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class SilverChariotDashAttack extends StandEntityHeavyAttack {

    public SilverChariotDashAttack(StandEntityHeavyAttack.Builder builder, Supplier<StandEntityComboHeavyAttack> comboAttack) {
        super(builder, comboAttack);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        if (stand instanceof SilverChariotEntity && !((SilverChariotEntity) stand).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return 0;
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return Math.max(StandStatFormulas.getHeavyAttackWindup(standEntity.getAttackSpeed(), standEntity.getLastHeavyPunchCombo()), 2);
    }
    
    @Override
    public void onPhaseSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, ActionTarget target, int ticks) {
        super.onPhaseSet(world, standEntity, standPower, phase, target, ticks);
        if (phase == Phase.PERFORM) {
            if (standEntity.isFollowingUser() && standEntity.getAttackSpeed() < 24) {
                LivingEntity user = standEntity.getUser();
                if (user != null) {
                    Vector3d vec = MathUtil.relativeCoordsToAbsolute(0, 0, 1.0, user.yRot);
                    standEntity.setPos(user.getX() + vec.x, 
                            standEntity.getY(), 
                            user.getZ() + vec.z);
                }
            }
            ((SilverChariotEntity) standEntity).setDashVec(standEntity.getLookAngle().scale(10D / (double) ticks));
        }
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        float completion = task.getTaskCompletion(1.0F);
        boolean lastTick = task.getTicksLeft() <= 1;
        boolean moveForward = completion <= 0.5F;
        if (moveForward) {
        	for (RayTraceResult rayTraceResult : JojoModUtil.rayTraceMultipleEntities(standEntity, 
        			standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get()), 
        			standEntity.canTarget(), 0.25, standEntity.getPrecision())) {
        		standEntity.attackTarget(ActionTarget.fromRayTraceResult(rayTraceResult), PunchType.HEAVY_NO_COMBO, this, 1, null);
        	}
        }
        else if (!Vector3d.ZERO.equals(standEntity.getDeltaMovement())) {
        	standEntity.punch(PunchType.HEAVY_NO_COMBO, task.getTarget(), this);
        }
        if (!world.isClientSide() && lastTick && standEntity.isFollowingUser()) {
        	standEntity.retractStand(false);
        }
        standEntity.setDeltaMovement(moveForward ? ((SilverChariotEntity) standEntity).getDashVec().scale(completion * 8) : Vector3d.ZERO);
    }
    
    @Override
    public boolean isChainable(IStandPower standPower, StandEntity standEntity) {
    	return true;
    }
    
    @Override
    protected boolean canBeQueued(IStandPower standPower, StandEntity standEntity) {
        return false;
    }

    protected boolean lastTargetCheck(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
		return false;
    }
    
    @Override
    protected boolean standMovesByItself(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
}
