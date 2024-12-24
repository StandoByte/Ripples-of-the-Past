package com.github.standobyte.jojo.action.stand;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.punch.StandBlockPunch;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.action.stand.punch.StandMissedPunch;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StandEntityLightAttack extends StandEntityAction implements IHasStandPunch {
    public static final StandPose STAND_POSE = new StandPose("attack") {
        @Override
        public StandActionAnimation getAnim(List<StandActionAnimation> variants, StandEntity standEntity) {
            return standEntity != null ? variants.get((standEntity.punchComboCount - 1) % variants.size()) : super.getAnim(variants, standEntity);
        }
    };
    
    private final Supplier<SoundEvent> punchSound;
    private final Supplier<SoundEvent> swingSound;
    
    public StandEntityLightAttack(StandEntityLightAttack.Builder builder) {
        super(builder);
        this.punchSound = builder.punchSound;
        this.swingSound = builder.swingSound;
    }
    
    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        return !stand.canAttackMelee() ? ActionConditionResult.NEGATIVE : super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        standEntity.addFinisherMeter(0.05F, StandEntity.FINISHER_NO_DECAY_TICKS);
        standEntity.punch(task, this, task.getTarget());
    }
    
    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return IHasStandPunch.super.punchEntity(stand, target, dmgSource)
                .damage(StandStatFormulas.getLightAttackDamage(stand.getAttackDamage()))
                .addKnockback(stand.guardCounter())
                .addFinisher(0.15F)
                .impactSound(punchSound);
    }
    
    @Override
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state, Direction face) {
        return IHasStandPunch.super.punchBlock(stand, pos, state, face)
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
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        IHasStandPunch.playPunchSwingSound(task, Phase.PERFORM, 5, this, standEntity);
    }
    
    @Override
    public void clPlayPunchSwingSound(StandEntity standEntity, SoundEvent sound) {
        standEntity.playSound(sound, 1.0F, 0.9F + standEntity.getRandom().nextFloat() * 0.2F, ClientUtil.getClientPlayer());

    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        int ticks = StandStatFormulas.getLightAttackWindup(speed, standEntity.getFinisherMeter(), 
                standEntity.guardCounter(), standEntity.getCurrentTaskAction() != this);
        return ticks;
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackRecovery(speed, standEntity.getFinisherMeter());
    }
    
    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        double speed = standEntity.getAttackSpeed();
        return StandStatFormulas.getLightAttackRecovery(speed, standEntity.getFinisherMeter())
                * (standEntity.isArmsOnlyMode() ? 2 : 4);
    }
    
    @Override
    public Stream<SoundEvent> getSounds(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        return task.getTarget().getType() != TargetType.ENTITY || standEntity.isArmsOnlyMode() || standEntity.getFinisherMeter() > 0
                ? null : super.getSounds(standEntity, standPower, phase, task);
    }
    
    @Override
    protected boolean isCancelable(IStandPower standPower, StandEntity standEntity, @Nullable StandEntityAction newAction, Phase phase) {
        return phase == Phase.RECOVERY || super.isCancelable(standPower, standEntity, newAction, phase);
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        if (standEntity.isArmsOnlyMode() && standEntity.swingingArm == Hand.OFF_HAND) {
            standEntity.setArmsOnlyMode(true, true);
        }
        standEntity.alternateHands();
        if (world.isClientSide()) {
            standEntity.punchComboCount++;
        }
    }
    
    @Override
    protected void onTaskStopped(World world, StandEntity standEntity, IStandPower standPower, StandEntityTask task, @Nullable StandEntityAction newAction) {
        if (world.isClientSide() && newAction != this) {
            standEntity.punchComboCount = 0;
        }
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
    public boolean noFinisherBarDecay() {
        return true;
    }
    
    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return true;
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
    
    
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityLightAttack.Builder>  {
        private Supplier<SoundEvent> punchSound = ModSounds.STAND_PUNCH_LIGHT;
        private Supplier<SoundEvent> swingSound = ModSounds.STAND_PUNCH_SWING;
        
        public Builder() {
            staminaCost(10F).standUserWalkSpeed(1.0F)
            .standOffsetFront().standOffsetFromUser(-0.75, 0.75)
            .standPose(STAND_POSE)
            .standAutoSummonMode(AutoSummonMode.MAIN_ARM)
            .partsRequired(StandPart.ARMS);
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
    
    public static class LightPunchInstance extends StandEntityPunch {

        public LightPunchInstance(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
            super(stand, target, dmgSource);
        }
        
        @Override
        protected boolean onAttack(StandEntity stand, Entity target, StandEntityDamageSource dmgSource, float damage) {
            // FIXME light punch clashes
//            if (!target.level.isClientSide() && target instanceof StandEntity) {
//                StandEntity targetStand = (StandEntity) target;
//                StandEntityAction opponentTask = targetStand.getCurrentTaskAction();
//                if (opponentTask instanceof StandEntityLightAttack) {
//                    StandEntityLightAttack opponentAttack = (StandEntityLightAttack) opponentTask;
//                    if (targetStand.getCurrentTaskPhase().get() == StandEntityAction.Phase.WINDUP
//                            && targetStand.canBlockOrParryFromAngle(dmgSource.getSourcePosition())) {
//                        // TODO slight knockback
//                        // TODO spark particles
//                        targetStand.stopTask(true);
//                        
//                        SoundEvent thisSound = this.getImpactSound();
//                        if (thisSound != null) {
//                            stand.playSound(thisSound, 1.0F, 1.0F, null, targetStand.getEyePosition(1));
//                        }
//                        
//                        SoundEvent opponentSound = opponentAttack.punchSound != null ? opponentAttack.punchSound.get() : null;
//                        if (opponentSound != null) {
//                            targetStand.playSound(opponentSound, 1.0F, 1.0F, null, stand.getEyePosition(1));
//                        }
//                        return true;
//                    }
//                }
//            }
            
            return super.onAttack(stand, target, dmgSource, damage);
        }
    }
}
