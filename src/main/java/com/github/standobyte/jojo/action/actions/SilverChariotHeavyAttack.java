package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.PunchType;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class SilverChariotHeavyAttack extends StandEntityHeavyAttack {

    public SilverChariotHeavyAttack(Builder builder) {
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
        return standEntity.willHeavyPunchCombo() ? 
                Math.max(super.getStandWindupTicks(standPower, standEntity) - MathHelper.ceil(getStandActionTicks(standPower, standEntity) / 2F), 0)
                : 0;
    }
    
    @Override
    public int getStandActionTicks(IStandPower standPower, StandEntity standEntity) {
        return standEntity.isHeavyComboPunching() ? 
                3
                : StandStatFormulas.getHeavyAttackWindup(standEntity.getAttackSpeed(), standEntity.getLastHeavyPunchCombo());
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (standEntity.isHeavyComboPunching()) {
            double reach = standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
            world.getEntities(standEntity, standEntity.getBoundingBox().inflate(reach, 0, reach), 
                    e -> !e.isSpectator() && e.isPickable() && standEntity.canHarm(e)).forEach(targetEntity -> {
                        Vector3d standLookVec = standEntity.getLookAngle();
                        Vector3d targetVec = targetEntity.position().subtract(standEntity.position()).normalize();
                        double cos = standLookVec.dot(targetVec);
                        if (cos > -0.5) {
                            standEntity.attackEntity(targetEntity, PunchType.HEAVY_COMBO, this, 1, attack -> {
                                if (cos < 0.5) {
                                    attack.damage(attack.getDamage() * 0.5F);
                                }
                                attack.addKnockback(1);
                            });
                        }
                    });
        }
    }
    
    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, int ticks) {
        super.onTaskSet(world, standEntity, standPower, phase, ticks);
        if (!standEntity.isHeavyComboPunching() && ticks > 0) {
            if (standEntity.isFollowingUser() && standEntity.getAttackSpeed() < 24) {
                LivingEntity user = standEntity.getUser();
                if (user != null) {
                    Vector3d vec = MathUtil.relativeCoordsToAbsolute(0, 0, 1.0, user.yRot);
                    standEntity.setPos(user.getX() + vec.x, 
                            standEntity.getY(), 
                            user.getZ() + vec.z);
                }
            }
            ((SilverChariotEntity) standEntity).setDashVec(standEntity.getLookAngle().scale(10D / (double) ticks));
        }
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (!standEntity.isHeavyComboPunching()) {
            float completion = standEntity.getCurrentTaskCompletion(1.0F);
            boolean lastTick = standEntity.getCurrentTask().getTicksLeft() <= 1;
            boolean moveForward = completion <= 0.5F;
            if (!world.isClientSide()) {
                if (moveForward) {
                    for (RayTraceResult rayTraceResult : JojoModUtil.rayTraceMultipleEntities(standEntity, 
                            standEntity.getAttributeValue(ForgeMod.REACH_DISTANCE.get()), 
                            standEntity.canTarget(), 0.25, standEntity.getPrecision())) {
                        standEntity.attackTarget(ActionTarget.fromRayTraceResult(rayTraceResult), PunchType.HEAVY_NO_COMBO, this, 1);
                    }
                }
                else if (!Vector3d.ZERO.equals(standEntity.getDeltaMovement())) {
                    standEntity.punch(PunchType.HEAVY_NO_COMBO, target, this);
                }
                if (lastTick && standEntity.isFollowingUser()) {
                    standEntity.retractStand(false);
                }
            }
            standEntity.setDeltaMovement(moveForward ? ((SilverChariotEntity) standEntity).getDashVec().scale(completion * 8) : Vector3d.ZERO);
        }
    }
    
    @Override
    public boolean useDeltaMovement(IStandPower standPower, StandEntity standEntity) {
        return !standEntity.isHeavyComboPunching();
    }
}
