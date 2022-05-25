package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.world.World;

public class StandEntityComboHeavyAttack extends StandEntityHeavyAttack {

	public StandEntityComboHeavyAttack(StandEntityHeavyAttack.Builder builder) {
		super(builder, null);
	}
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            standEntity.punch(PunchType.HEAVY_COMBO, target, this);
        }
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return StandUtil.isComboUnlocked(power);
    }
    
    
    
    public static class Builder extends StandEntityHeavyAttack.Builder {
    	
    	public Builder() {
    		standPose(StandPose.HEAVY_ATTACK_COMBO);
    	}
    }
}
