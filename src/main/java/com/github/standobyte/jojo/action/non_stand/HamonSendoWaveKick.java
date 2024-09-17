package com.github.standobyte.jojo.action.non_stand;

import java.util.List;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
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

public class HamonSendoWaveKick extends HamonAction implements IPlayerAction<HamonSendoWaveKick.Instance, INonStandPower> {

    public HamonSendoWaveKick(HamonAction.Builder builder) {
        super(builder);
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
                user.setDeltaMovement(leap.x, leap.y * 0.5, leap.z);
            }
        }
    }
    
    private static boolean dealPhysicalDamage(LivingEntity user, Entity target) {
        return target.hurt(new EntityDamageSource(user instanceof PlayerEntity ? "player" : "mob", user), 
                DamageUtil.getDamageWithoutHeldItem(user));
    }
    
    public static AxisAlignedBB kickHitbox(LivingEntity user) {
        float xzAngle = -user.yRot * MathUtil.DEG_TO_RAD;
        Vector3d lookVec = new Vector3d(Math.sin(xzAngle), 0, Math.cos(xzAngle));
        Vector3d hitboxXZCenter = user.position().add(lookVec.scale(user.getBbWidth() * 0.75F));
        return new AxisAlignedBB(hitboxXZCenter, hitboxXZCenter)
                .inflate(user.getBbWidth() * 1.25F, 0.125, user.getBbWidth() * 1.25F)
                .expandTowards(0, user.getBbHeight() / 2, 0);
    }
    
    @Override
    public Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            ModPlayerAnimations.sendoWaveKick.setAnimEnabled((PlayerEntity) user, true);
        }
        return new Instance(user, userCap, power, this);
    }
    
    
    
    public static class Instance extends ContinuousActionInstance<Instance, INonStandPower> {
        private int sendoWaveKickPositionWaitingTimer = 0;
        private boolean gaveThisSendoWaveKickPoints = false;
        private float energySpent;
        private final float initialYRot;

        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, HamonSendoWaveKick action) {
            super(user, userCap, playerPower, action);
            this.initialYRot = user.yRot;
        }
        
        // FIXME ! (hamon 2) set spent energy points to give hamon strength points
        public void setEnergySpent(float energy) {
            this.energySpent = energy;
        }

        @Override
        protected Instance getThis() {
            return this;
        }
        
        public float getInitialYRot() {
            return initialYRot;
        }
        
        @Override
        public boolean cancelIncomingDamage(DamageSource dmgSource, float dmgAmount) {
            return DamageUtil.isMeleeAttack(dmgSource);
        }

        private static final int USUAL_SENDO_WAVE_KICK_DURATION = 10;
        @Override
        public void playerTick() {
            LivingEntity user = getUser();
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
                
                List<LivingEntity> targets = user.level.getEntitiesOfClass(LivingEntity.class, kickHitbox(user), 
                        entity -> !entity.is(user) && user.canAttack(entity));
                boolean points = false;
                for (LivingEntity target : targets) {
                    boolean kickDamage = dealPhysicalDamage(user, target);
                    boolean hamonDamage = DamageUtil.dealHamonDamage(target, 2.5F, user, null);
                    if (kickDamage || hamonDamage) {
                        Vector3d vecToTarget = target.position().subtract(user.position());
                        boolean left = MathHelper.wrapDegrees(
                                user.yBodyRot - MathUtil.yRotDegFromVec(vecToTarget))
                                < 0;
                        float knockbackYRot = (60F + user.getRandom().nextFloat() * 30F) * (left ? 1 : -1);
                        knockbackYRot += (float) -MathHelper.atan2(vecToTarget.x, vecToTarget.z) * MathUtil.RAD_TO_DEG;
                        DamageUtil.knockback((LivingEntity) target, 0.75F, knockbackYRot);
                        
                        if (hamonDamage) {
                            points = true;
                        }
                    }
                }

                if (!gaveThisSendoWaveKickPoints && points) {
                    INonStandPower.getNonStandPowerOptional(user).ifPresent(power -> {
                        power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                            hamon.hamonPointsFromAction(HamonStat.STRENGTH, energySpent); 
                        });
                    });
                    gaveThisSendoWaveKickPoints = true;
                }
            }
            
            else {
                HamonSparksLoopSound.playSparkSound(user, new Vector3d(user.getX(), user.getY(0.25), user.getZ()), 1.0F, true);
            }
            
            user.fallDistance = 0;
        }
        
        @Override
        public boolean stopAction() {
            if (super.stopAction()) {
                if (user.level.isClientSide() && user instanceof PlayerEntity) {
                    ModPlayerAnimations.sendoWaveKick.setAnimEnabled((PlayerEntity) user, false);
                }
                return true;
            }
            
            return false;
        }
        
    }
}

