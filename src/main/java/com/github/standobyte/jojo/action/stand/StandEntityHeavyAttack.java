package com.github.standobyte.jojo.action.stand;

import java.util.EnumSet;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.punch.PunchHandler;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StandEntityHeavyAttack extends StandEntityAction {
	private final Supplier<StandEntityComboHeavyAttack> comboAttack;
	protected final PunchHandler punch;

    public StandEntityHeavyAttack(StandEntityHeavyAttack.Builder builder, @Nullable Supplier<StandEntityComboHeavyAttack> comboAttack) {
        super(builder);
        this.comboAttack = comboAttack;
        this.punch = builder.punch.build();
    }

	@Override
    protected Action<IStandPower> replaceAction(IStandPower power) {
	    if (comboAttack != null && comboAttack.get() != null) {
	        StandEntityComboHeavyAttack comboAttack = this.comboAttack.get();
	        EnumSet<StandPart> missingParts = EnumSet.complementOf(power.getStandInstance().get().getAllParts());
	        if (!missingParts.isEmpty()) {
	            boolean canUseThis = true;
	            for (StandPart missingPart : missingParts) {
	                if (comboAttack.isPartRequired(missingPart)) {
	                    return this;
	                }
                    if (this.isPartRequired(missingPart)) {
                        canUseThis = false;
                    }
	            }
	            if (!canUseThis) {
	                return comboAttack;
	            }
	        }
	        
	        return power.isActive() && ((StandEntity) power.getStandManifestation()).willHeavyPunchCombo()
	                ? comboAttack : this;
	    }
	    return this;
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
            .standOffsetFromUser(-0.75, 0.75);
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
    
    
    
    public static class HeavyEntityPunch extends StandEntityPunch {
        
        public HeavyEntityPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
            super(stand, target, dmgSource);
            double strength = stand.getAttackDamage();
            this
            .damage(StandStatFormulas.getHeavyAttackDamage(strength))
            .addKnockback(0.5F + (float) strength / 8)
            .setStandInvulTime(10)
            .setPunchSound(ModSounds.STAND_STRONG_ATTACK);
        }
    }
}
