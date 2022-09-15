package com.github.standobyte.jojo.action.stand;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack.HeavyPunchInstance;
import com.github.standobyte.jojo.action.stand.punch.IPunch;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrBarrageHitSoundPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class StandEntityMeleeBarrage extends StandEntityAction implements IHasStandPunch {
    private final Supplier<SoundEvent> hitSound;

    public StandEntityMeleeBarrage(StandEntityMeleeBarrage.Builder builder) {
        super(builder);
        this.hitSound = builder.hitSound;
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
            hitsThisTick++;
        }
        hitsPerSecond -= extraTickSwings * 20;
        
        if (standEntity.barrageHandler.popDelayedHit()) {
            hitsThisTick++;
        }
        else if (hitsPerSecond > 0) {
            double ticksInterval = 20D / hitsPerSecond;
            int intTicksInterval = (int) ticksInterval;
            if ((getStandActionTicks(userPower, standEntity) - task.getTick() + standEntity.barrageHandler.getHitsDelayed()) % intTicksInterval == 0) {
                if (!world.isClientSide()) {
                    double delayProb = ticksInterval - intTicksInterval;
                    if (standEntity.getRandom().nextDouble() < delayProb) {
                        standEntity.barrageHandler.delayHit();
                    }
                    else {
                        hitsThisTick++;
                    }
                }
            }
        }
        int barrageHits = hitsThisTick;
        standEntity.setBarrageHitsThisTick(barrageHits);
        standEntity.punch(task, this, task.getTarget());
    }
    
    @Override
    public void phaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            Phase from, Phase to, StandEntityTask task, int ticks) {
        if (world.isClientSide()) {
            standEntity.getBarrageHitSoundsHandler().setIsBarraging(to == Phase.PERFORM);
        }
    }
    
    @Override
    public BarrageEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        BarrageEntityPunch punch = new BarrageEntityPunch(stand, target, dmgSource).barrageHits(stand, stand.barrageHits);
        punch.impactSound(hitSound);
        return punch;
    }
    
    @Override
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state) {
        return IHasStandPunch.super.punchBlock(stand, pos, state).impactSound(hitSound);
    }
    
    public SoundEvent getHitSound() {
        return hitSound == null ? null : hitSound.get();
    }
    
    @Override
    public void playPunchSound(IPunch punch, TargetType punchType, boolean canPlay, boolean playAlways) {
        StandEntity stand = punch.getStand();
        if (!stand.level.isClientSide()) {
            SoundEvent sound = punch.getSound();
            Vector3d pos = punch.getSoundPos();
            PacketManager.sendToClientsTracking(
                    sound != null && pos != null && canPlay && (playAlways || punch.targetWasHit()) ? 
                            new TrBarrageHitSoundPacket(stand.getId(), sound, pos)
                            : TrBarrageHitSoundPacket.noSound(stand.getId()), 
            stand);
        }
    }

    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        if (standEntity.isArmsOnlyMode()) {
            return super.getOffsetFromUser(standPower, standEntity, task);
        }
        double minOffset = 0.5;
        double maxOffset = minOffset + standEntity.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5 * standEntity.getStaminaCondition();
        return offsetToTarget(standPower, standEntity, task, 
                minOffset, maxOffset, 
                () -> ActionTarget.fromRayTraceResult(JojoModUtil.rayTrace(standPower.getUser(), maxOffset, standEntity::canHarm, 0.25, 0)))
                .orElse(StandRelativeOffset.withXRot(0, Math.min(maxOffset, standEntity.getMaxEffectiveRange())));
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
    protected void onTaskStopped(World world, StandEntity standEntity, IStandPower standPower, StandEntityTask task, @Nullable StandEntityAction newAction) {
    	if (!world.isClientSide() && newAction != this) {
    		standEntity.barrageClashStopped();
    	}
        if (world.isClientSide()) {
            standEntity.getBarrageHitSoundsHandler().setIsBarraging(false);
        }
    }

    @Override
    public boolean cancelHeldOnGettingAttacked(IStandPower power, DamageSource dmgSource, float dmgAmount) {
        return dmgAmount >= 4F && "healthLink".equals(dmgSource.msgId);
    }
    
    @Override
    public boolean stopOnHeavyAttack(HeavyPunchInstance punch) {
        return true;
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public ActionConditionResult checkStandTarget(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
        if (target.getType() == TargetType.ENTITY) {
            return ActionConditionResult.noMessage(standEntity.barrageClashOpponent().map(otherStand -> {
                return otherStand == target.getEntity();
            }).orElse(false));
        }
        return ActionConditionResult.NEGATIVE;
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
        return standEntity.isArmsOnlyMode() ? 0 : StandStatFormulas.getBarrageRecovery(standEntity.getSpeed());
    }
    
    @Override
    public boolean isFreeRecovery(IStandPower standPower, StandEntity standEntity) {
        LivingEntity user = standPower.getUser();
        return user != null && user.hasEffect(ModEffects.RESOLVE.get());
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityMeleeBarrage.Builder> {
        private Supplier<SoundEvent> hitSound = () -> null;
        
        public Builder() {
            super();
            standPose(StandPose.BARRAGE)
            .standAutoSummonMode(AutoSummonMode.ARMS).holdType().staminaCostTick(3F)
            .standUserSlowDownFactor(0.3F).standOffsetFront()
            .partsRequired(StandPart.ARMS);
        }
        
        public Builder barrageHitSound(Supplier<SoundEvent> barrageHitSound) {
            this.hitSound = barrageHitSound != null ? barrageHitSound : () -> null;
            return getThis();
        }
        
        @Override
        protected Builder getThis() {
            return this;
        }
    }
    
    

    public static class BarrageEntityPunch extends StandEntityPunch {
        private int barrageHits = 0;

        public BarrageEntityPunch(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
            super(stand, target, dmgSource);
            this
            .damage(StandStatFormulas.getBarrageHitDamage(stand.getAttackDamage(), stand.getPrecision()))
            .addCombo(0.005F)
            .reduceKnockback(0);
        }
        
        public BarrageEntityPunch barrageHits(StandEntity stand, int hits) {
            this.barrageHits = hits;
            damage(StandStatFormulas.getBarrageHitDamage(stand.getAttackDamage(), stand.getPrecision()) * hits);
            return this;
        }
        
        @Override
        public boolean doHit(StandEntityTask task) {
            if (stand.level.isClientSide()) return false;
            if (barrageHits > 0) {
                dmgSource.setBarrageHitsCount(barrageHits);
            }
            return super.doHit(task);
        }

        @Override
        protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {
            if (hurt && dmgSource.getBarrageHitsCount() > 0) {
                addCombo *= dmgSource.getBarrageHitsCount();
            }
            super.afterAttack(stand, target, dmgSource, task, hurt, killed);
        }
    }
}
