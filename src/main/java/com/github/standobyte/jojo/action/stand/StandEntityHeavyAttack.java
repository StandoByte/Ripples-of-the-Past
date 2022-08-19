package com.github.standobyte.jojo.action.stand;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.HeavyEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.PunchHandler;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StandEntityHeavyAttack extends StandEntityAction {
    protected final PunchHandler punch;
	private final Supplier<StandEntityComboHeavyAttack> comboAttack;

    public StandEntityHeavyAttack(StandEntityHeavyAttack.Builder builder, @Nullable Supplier<StandEntityComboHeavyAttack> comboAttack) {
        super(builder);
        this.punch = builder.punch.build();
        this.comboAttack = comboAttack;
    }

	@Override
    protected Action<IStandPower> replaceAction(IStandPower power) {
    	return comboAttack != null && comboAttack.get() != null
    			&& power.isActive() && ((StandEntity) power.getStandManifestation()).willHeavyPunchCombo()
    			? comboAttack.get() : this;
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
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        standEntity.alternateHands();
        if (!world.isClientSide()) {
            standEntity.addComboMeter(-0.51F, 0);
        }
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        standEntity.punch(task, punch, task.getTarget());
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
    public boolean noComboDecay() {
        return true;
    }
    
    @Override
    public boolean canFollowUpBarrage() {
        return true;
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityHeavyAttack.Builder> {
        private PunchHandler.Builder punch = new PunchHandler.Builder().setEntityPunch(HeavyEntityPunch::new);
    	
    	public Builder() {
    		standPose(StandPose.HEAVY_ATTACK).staminaCost(50F)
            .standOffsetFromUser(-0.75, 0.75).standKeepsTarget(TargetType.ENTITY);
    	}
        
        public Builder modifyPunch(UnaryOperator<PunchHandler.Builder> modifier) {
            return setPunch(modifier.apply(punch));
        }
        
        public Builder setPunch(PunchHandler.Builder punch) {
            this.punch = punch;
            return getThis();
        }

		@Override
		protected Builder getThis() {
			return this;
		}
    }
}
