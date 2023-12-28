package com.github.standobyte.jojo.action.stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class MagiciansRedKick extends StandEntityHeavyAttack {

    public MagiciansRedKick(StandEntityHeavyAttack.Builder builder) {
        super(builder);
    }

    @Override
    public ActionTarget targetBeforePerform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (power.isActive() && power.getStandManifestation() instanceof StandEntity) {
            StandEntity standEntity = (StandEntity) power.getStandManifestation();
            Optional<LivingEntity> bound = MagiciansRedRedBind.getLandedRedBind(standEntity).map(MRRedBindEntity::getEntityAttachedTo);
            if (bound.isPresent()) {
                return new ActionTarget(bound.get());
            }
        }
        return super.targetBeforePerform(world, user, power, target);
    }

    @Override
    protected void setAction(IStandPower standPower, StandEntity standEntity, int ticks, Phase phase, ActionTarget target) {
        MagiciansRedRedBind.getLandedRedBind(standEntity).ifPresent(redBind -> {
            redBind.setKickAttack();
        });
        super.setAction(standPower, standEntity, ticks, phase, target);
    }
    
    private static final double SLIDE_DISTANCE = 3;
    @Override
    public void standTickWindup(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        int ticksLeft = task.getTicksLeft();
        if (ticksLeft == 2) {
            Vector3d targetPos = task.getTarget().getTargetPos(true);
            Vector3d slideVec;
            if (targetPos != null) {
                slideVec = targetPos.subtract(standEntity.getEyePosition(1.0F));
                slideVec = slideVec.normalize().scale(MathHelper.clamp(slideVec.length() - standEntity.getBbWidth(), 0, SLIDE_DISTANCE));
            }
            else {
                slideVec = standEntity.getLookAngle().scale(SLIDE_DISTANCE);
            }
            standEntity.setDeltaMovement(slideVec);
        }
        else if (ticksLeft == 1) {
            standEntity.setDeltaMovement(Vector3d.ZERO);
            
            if (!world.isClientSide()) {
                MagiciansRedRedBind.getLandedRedBind(standEntity).ifPresent(redBind -> {
                    if (redBind.isInKickAttack()) {
                        redBind.remove();
                    }
                });
            }
        }
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return super.punchEntity(stand, target, dmgSource)
                .multiplyAddKnockback(1.2F);
    }
    
    @Override
    protected boolean standMovesByItself(IStandPower standPower, StandEntity standEntity) {
        Phase phase = standEntity.getCurrentTaskPhase().get();
        return phase == Phase.WINDUP && standEntity.getCurrentTask().map(StandEntityTask::getTicksLeft).get() <= 2
                || phase == Phase.PERFORM || phase == Phase.RECOVERY;
    }
    
    @Override
    public String getTranslationKey(IStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        if (power.isActive() && MagiciansRedRedBind.getLandedRedBind((StandEntity) power.getStandManifestation()).isPresent()) {
            key += "_bind";
        }
        return key;
    }
    
    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        if (power.isActive() && MagiciansRedRedBind.getLandedRedBind((StandEntity) power.getStandManifestation()).isPresent()) {
            return ModSounds.AVDOL_HELL_2_U.get();
        }
        return super.getShout(user, power, target, wasActive);
    }
}
