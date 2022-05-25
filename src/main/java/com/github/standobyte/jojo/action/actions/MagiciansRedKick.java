package com.github.standobyte.jojo.action.actions;

import java.util.Optional;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class MagiciansRedKick extends StandEntityComboHeavyAttack {

    public MagiciansRedKick(Builder builder) {
        super(builder);
    }

    @Override
    public ActionTarget targetBeforePerform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) power.getStandManifestation();
            Optional<LivingEntity> bound = MagiciansRedRedBind.getLandedRedBind(standEntity).map(MRRedBindEntity::getEntityAttachedTo);
            if (bound.isPresent()) {
                return new ActionTarget(bound.get());
            }
        }
        return super.targetBeforePerform(world, user, power, target);
    }

    @Override
    protected void setAction(IStandPower standPower, StandEntity standEntity, int ticks, Phase phase, ActionTarget target) {
    	MagiciansRedRedBind.getLandedRedBind(standEntity).ifPresent(redBind -> {
    		redBind.setKickCombo();
    	});
        super.setAction(standPower, standEntity, ticks, phase, target);
    }
    
    private static final double SLIDE_DISTANCE = 3;
    @Override
    public void standTickWindup(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
    	int ticksLeft = standEntity.getCurrentTask().map(StandEntityTask::getTicksLeft).get();
    	if (ticksLeft == 2) {
    		Vector3d targetPos = target.getTargetPos(true);
    		Vector3d slideVec;
    		if (targetPos != null) {
    			slideVec = targetPos.subtract(standEntity.getEyePosition(1.0F));
    			slideVec = slideVec.normalize().scale(MathHelper.clamp(slideVec.length() - standEntity.getBbWidth(), 0, SLIDE_DISTANCE));
    		}
    		else {
    			slideVec = standEntity.getLookAngle().scale(SLIDE_DISTANCE);
    		}
    		standEntity.setDeltaMovement(slideVec);
    	}
    	else if (ticksLeft == 1) {
    		standEntity.setDeltaMovement(Vector3d.ZERO);
    	}
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            MagiciansRedRedBind.getLandedRedBind(standEntity).ifPresent(redBind -> {
                if (redBind.isInKickCombo()) {
                    redBind.remove();
                }
            });
        }
        super.standPerform(world, standEntity, userPower, target);
    }
    
    @Override
    protected boolean standMovesByItself(IStandPower standPower, StandEntity standEntity) {
    	Phase phase = standEntity.getCurrentTaskPhase().get();
    	return phase == Phase.WINDUP && standEntity.getCurrentTask().map(StandEntityTask::getTicksLeft).get() <= 2
    			|| phase == Phase.PERFORM || phase == Phase.RECOVERY;
    }
}
