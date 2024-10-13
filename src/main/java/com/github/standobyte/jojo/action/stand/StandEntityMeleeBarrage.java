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
import com.github.standobyte.jojo.action.stand.punch.StandMissedPunch;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrBarrageHitSoundPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class StandEntityMeleeBarrage extends StandEntityAction implements IHasStandPunch {
    protected final Supplier<SoundEvent> hitSound;
    protected final Supplier<SoundEvent> swingSound;

    public StandEntityMeleeBarrage(StandEntityMeleeBarrage.Builder builder) {
        super(builder);
        this.hitSound = builder.hitSound;
        this.swingSound = builder.swingSound;
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
        if (barrageHits > 0) {
            standEntity.punch(task, this, task.getTarget());
            if (world.isClientSide()) {
                clTtickSwingSound(task.getTick(), standEntity);
            }
        }
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
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state, Direction face) {
        return IHasStandPunch.super.punchBlock(stand, pos, state, face).impactSound(hitSound);
    }
    
    @Override
    public StandMissedPunch punchMissed(StandEntity stand) {
        return IHasStandPunch.super.punchMissed(stand).swingSound(hitSound);
    }
    
    public SoundEvent getHitSound() {
        return hitSound == null ? null : hitSound.get();
    }
    
    protected void clTtickSwingSound(int tick, StandEntity standEntity) {
        SoundEvent swingSound = getPunchSwingSound();
        if (swingSound != null) {
            standEntity.playSound(swingSound, 0.25F, 
                    1.8F - (float) standEntity.getAttackDamage() * 0.05F + standEntity.getRandom().nextFloat() * 0.2F, 
                    ClientUtil.getClientPlayer());
        }
    }
    
    @Override
    public SoundEvent getPunchSwingSound() {
        return swingSound.get();
    }
    
    @Override
    public void playPunchImpactSound(IPunch punch, TargetType punchType, boolean canPlay, boolean playAlways) {
        StandEntity stand = punch.getStand();
        if (!stand.level.isClientSide()) {
            boolean playSound = canPlay && (playAlways || punch.playImpactSound());
            SoundEvent sound = punch.getImpactSound();
            Vector3d pos = punch.getImpactSoundPos();
            tickBarrageSound(playSound, sound, pos, stand);
        }
    }
    
    public static void tickBarrageSound(boolean playSound, SoundEvent sound, Vector3d soundPos, StandEntity stand) {
        if (!stand.level.isClientSide()) {
            PacketManager.sendToClientsTracking(
                    playSound && sound != null && soundPos != null ? 
                            new TrBarrageHitSoundPacket(stand.getId(), sound, soundPos)
                            : TrBarrageHitSoundPacket.noSound(stand.getId()), 
                            stand);
        }
    }
    
    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        double minOffset = Math.min(0.5, standEntity.getMaxEffectiveRange());
        double maxOffset = Math.min(1.5, standEntity.getMaxEffectiveRange());

        return front3dOffset(standPower, standEntity, task.getTarget(), minOffset, maxOffset)
                .orElse(super.getOffsetFromUser(standPower, standEntity, task));
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
    public boolean noFinisherBarDecay() {
        return true;
    }
    
    @Override
    public int getHoldDurationMax(IStandPower standPower) {
        LivingEntity user = standPower.getUser();
        if (user != null && user.hasEffect(ModStatusEffects.RESOLVE.get())) {
            return Integer.MAX_VALUE;
        }
        if (standPower.getStandManifestation() instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) standPower.getStandManifestation();
            return StandStatFormulas.getBarrageMaxDuration(standEntity.getDurability());
        }
        return 20;
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return standEntity.isArmsOnlyMode() ? 0 : StandStatFormulas.getBarrageRecovery(standEntity.getAttackSpeed());
    }
    
    @Override
    public boolean isFreeRecovery(IStandPower standPower, StandEntity standEntity) {
        if (standEntity.barrageClashOpponent().isPresent()) {
            return true;
        }
        
        LivingEntity user = standPower.getUser();
        return user != null && user.hasEffect(ModStatusEffects.RESOLVE.get());
    }
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityMeleeBarrage.Builder> {
        private Supplier<SoundEvent> hitSound = ModSounds.STAND_PUNCH_LIGHT;
        private Supplier<SoundEvent> swingSound = ModSounds.STAND_PUNCH_BARRAGE_SWING;
        
        public Builder() {
            super();
            standPose(StandPose.BARRAGE)
            .standAutoSummonMode(AutoSummonMode.ARMS).holdType().staminaCostTick(4F)
            .standUserWalkSpeed(0.15F).standOffsetFront()
            .partsRequired(StandPart.ARMS);
        }
        
        public Builder barrageHitSound(Supplier<SoundEvent> barrageHitSound) {
            this.hitSound = barrageHitSound != null ? barrageHitSound : () -> null;
            return getThis();
        }
        
        public Builder barrageSwingSound(Supplier<SoundEvent> barrageSwingSound) {
            this.swingSound = barrageSwingSound != null ? barrageSwingSound : () -> null;
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
            dmgSource.setStackKnockback();
            this
            .damage(StandStatFormulas.getBarrageHitDamage(stand.getAttackDamage(), stand.getPrecision()))
            .addFinisher(0.005F)
            .reduceKnockback(target instanceof StandEntity ? 0 : (float) stand.getAttackDamage() * 0.0075F);
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
            boolean resolve = stand.getUser() != null && stand.getUser().hasEffect(ModStatusEffects.RESOLVE.get());
            if (resolve) {
                reduceKnockback(0);
            }
            
            boolean hit = super.doHit(task);
            
            if (hit && resolve && target instanceof LivingEntity) {
                ((LivingEntity) target).getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setNoGravityFor(3));
                if (target instanceof MobEntity) {
                    MobEntity mob = ((MobEntity) target);
                    mob.getNavigation().stop();
                }
            }
            return hit;
        }

        @Override
        protected void afterAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, StandEntityTask task, boolean hurt, boolean killed) {
            if (dmgSource.getBarrageHitsCount() > 0) {
                addFinisher *= dmgSource.getBarrageHitsCount();
            }
            super.afterAttack(stand, target, dmgSource, task, hurt, killed);
        }
    }
}
