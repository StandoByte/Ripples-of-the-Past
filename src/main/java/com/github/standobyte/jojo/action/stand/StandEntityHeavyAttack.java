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
    private final Supplier<StandEntityAction> recoveryAction;
	protected final PunchHandler punch;

    public StandEntityHeavyAttack(StandEntityHeavyAttack.Builder builder) {
        super(builder);
        this.punch = builder.punch.build();
        this.comboAttack = builder.comboAttack;
        this.recoveryAction = builder.recoveryAction;
    }

	@Override
    protected Action<IStandPower> replaceAction(IStandPower power) {
	    StandEntityHeavyAttack attackWithCombo = getComboAttack(power);
	    if (attackWithCombo != this) {
	        return attackWithCombo.replaceAction(power);
	    }
	    
	    if (power.isActive()) {
	        StandEntity standEntity = (StandEntity) power.getStandManifestation();
	        return standEntity.getCurrentTask().map(task -> {
	            if (task.getPhase() == Phase.RECOVERY) {
	                StandEntityAction action = getRecoveryAction(power, standEntity, task);
	                if (action != null) {
	                    return action;
	                }
	            }
	            return this;
	        }).orElse(this);
	    }
	    
	    return this;
    }
	
	private StandEntityHeavyAttack getComboAttack(IStandPower power) {
        StandEntityComboHeavyAttack comboAttack = this.comboAttack.get();
        if (comboAttack != null) {
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
	
	protected StandEntityAction getRecoveryAction(IStandPower power, StandEntity standEntity, StandEntityTask task) {
	    return recoveryAction.get();
	}
	
	@Override
	protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
	    return phase == Phase.RECOVERY && recoveryAction.get() != null && recoveryAction.get() == newAction
	            || super.isCancelable(standPower, standEntity, newAction, phase);
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
        private Supplier<StandEntityComboHeavyAttack> comboAttack = () -> null;
        private Supplier<StandEntityAction> recoveryAction = () -> null;
    	
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
        
        public Builder setComboAttack(Supplier<StandEntityComboHeavyAttack> comboAttack) {
            this.comboAttack = comboAttack != null ? comboAttack : () -> null;
            return getThis();
        }
        
        public Builder setRecoveryAction(Supplier<StandEntityAction> recoveryAction) {
            this.recoveryAction = recoveryAction != null ? recoveryAction : () -> null;
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
