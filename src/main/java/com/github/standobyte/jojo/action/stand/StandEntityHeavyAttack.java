package com.github.standobyte.jojo.action.stand;

import java.util.EnumSet;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StandEntityHeavyAttack extends StandEntityAction {
	private final Supplier<StandEntityComboHeavyAttack> comboAttack;

    public StandEntityHeavyAttack(StandEntityHeavyAttack.Builder builder, @Nullable Supplier<StandEntityComboHeavyAttack> comboAttack) {
        super(builder);
        this.comboAttack = comboAttack;
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
        standEntity.punch(this, getPunch(), task.getTarget());
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
    	
    	public Builder() {
    		standPose(StandPose.HEAVY_ATTACK).staminaCost(50F)
            .standOffsetFromUser(-0.75, 0.75)
            .targetPunchProperties((punch, stand, target) -> {
            	double strength = stand.getAttackDamage();
            	return punch.get()
            			.damage(StandStatFormulas.getHeavyAttackDamage(strength))
                        .addKnockback(1 + (float) strength / 4 * stand.getLastHeavyPunchCombo())
                        .setStandInvulTime(10)
                        .setPunchSound(ModSounds.STAND_STRONG_ATTACK.get());
            });
    	}

		@Override
		protected Builder getThis() {
			return this;
		}
    }
}
