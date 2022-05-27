package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class StarPlatinumInhale extends StandEntityAction {

	public StarPlatinumInhale(StandEntityAction.Builder builder) {
		super(builder);
	}

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
    	Vector3d mouthPos = standEntity.position()
    			.add(0, standEntity.getBbHeight() * 0.75F, 0)
    			.add(new Vector3d(0, standEntity.getBbHeight() / 16F, standEntity.getBbWidth() * 0.5F)
    					.xRot(-standEntity.xRot * MathUtil.DEG_TO_RAD).yRot((-standEntity.yRot) * MathUtil.DEG_TO_RAD));
    	
    	if (!world.isClientSide()) {
    		// FIXME (!!!) (inhale) pull & suffocate entities
    	}
    	else {
    		// FIXME (!!!) (inhale) particles
    	}
    }

    @Override
    public int getCooldownAdditional(IStandPower power, int ticksHeld) {
    	return cooldownFromHoldDuration(super.getCooldownAdditional(power, ticksHeld), power, ticksHeld);
    }

}
