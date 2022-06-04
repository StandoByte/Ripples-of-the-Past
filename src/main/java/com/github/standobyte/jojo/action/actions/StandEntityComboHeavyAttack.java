package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.world.World;

public class StandEntityComboHeavyAttack extends StandEntityHeavyAttack {

	public StandEntityComboHeavyAttack(StandEntityHeavyAttack.Builder builder) {
		super(builder, null);
	}
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            standEntity.punch(PunchType.HEAVY_COMBO, task.getTarget(), this);
        }
    }
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return StandUtil.isComboUnlocked(power);
    }
    
    @Override
    protected boolean playsVoiceLineOnShift() {
    	return true;
    }
    
    
    
    public static class Builder extends StandEntityHeavyAttack.Builder {
    	
    	public Builder() {
    		standPose(StandPose.HEAVY_ATTACK_COMBO);
    	}
    }
}
