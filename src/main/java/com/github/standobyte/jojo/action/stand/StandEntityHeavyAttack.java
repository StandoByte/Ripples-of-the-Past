package com.github.standobyte.jojo.action.stand;

import java.util.EnumSet;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class StandEntityHeavyAttack extends StandEntityAction implements IHasStandPunch {
	private final Supplier<StandEntityHeavyAttack> comboAttack;
    private final Supplier<? extends Action<IStandPower>> recoveryAction;
    boolean isCombo = false;
    private final Supplier<SoundEvent> punchSound;

    public StandEntityHeavyAttack(StandEntityHeavyAttack.Builder builder) {
        super(builder);
        this.comboAttack = builder.comboAttack;
        this.recoveryAction = builder.recoveryAction;
        this.punchSound = builder.punchSound;
    }

	@Override
    protected Action<IStandPower> replaceAction(IStandPower power) {
	    StandEntityHeavyAttack attackWithCombo = getComboAttack(power);
	    if (attackWithCombo != this) {
	        return attackWithCombo.replaceAction(power);
	    }
	    
//	    if (power.isActive()) {
//	        StandEntity standEntity = (StandEntity) power.getStandManifestation();
//	        return standEntity.getCurrentTask().map(task -> {
//	            if (task.getPhase() == Phase.RECOVERY) {
//	                StandEntityAction action = getRecoveryAction(power, standEntity, task);
//	                if (action != null) {
//	                    return action;
//	                }
//	            }
//	            return this;
//	        }).orElse(this);
//	    }
	    
	    return this;
    }
	
	private StandEntityHeavyAttack getComboAttack(IStandPower power) {
        StandEntityHeavyAttack comboAttack = this.comboAttack.get();
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
        standEntity.punch(task, this, task.getTarget());
    }
    
    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        double strength = stand.getAttackDamage();
        return IHasStandPunch.super.punchEntity(stand, target, dmgSource)
                .damage(StandStatFormulas.getHeavyAttackDamage(strength))
                .addKnockback(0.5F + (float) strength / 8)
                .setStandInvulTime(10)
                .setPunchSound(ModSounds.STAND_STRONG_ATTACK);
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
    
    @Override
    public boolean isUnlocked(IStandPower power) {
        return isCombo ? StandUtil.isComboUnlocked(power) : super.isUnlocked(power);
    }
    
    @Override
    protected boolean playsVoiceLineOnShift() {
        return isCombo || super.playsVoiceLineOnShift();
    }
    
    @Override
    public StandPose getStandPose(IStandPower standPower, StandEntity standEntity) {
        return isCombo ? StandPose.HEAVY_ATTACK_COMBO : super.getStandPose(standPower, standEntity);
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityHeavyAttack.Builder> {
        private Supplier<StandEntityHeavyAttack> comboAttack = () -> null;
        private Supplier<? extends Action<IStandPower>> recoveryAction = () -> null;
        private Supplier<SoundEvent> punchSound = () -> null;
    	
    	public Builder() {
            standPose(StandPose.HEAVY_ATTACK).staminaCost(50F)
            .standOffsetFromUser(-0.75, 0.75);
    	}
        
        public Builder setComboAttack(Supplier<StandEntityHeavyAttack> comboAttack) {
            if (this.comboAttack.get() == null && comboAttack != null && comboAttack.get() != null) {
                this.comboAttack = comboAttack;
                comboAttack.get().isCombo = true;
            }
            return getThis();
        }
        
        public Builder setRecoveryFollowUpAction(Supplier<? extends Action<IStandPower>> recoveryAction) {
            this.recoveryAction = recoveryAction != null ? recoveryAction : () -> null;
            return getThis();
        }
        
        public Builder punchSound(Supplier<SoundEvent> punchSound) {
            this.punchSound = punchSound != null ? punchSound : () -> null;
            return getThis();
        }

		@Override
		protected Builder getThis() {
			return this;
		}
    }
}
