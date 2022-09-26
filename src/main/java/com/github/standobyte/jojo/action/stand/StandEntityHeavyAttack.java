package com.github.standobyte.jojo.action.stand;

import java.util.EnumSet;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.StandMissedPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.util.Container;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StandEntityHeavyAttack extends StandEntityAction implements IHasStandPunch {
	private final Supplier<StandEntityHeavyAttack> finisherVariation;
    private final Supplier<StandEntityActionModifier> recoveryAction;
    boolean isFinisher = false;
    private final Supplier<SoundEvent> punchSound;

    public StandEntityHeavyAttack(StandEntityHeavyAttack.Builder builder) {
        super(builder);
        this.finisherVariation = builder.finisherVariation;
        this.recoveryAction = builder.recoveryAction;
        this.punchSound = builder.punchSound;
    }

	@Override
    protected Action<IStandPower> replaceAction(IStandPower power) {
	    StandEntity standEntity = power.isActive() ? (StandEntity) power.getStandManifestation() : null;
	    
	    StandEntityHeavyAttack finisherVariation = getFinisherVariationIfPresent(power, standEntity);
	    if (finisherVariation != this) {
	        return finisherVariation.replaceAction(power);
	    }
	    
	    StandEntityActionModifier followUp = getRecoveryFollowup(power, standEntity);
	    if (followUp != null && standEntity != null && standEntity.getCurrentTask().map(task -> {
	        return task.getAction() == this && 
	                !task.getModifierActions().filter(action -> action == followUp).findAny().isPresent() &&
	                power.checkRequirements(followUp, new Container<>(task.getTarget()), true).isPositive();
	    }).orElse(false)) {
	        return followUp;
	    };
	    
	    return this;
    }
	
	public StandEntityHeavyAttack getFinisherVariationIfPresent(IStandPower power, @Nullable StandEntity standEntity) {
        StandEntityHeavyAttack finisherVariation = getFinisherVariation();
        if (finisherVariation != null) {
            EnumSet<StandPart> missingParts = EnumSet.complementOf(power.getStandInstance().get().getAllParts());
            if (!missingParts.isEmpty()) {
                boolean canUseThis = true;
                for (StandPart missingPart : missingParts) {
                    if (finisherVariation.isPartRequired(missingPart)) {
                        return this;
                    }
                    if (this.isPartRequired(missingPart)) {
                        canUseThis = false;
                    }
                }
                if (!canUseThis) {
                    return finisherVariation;
                }
            }
            
            if (standEntity != null && (standEntity.getCurrentTaskAction() == finisherVariation || standEntity.willHeavyPunchCombo())) {
                return finisherVariation;
            }
        }
        return this;
	}
	
	@Nullable
	public StandEntityHeavyAttack getFinisherVariation() {
	    return finisherVariation.get();
	}
	
	@Nullable
	protected StandEntityActionModifier getRecoveryFollowup(IStandPower standPower, StandEntity standEntity) {
	    return recoveryAction.get();
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
        return new HeavyPunchInstance(stand, target, dmgSource)
                .damage(StandStatFormulas.getHeavyAttackDamage(strength))
                .addKnockback(0.5F + (float) strength / 8)
                .setStandInvulTime(10)
                .impactSound(punchSound);
    }
    
    @Override
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state) {
        return IHasStandPunch.super.punchBlock(stand, pos, state)
                .impactSound(punchSound);
    }
    
    @Override
    public StandMissedPunch punchMissed(StandEntity stand) {
        return IHasStandPunch.super.punchMissed(stand).swingSound(punchSound);
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
    protected boolean standKeepsTarget(ActionTarget target) {
        return target.getType() == TargetType.ENTITY;
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
        return isFinisher ? StandUtil.isComboUnlocked(power) : super.isUnlocked(power);
    }
    
    @Override
    protected boolean playsVoiceLineOnShift() {
        return isFinisher || super.playsVoiceLineOnShift();
    }
    
    @Override
    public StandPose getStandPose(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return isFinisher ? StandPose.HEAVY_ATTACK_COMBO : super.getStandPose(standPower, standEntity, task);
    }
    
    @Override
    public boolean greenSelection(IStandPower power, ActionConditionResult conditionCheck) {
        return isFinisher && conditionCheck.isPositive();
    }
    
    public boolean isFinisher() {
        return isFinisher;
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityHeavyAttack.Builder> {
        private Supplier<StandEntityHeavyAttack> finisherVariation = () -> null;
        private Supplier<StandEntityActionModifier> recoveryAction = () -> null;
        private Supplier<SoundEvent> punchSound = () -> null;
    	
    	public Builder() {
            standPose(StandPose.HEAVY_ATTACK).staminaCost(50F)
            .punchSound(ModSounds.STAND_PUNCH_HEAVY)
            .standOffsetFromUser(-0.75, 0.75);
    	}
        
        public Builder setFinisherVariation(Supplier<StandEntityHeavyAttack> variation) {
            if (this.finisherVariation.get() == null && variation != null && variation.get() != null) {
                this.finisherVariation = variation;
                variation.get().isFinisher = true;
            }
            return getThis();
        }
        
        public Builder setRecoveryFollowUpAction(Supplier<StandEntityActionModifier> recoveryAction) {
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
    
    
    
    public static class HeavyPunchInstance extends StandEntityPunch {

        public HeavyPunchInstance(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
            super(stand, target, dmgSource);
        }

        @Override
        protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {
            if (!stand.level.isClientSide() && target instanceof StandEntity && hurt && !killed) {
                StandEntity standTarget = (StandEntity) target;
                if (standTarget.getCurrentTask().isPresent() && standTarget.getCurrentTaskAction().stopOnHeavyAttack(this)) {
                    standTarget.stopTaskWithRecovery();
                }
            }
            super.afterAttack(stand, target, dmgSource, task, hurt, killed);
        }
    }
}
