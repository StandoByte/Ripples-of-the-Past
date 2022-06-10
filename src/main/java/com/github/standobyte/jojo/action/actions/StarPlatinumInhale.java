package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class StarPlatinumInhale extends StandEntityAction {

	public StarPlatinumInhale(StandEntityAction.Builder builder) {
		super(builder);
	}

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
    	Vector3d mouthPos = standEntity.position()
    			.add(0, standEntity.getBbHeight() * 0.75F, 0)
    			.add(new Vector3d(0, standEntity.getBbHeight() / 16F, standEntity.getBbWidth() * 0.5F)
    					.xRot(-standEntity.xRot * MathUtil.DEG_TO_RAD).yRot((-standEntity.yRot) * MathUtil.DEG_TO_RAD));
    	
    	Vector3d spLookVec = standEntity.getLookAngle();
    	world.getEntities(standEntity, standEntity.getBoundingBox().inflate(10, 10, 10), 
    			entity -> spLookVec.dot(entity.position().subtract(standEntity.position()).normalize()) > 0.5 && standEntity.canSee(entity)
    			&& entity.distanceToSqr(standEntity) > 0.5
        		// FIXME (!!) free flight
    			&& (standEntity.isManuallyControlled() || !entity.is(standEntity.getUser()))).forEach(entity -> {
    				double distance = entity.distanceTo(standEntity);
    				Vector3d suctionVec = mouthPos.subtract(entity.getBoundingBox().getCenter())
    						.normalize().scale(0.5 * standEntity.getStandEfficiency());
    				entity.setDeltaMovement(distance > 2 ? 
    						entity.getDeltaMovement().add(suctionVec.scale(1 / distance))
    						: suctionVec.scale(Math.max(distance - 1, 0)));
    				if (!world.isClientSide() && distance < 4 && entity instanceof LivingEntity) {
    					DamageUtil.suffocateTick((LivingEntity) entity, 0.05F);
    				}
    			});
    	if (world.isClientSide()) {
    		// FIXME (!!) (inhale) particles
    	}
    }

    @Override
    public int getCooldownAdditional(IStandPower power, int ticksHeld) {
    	int cooldown = super.getCooldownAdditional(power, ticksHeld);
    	return cooldown / 2 + cooldownFromHoldDuration(cooldown / 2, power, ticksHeld);
    }

}
