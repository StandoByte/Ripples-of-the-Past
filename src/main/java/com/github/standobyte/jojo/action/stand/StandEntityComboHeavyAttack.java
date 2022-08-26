package com.github.standobyte.jojo.action.stand;

import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;

public class StandEntityComboHeavyAttack extends StandEntityHeavyAttack {

	public StandEntityComboHeavyAttack(StandEntityHeavyAttack.Builder builder) {
		super(builder);
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
        
    	@Override
        public StandEntityHeavyAttack.Builder setComboAttack(Supplier<StandEntityComboHeavyAttack> comboAttack) {
            return getThis();
        }
    }
}
