package com.github.standobyte.jojo.action.non_stand;

import java.util.List;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanBladeDashAttack extends PillarmanAction implements IPlayerAction<PillarmanBladeDashAttack.PillarmanBladeDashInstance, INonStandPower> {

    public PillarmanBladeDashAttack(PillarmanAction.Builder builder) {
        super(builder);
        mode = Mode.LIGHT;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        return ActionConditionResult.noMessage(user.isOnGround());
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user instanceof PlayerEntity) {
            if (!user.level.isClientSide()) {
                user.setOnGround(false);
                setPlayerAction(user, power);
            }
            else {
                user.setOnGround(false);
                user.hasImpulse = true;
                Vector3d leap = Vector3d.directionFromRotation(MathHelper.clamp(user.xRot, -45F, -18F), user.yRot)
                        .scale(1 + user.getAttributeValue(Attributes.MOVEMENT_SPEED) * 5);
                user.setDeltaMovement(leap.x, leap.y * 0.25, leap.z);
            }
        }
    }

    private static final int USUAL_SENDO_WAVE_KICK_DURATION = 10;
    @Override
    public void playerTick(PillarmanBladeDashInstance kick) {
        LivingEntity user = kick.getUser();
        if (!user.level.isClientSide()) {
            if (kick.sendoWaveKickPositionWaitingTimer >= 0) {
                // FIXME ! (hamon 2) check if the client sent position
                boolean clientSentPosition = true;
                if (clientSentPosition) {
                    kick.sendoWaveKickPositionWaitingTimer = -1;
                }
                else {
                    kick.sendoWaveKickPositionWaitingTimer++;
                }
            }
            if (kick.sendoWaveKickPositionWaitingTimer < 0 && user.isOnGround()
                    || kick.sendoWaveKickPositionWaitingTimer >= USUAL_SENDO_WAVE_KICK_DURATION) {
                kick.stopAction();
                return;
            }
            
            List<LivingEntity> targets = user.level.getEntitiesOfClass(LivingEntity.class, HamonSendoWaveKick.kickHitbox(user), 
                    entity -> !entity.is(user) && user.canAttack(entity));
            for (LivingEntity target : targets) {
                boolean kickDamage = dealPhysicalDamage(user, target);
                if (kickDamage) {
                    Vector3d vecToTarget = target.position().subtract(user.position());
                    boolean left = MathHelper.wrapDegrees(
                            user.yBodyRot - MathUtil.yRotDegFromVec(vecToTarget))
                            < 0;
                    float knockbackYRot = (60F + user.getRandom().nextFloat() * 30F) * (left ? 1 : -1);
                    knockbackYRot += (float) -MathHelper.atan2(vecToTarget.x, vecToTarget.z) * MathUtil.RAD_TO_DEG;
                    DamageUtil.knockback((LivingEntity) target, 0.75F, knockbackYRot);
                }
            }
        }
        user.fallDistance = 0;
    }
    
    private static boolean dealPhysicalDamage(LivingEntity user, Entity target) {
        return target.hurt(new EntityDamageSource(user instanceof PlayerEntity ? "player" : "mob", user), 
                DamageUtil.getDamageWithoutHeldItem(user) + 4.0F);
    }
    
    @Override
    public PillarmanBladeDashInstance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        return new PillarmanBladeDashInstance(user, userCap, power, this);
    }
    
    
    
    public static class PillarmanBladeDashInstance extends ContinuousActionInstance<PillarmanBladeDashInstance, INonStandPower> {
        private int sendoWaveKickPositionWaitingTimer = 0;
        private final float initialYRot;

        public PillarmanBladeDashInstance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, PillarmanBladeDashAttack action) {
            super(user, userCap, playerPower, action);
            this.initialYRot = user.yRot;
        }
        
        @Override
        protected PillarmanBladeDashInstance getThis() {
            return this;
        }
        
        public float getInitialYRot() {
            return initialYRot;
        }
    }
}

