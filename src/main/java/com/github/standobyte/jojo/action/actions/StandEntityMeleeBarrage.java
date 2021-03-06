package com.github.standobyte.jojo.action.actions;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.stand.StandAttackProperties;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class StandEntityMeleeBarrage extends StandEntityAction {

    public StandEntityMeleeBarrage(StandEntityMeleeBarrage.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        int hitsThisTick = 0;
        int hitsPerSecond = StandStatFormulas.getBarrageHitsPerSecond(standEntity.getAttackSpeed());
        int extraTickSwings = hitsPerSecond / 20;
        for (int i = 0; i < extraTickSwings; i++) {
            swing(standEntity);
            hitsThisTick++;
        }
        hitsPerSecond -= extraTickSwings * 20;
        
        if (standEntity.barragePunchDelayed) {
            standEntity.barragePunchDelayed = false;
            hitsThisTick++;
        }
        else if (hitsPerSecond > 0) {
            double ticksInterval = 20D / hitsPerSecond;
            int intTicksInterval = (int) ticksInterval;
            if ((getStandActionTicks(userPower, standEntity) - task.getTick() + standEntity.barrageDelayedPunches) % intTicksInterval == 0) {
                if (!world.isClientSide()) {
                    double delayProb = ticksInterval - intTicksInterval;
                    if (standEntity.getRandom().nextDouble() < delayProb) {
                        standEntity.barragePunchDelayed = true;
                        standEntity.barrageDelayedPunches++;
                    }
                    else {
                        hitsThisTick++;
                    }
                }
                swing(standEntity);
            }
        }
        int barrageHits = hitsThisTick;
        standEntity.addBarrageParryCount(barrageHits);
        standEntity.punch(this, 
        		getPunch()
        		.doFirst((punch, stand, target) -> punch.get().barrageHits(barrageHits)), 
        		task.getTarget());
    }
    
    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, ActionTarget target) {
        if (standEntity.isArmsOnlyMode()) {
            return super.getOffsetFromUser(standPower, standEntity, target);
        }
        double maxVariation = standEntity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5 * standEntity.getStaminaCondition();
        Vector3d targetPos = target.getTargetPos(true);
        double offset = 0.5;
        if (targetPos == null) {
            return StandRelativeOffset.withXRot(0, Math.min(offset + maxVariation, standEntity.getMaxEffectiveRange()));
        }
        else {
            LivingEntity user = standEntity.getUser();
            double backAway = 0.5 + (target.getType() == TargetType.ENTITY ? 
                    target.getEntity().getBoundingBox().getXsize() / 2
                    : 0.5);
            double offsetToTarget = targetPos.subtract(user.position()).multiply(1, 0, 1).length() - backAway;
            offset = MathHelper.clamp(offsetToTarget, offset, offset + maxVariation);
            return StandRelativeOffset.withXRot(0, offset);
        }
    }
    
    private void swing(StandEntity standEntity) {
        if (standEntity.level.isClientSide()) {
            standEntity.swing(standEntity.alternateHands());
        }
    }
    
    @Override
	protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
    	if (standEntity.barrageClashOpponent().isPresent()) {
    		return true;
    	}
        if (phase == Phase.RECOVERY) {
            return newAction != null && newAction.canFollowUpBarrage();
        }
        else {
        	return super.isCancelable(standPower, standEntity, newAction, phase);
        }
    }
    
    @Override
    public void onClear(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction) {
    	if (newAction != this) {
    		standEntity.barrageClashStopped();
    	}
    }

    @Override
    public boolean cancelHeldOnGettingAttacked(IStandPower power, DamageSource dmgSource, float dmgAmount) {
        return dmgAmount >= 4F && "healthLink".equals(dmgSource.msgId);
    }
    
    @Override
    protected boolean lastTargetCheck(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
		return target.getType() == TargetType.ENTITY && standEntity.barrageClashOpponent().map(otherStand -> {
			return otherStand == target.getEntity();
		}).orElse(false)
				|| super.lastTargetCheck(target, standEntity, standPower);
    }
    
    @Override
    public boolean noComboDecay() {
        return true;
    }
    
    @Override
    public int getHoldDurationMax(IStandPower standPower) {
        LivingEntity user = standPower.getUser();
        if (user != null && user.hasEffect(ModEffects.RESOLVE.get())) {
            return Integer.MAX_VALUE;
        }
        if (standPower.getStandManifestation() instanceof StandEntity) {
            return StandStatFormulas.getBarrageMaxDuration(((StandEntity) standPower.getStandManifestation()).getDurability());
        }
        return 0;
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        LivingEntity user = standPower.getUser();
        if (user != null && user.hasEffect(ModEffects.RESOLVE.get())) {
            return 0;
        }
        return standEntity.isArmsOnlyMode() ? 0 : StandStatFormulas.getBarrageRecovery(standEntity.getSpeed());
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityMeleeBarrage.Builder> {
    	
    	public Builder() {
    		super();
    		standAutoSummonMode(AutoSummonMode.ARMS).holdType().staminaCostTick(3F)
            .standUserSlowDownFactor(0.3F).standOffsetFront()
            .targetPunchProperties((p, stand, target) -> {
            	StandAttackProperties punch = p.get();
            	return punch
            			.damage(StandStatFormulas.getBarrageHitDamage(stand.getAttackDamage(), stand.getPrecision()) * punch.getBarrageHits())
            			.addCombo(0.005F)
            			.reduceKnockback(0.1F)
            			.setPunchSound(ModSounds.STAND_BARRAGE_ATTACK.get());
            });
    	}
        
		@Override
		protected Builder getThis() {
			return this;
		}
    }
}
