package com.github.standobyte.jojo.action.stand;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.StandMissedPunch;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.KnockbackCollisionImpact;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class StandEntityHeavyAttack extends StandEntityAction implements IHasStandPunch {
    private final Supplier<? extends StandEntityHeavyAttack> finisherVariation;
    boolean isFinisher = false;
    
    private final Supplier<? extends StandEntityActionModifier> recoveryAction;
    
    private final Supplier<SoundEvent> punchSound;
    private final Supplier<SoundEvent> swingSound;

    public StandEntityHeavyAttack(StandEntityHeavyAttack.Builder builder) {
        super(builder);
        this.finisherVariation = builder.finisherVariation;
        this.recoveryAction = builder.recoveryAction;
        this.punchSound = builder.punchSound;
        this.swingSound = builder.swingSound;
    }

    @Override
    protected Action<IStandPower> replaceAction(IStandPower power, ActionTarget target) {
        StandEntity standEntity = power.isActive() ? (StandEntity) power.getStandManifestation() : null;
        
        StandEntityHeavyAttack finisherVariation = getFinisherVariationIfPresent(power, standEntity);
        if (finisherVariation != this) {
            return finisherVariation.replaceAction(power, target);
        }
        
        StandEntityActionModifier followUp = getRecoveryFollowup(power, standEntity);
        if (followUp != null && standEntity != null && standEntity.getCurrentTask().map(task -> {
            return task.getAction() == this && 
                    !task.getModifierActions().filter(action -> action == followUp).findAny().isPresent() &&
                    power.checkRequirements(followUp, new ObjectWrapper<>(task.getTarget()), true).isPositive();
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
            
            if (standEntity != null && (standEntity.getCurrentTaskAction() == finisherVariation || standEntity.willHeavyPunchBeFinisher())) {
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
            ((StandEntity) power.getStandManifestation()).setHeavyPunchFinisher();
        }
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        standEntity.alternateHands();
        if (!world.isClientSide()) {
            standEntity.addFinisherMeter(-0.51F, 0);
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
                .addKnockback(0.5F + (float) strength / (8 - stand.getLastHeavyFinisherValue() * 4))
                .setStandInvulTime(10)
                .impactSound(punchSound);
    }
    
    @Override
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state) {
        return new HeavyPunchBlockInstance(stand, pos, state)
                .impactSound(punchSound);
    }
    
    @Override
    public StandMissedPunch punchMissed(StandEntity stand) {
        return IHasStandPunch.super.punchMissed(stand).swingSound(punchSound);
    }
    
    @Override
    public SoundEvent getPunchSwingSound() {
        return swingSound.get();
    }
    
    @Override
    public void standTickWindup(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        IHasStandPunch.playPunchSwingSound(task, Phase.WINDUP, 3, this, standEntity);
    }
    
    @Override
    public void clPlayPunchSwingSound(StandEntity standEntity, SoundEvent sound) {
        standEntity.playSound(sound, 1.0F, 0.65F + standEntity.getRandom().nextFloat() * 0.2F, ClientUtil.getClientPlayer());
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackWindup(standEntity.getAttackSpeed(), standEntity.getFinisherMeter());
    }

    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackRecovery(standEntity.getAttackSpeed(), standEntity.getLastHeavyFinisherValue());
    }
    
    
    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return target.getType() == TargetType.ENTITY;
    }
    
    @Override
    public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        double minOffset = Math.min(0.5, standEntity.getMaxEffectiveRange());
        double maxOffset = Math.min(2, standEntity.getMaxRange());

        return front3dOffset(standPower, standEntity, task.getTarget(), minOffset, maxOffset)
                .orElse(super.getOffsetFromUser(standPower, standEntity, task));
    }
    
    @Override
    public boolean lockOnTargetPosition(IStandPower standPower, StandEntity standEntity, StandEntityTask curTask) {
        return false;
    }
    
    
    @Override
    public boolean noFinisherBarDecay() {
        return true;
    }
    
    @Override
    public boolean canFollowUpBarrage() {
        return true;
    }
    
    @Override
    public boolean isFreeRecovery(IStandPower standPower, StandEntity standEntity) {
        return isFinisher();
    }
    
    @Override
    protected boolean playsVoiceLineOnSneak() {
        return isFinisher || super.playsVoiceLineOnSneak();
    }
    
    @Override
    public StandPose getStandPose(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
        return isFinisher ? StandPose.HEAVY_ATTACK_FINISHER : super.getStandPose(standPower, standEntity, task);
    }
    
    @Override
    public boolean greenSelection(IStandPower power, ActionConditionResult conditionCheck) {
        return isFinisher && conditionCheck.isPositive();
    }
    
    public boolean isFinisher() {
        return isFinisher;
    }
    
    @Override
    public boolean isLegalInHud(IStandPower power) {
        return !isFinisher;
    }
    
    @Deprecated
    public boolean canBeParried() {
        return !isFinisher;
    }
    
//    @Override
//    public StandAction[] getExtraUnlockable() {
//        StandAction[] actions = new StandAction[2];
//        int i = 0;
//        if (finisherVariation.get() != null) {
//            actions[i++] = finisherVariation.get();
//        }
//        if (recoveryAction.get() != null) {
//            actions[i++] = recoveryAction.get();
//        }
//        actions = Arrays.copyOfRange(actions, 0, i);
//        for (int j = 0; j < i; j++) {
//            actions = ArrayUtils.addAll(actions, actions[j].getExtraUnlockable());
//        }
//        return actions;
//    }
    
    
    
    public static final float DEFAULT_STAMINA_COST = 50;
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityHeavyAttack.Builder> {
        private Supplier<? extends StandEntityHeavyAttack> finisherVariation = () -> null;
        private Supplier<? extends StandEntityActionModifier> recoveryAction = () -> null;
        private Supplier<SoundEvent> punchSound = ModSounds.STAND_PUNCH_HEAVY;
        private Supplier<SoundEvent> swingSound = ModSounds.STAND_PUNCH_HEAVY_SWING;
        
        public Builder() {
            standPose(StandPose.HEAVY_ATTACK).staminaCost(DEFAULT_STAMINA_COST)
            .standOffsetFromUser(-0.75, 0.75);
        }
        
        public Builder setFinisherVariation(Supplier<? extends StandEntityHeavyAttack> variation) {
            if (variation != null) {
                this.finisherVariation = variation;
                variation.get().isFinisher = true;
                addExtraUnlockable(this.finisherVariation);
            }
            return getThis();
        }
        
        public Builder setRecoveryFollowUpAction(Supplier<? extends StandEntityActionModifier> recoveryAction) {
            if (recoveryAction != null) {
                this.recoveryAction = recoveryAction;
                addExtraUnlockable(this.recoveryAction);
            }
            return getThis();
        }
        
        public Builder punchSound(Supplier<SoundEvent> punchSound) {
            this.punchSound = punchSound != null ? punchSound : () -> null;
            return getThis();
        }
        
        public Builder swingSound(Supplier<SoundEvent> swingSound) {
            this.swingSound = swingSound != null ? swingSound : () -> null;
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
            if (!stand.level.isClientSide() && hurt) {
                if (target instanceof StandEntity && !killed) {
                    StandEntity standTarget = (StandEntity) target;
                    if (standTarget.getCurrentTask().isPresent() && standTarget.getCurrentTaskAction().stopOnHeavyAttack(this)) {
                        standTarget.stopTaskWithRecovery();
                    }
                }
                
                KnockbackCollisionImpact.getHandler(target).ifPresent(
                        cap -> cap.onPunchSetKnockbackImpact(target.getDeltaMovement()));
            }
            super.afterAttack(stand, target, dmgSource, task, hurt, killed);
        }
    }
    
    // FIXME make Silver Chariot's attacks not use this
    public static class HeavyPunchBlockInstance extends StandBlockPunch {

        public HeavyPunchBlockInstance(StandEntity stand, BlockPos targetPos, BlockState blockState) {
            super(stand, targetPos, blockState);
        }

        @Override
        public boolean doHit(StandEntityTask task) {
            if (stand.level.isClientSide()) return false;
            super.doHit(task);
            
            HeavyPunchExplosion explosion = new HeavyPunchExplosion(stand.level, stand, 
                    null, null, 
                    blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 
                    (float) stand.getAttackDamage() / 4, false, 
                    JojoModUtil.breakingBlocksEnabled(stand.level) ? Explosion.Mode.BREAK : Explosion.Mode.NONE);
            if (!ForgeEventFactory.onExplosionStart(stand.level, explosion)) {
                explosion.explode();
                explosion.finalizeExplosion(true);
            }
            
            return targetHit;
        }
        
        @Override
        public boolean playImpactSound() {
            return true;
        }
        
        
        
        public static class HeavyPunchExplosion extends CustomExplosion {
            private final LivingEntity attacker;

            // FIXME limit the radius
            // FIXME set the proper damage source
            // FIXME wth is explosion context
            protected HeavyPunchExplosion(World pLevel, LivingEntity pSource, 
                    @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pDamageCalculator, 
                    double pToBlowX, double pToBlowY, double pToBlowZ, 
                    float pRadius, boolean pFire, Explosion.Mode pBlockInteraction) {
                super(pLevel, pSource, 
                        pDamageSource, pDamageCalculator, 
                        pToBlowX, pToBlowY, pToBlowZ, 
                        pRadius, pFire, pBlockInteraction);
                this.attacker = pSource;
            }
            
            // FIXME seemingly drops 1 less item (or just can't restore the block with CD)
            @Override
            protected void explodeBlocks() {
                if (JojoModUtil.breakingBlocksEnabled(level) && level instanceof ServerWorld) {
                    ServerWorld world = (ServerWorld) level;
                    List<BlockPos> toBlow = getToBlow();
                    MCUtil.destroyBlocksInBulk(toBlow, world, attacker, true);
                }
            }
            
            @Override
            protected void filterEntities(List<Entity> entities) {
                Iterator<Entity> iter = entities.iterator();
                while (iter.hasNext()) {
                    Entity entity = iter.next();
                    if (entity instanceof LivingEntity && !attacker.canAttack((LivingEntity) entity)) {
                        iter.remove();
                    }
                }
            }
            
            // FIXME the mobs' AI targets the stand
            @Override
            protected void hurtEntity(Entity entity, float damage, double knockback, Vector3d vecToEntityNorm) {
                super.hurtEntity(entity, damage, knockback, vecToEntityNorm);
            }
            
            // FIXME only 1 damage?
            @Override
            protected float calcDamage(double impact, double diameter) {
                return (float) ((impact * impact + impact) / 2.0D * 7.0D * diameter + 1.0D);
            }
            
            // FIXME explosion is barely exploding if punching smth like a stone
            @Override
            protected Set<BlockPos> calculateBlocksToBlow() {
                return super.calculateBlocksToBlow();
            }
            
            @Override
            protected void playSound() {}
            
            @Override
            protected void spawnParticles() {}
        }
        
    }
}
