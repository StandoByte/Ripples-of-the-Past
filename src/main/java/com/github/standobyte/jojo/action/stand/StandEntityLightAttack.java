package com.github.standobyte.jojo.action.stand;

import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.LightEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.PunchHandler;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class StandEntityLightAttack extends StandEntityAction {
    private final PunchHandler punch;

    public StandEntityLightAttack(StandEntityLightAttack.Builder builder) {
        super(builder);
        this.punch = builder.punch.build();
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        if (standEntity.isArmsOnlyMode() && standEntity.swingingArm == Hand.OFF_HAND) {
            standEntity.setArmsOnlyMode(true, true);
        }
        standEntity.alternateHands();
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        standEntity.punch(task, punch, task.getTarget());
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackWindup(speed, standEntity.getComboMeter(), standEntity.guardCounter());
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackRecovery(speed, standEntity.getComboMeter());
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackRecovery(speed, standEntity.getComboMeter())
                * (standEntity.isArmsOnlyMode() ? 2 : 4);
    }
    
    @Override
    public SoundEvent getSound(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        return task.getTarget().getType() != TargetType.ENTITY || standEntity.isArmsOnlyMode() || standEntity.getComboMeter() > 0
        		? null : super.getSound(standEntity, standPower, phase, task);
    }
    
    @Override
	protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
        return phase == Phase.RECOVERY || super.isCancelable(standPower, standEntity, newAction, phase);
    }
    
    @Override
    protected boolean isChainable(IStandPower standPower, StandEntity standEntity) {
    	return true;
    }
    
    @Override
    public boolean isFreeRecovery(IStandPower standPower, StandEntity standEntity) {
    	return true;
    }
    
    @Override
    public boolean noComboDecay() {
        return true;
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower standPower) {
        if (target.getType() == TargetType.BLOCK) {
            return ActionConditionResult.POSITIVE;
        }
        return super.checkTarget(target, user, standPower);
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityLightAttack.Builder>  {
        private PunchHandler.Builder punch = new PunchHandler.Builder().setEntityPunch(LightEntityPunch::new);
    	
    	public Builder() {
            standAutoSummonMode(AutoSummonMode.ONE_ARM).staminaCost(10F).standUserSlowDownFactor(1.0F)
            .standOffsetFront().standOffsetFromUser(-0.75, 0.75)
            .standPose(StandPose.LIGHT_ATTACK)
            .partsRequired(StandPart.ARMS);
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
