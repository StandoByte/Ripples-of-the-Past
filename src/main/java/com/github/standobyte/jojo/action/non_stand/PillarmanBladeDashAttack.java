package com.github.standobyte.jojo.action.non_stand;

import java.util.List;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanBladeDashAttack extends PillarmanAction implements IPlayerAction<PillarmanBladeDashAttack.Instance, INonStandPower> {

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
                user.setDeltaMovement(leap.x, leap.y * 0.375, leap.z);
            }
        }
    }
    
    @Override
    public Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
    	if (user.level.isClientSide() && user instanceof PlayerEntity) {
            ModPlayerAnimations.bladeDash.setAnimEnabled((PlayerEntity) user, true);
        }
        return new Instance(user, userCap, power, this);
    }
    
    
    
    public static class Instance extends ContinuousActionInstance<PillarmanBladeDashAttack, INonStandPower> {
        private int sendoWaveKickPositionWaitingTimer = 0;
        private final float initialYRot;

        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, PillarmanBladeDashAttack action) {
            super(user, userCap, playerPower, action);
            this.initialYRot = user.yRot;
        }
        
        private static final int USUAL_SENDO_WAVE_KICK_DURATION = 10;
        @Override
        protected void playerTick() {
            if (!user.level.isClientSide()) {
                if (sendoWaveKickPositionWaitingTimer >= 0) {
                    // FIXME ! (hamon 2) check if the client sent position
                    boolean clientSentPosition = true;
                    if (clientSentPosition) {
                        sendoWaveKickPositionWaitingTimer = -1;
                    }
                    else {
                        sendoWaveKickPositionWaitingTimer++;
                    }
                }
                if (sendoWaveKickPositionWaitingTimer < 0 && user.isOnGround()
                        || sendoWaveKickPositionWaitingTimer >= USUAL_SENDO_WAVE_KICK_DURATION) {
                    stopAction();
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
                    DamageUtil.getDamageWithoutHeldItem(user) + 0.5F);
        }
        
        public float getInitialYRot() {
            return initialYRot;
        }
        
        @Override
        public boolean cancelIncomingDamage(DamageSource dmgSource, float dmgAmount) {
            return DamageUtil.isMeleeAttack(dmgSource);
        }
        
        @Override
        public void onStop() {
            super.onStop();
            if (user.level.isClientSide() && user instanceof PlayerEntity) {
                ModPlayerAnimations.bladeDash.setAnimEnabled((PlayerEntity) user, false);
            }
        }
    }
}

