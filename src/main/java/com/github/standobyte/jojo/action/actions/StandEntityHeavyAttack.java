package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StandEntityHeavyAttack extends StandEntityAction {
	private final Supplier<StandEntityComboHeavyAttack> comboAttack;

    public StandEntityHeavyAttack(StandEntityHeavyAttack.Builder builder, Supplier<StandEntityComboHeavyAttack> comboAttack) {
        super(builder);
        this.comboAttack = comboAttack;
    }

	@Override
    public Action<IStandPower> getVisibleAction(IStandPower power) {
    	return comboAttack != null && comboAttack.get() != null && comboAttack.get().isUnlocked(power)
    			&& power.isActive() && ((StandEntity) power.getStandManifestation()).willHeavyPunchCombo()
    			? comboAttack.get() : super.getVisibleAction(power);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    public void onClick(World world, LivingEntity user, IStandPower power) {
    	super.onClick(world, user, power);
    	if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
    		((StandEntity) power.getStandManifestation()).setHeavyPunchCombo();
    	}
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, ActionTarget target, int ticks) {
        standEntity.alternateHands();
        if (!world.isClientSide()) {
            standEntity.addComboMeter(-0.51F, 0);
        }
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            standEntity.punch(PunchType.HEAVY_NO_COMBO, target, this);
        }
    }

    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackWindup(standEntity.getAttackSpeed(), standEntity.getComboMeter());
    }

    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackRecovery(standEntity.getAttackSpeed());
    }
    
    @Override
    public boolean isCombatAction() {
        return true;
    }
    
    @Override
    public boolean canFollowUpBarrage() {
        return true;
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityHeavyAttack.Builder> {
    	
    	public Builder() {
    		standPose(StandPose.HEAVY_ATTACK).staminaCost(50F)
            .standOffsetFromUser(-0.75, 0.75).standTakesCrosshairTarget(TargetType.ENTITY);
    	}

		@Override
		protected Builder getThis() {
			return this;
		}
    }
}
