package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class SilverChariotDashAttack extends StandEntityHeavyAttack {

    public SilverChariotDashAttack(StandEntityHeavyAttack.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        if (stand instanceof SilverChariotEntity && !((SilverChariotEntity) stand).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public int getStandWindupTicks(IStandPower standPower, StandEntity standEntity) {
        return 0;
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return Math.max(StandStatFormulas.getHeavyAttackWindup(standEntity.getAttackSpeed(), standEntity.getLastHeavyFinisherValue()), 2);
    }
    
    @Override
    public void phaseTransition(World world, StandEntity standEntity, IStandPower standPower, 
            Phase from, Phase to, StandEntityTask task, int ticks) {
        super.phaseTransition(world, standEntity, standPower, from, to, task, ticks);
        if (to == Phase.PERFORM) {
            if (standEntity.isFollowingUser() && standEntity.getAttackSpeed() < 24) {
                LivingEntity user = standEntity.getUser();
                if (user != null) {
                    Vector3d vec = MathUtil.relativeCoordsToAbsolute(0, 0, 1.0, user.yRot);
                    standEntity.setPos(user.getX() + vec.x, 
                            standEntity.getY(), 
                            user.getZ() + vec.z);
                }
            }
            task.getAdditionalData().push(Vector3d.class, standEntity.getLookAngle().scale(10D / (double) ticks));
        }
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        float completion = task.getPhaseCompletion(1.0F);
        boolean lastTick = task.getTicksLeft() <= 1;
        boolean moveForward = completion <= 0.5F;
        if (moveForward) {
            for (RayTraceResult rayTraceResult : JojoModUtil.rayTraceMultipleEntities(standEntity, 
                    standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get()), 
                    standEntity.canTarget(), 0.25, standEntity.getPrecision())) {
                standEntity.punch(task, this, ActionTarget.fromRayTraceResult(rayTraceResult));
            }
        }
        else if (!Vector3d.ZERO.equals(standEntity.getDeltaMovement())) {
            standEntity.punch(task, this, task.getTarget());
        }
        if (!world.isClientSide() && lastTick && standEntity.isFollowingUser()) {
            standEntity.retractStand(false);
        }
        standEntity.setDeltaMovement(moveForward ? task.getAdditionalData().peek(Vector3d.class) : Vector3d.ZERO);
    }
    
    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        StandEntityPunch stab = super.punchEntity(stand, target, dmgSource);
        stab.impactSound(null);
        if (stand.getAttackSpeed() < 24) {
            boolean left = MathHelper.wrapDegrees(
                    stand.yRot - MathUtil.yRotDegFromVec(stab.target.position().subtract(stand.position())))
                    < 0;
            return stab
                    .addKnockback(1.5F)
                    .knockbackYRotDeg((60F + stand.getRandom().nextFloat() * 30F) * (left ? 1 : -1));
        }
        else {
            return stab
                    .addKnockback(0.25F)
                    .knockbackXRot(-90F)
                    .impactSound(null);
        }
    }
    
    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return false;
    }
    
    @Override
    public boolean isChainable(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    @Override
    protected boolean canBeQueued(IStandPower standPower, StandEntity standEntity) {
        return false;
    }

    protected boolean lastTargetCheck(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
        return false;
    }
    
    @Override
    protected boolean standMovesByItself(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
    
    @Override
    public ActionConditionResult checkStandTarget(ActionTarget target, StandEntity standEntity, IStandPower standPower) {
        return ActionConditionResult.NEGATIVE;
    }
}
