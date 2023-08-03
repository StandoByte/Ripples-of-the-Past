package com.github.standobyte.jojo.capability.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.AfterimageEntity;
import com.github.standobyte.jojo.entity.ai.LookAtEntityWithoutMovingGoal;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.potion.HamonSpreadEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;

public class LivingUtilCap {
    private final LivingEntity entity;
    
    private IStandPower lastHurtByStand;
    private int lastHurtByStandTicks;
    public float lastStandDamage;
    public int standInvulnerableTime;
    
    private int hurtThroughInvulTime;
    
    private boolean reduceKnockback;
    private float futureKnockbackFactor;
    @Nullable private Explosion latestExplosion;
    
    public boolean hasUsedTimeStopToday = false;
    private int noLerpTicks = 0;
    private int hurtTimeSaved;
    
    private float receivedHamonDamage = 0;
    @Nullable private UUID preHypnosisOwner = null;
    
    private final List<AfterimageEntity> afterimages = new ArrayList<>();
    private boolean usedZoomPunch = false;
    
    public LivingUtilCap(LivingEntity entity) {
        this.entity = entity;
    }
    
    public void tick() {
        lastHurtByStandTick();
        tickNoLerp();
        tickHurtAnim();
        tickDownHamonDamage();
        
        if (!entity.level.isClientSide()) {
            tickHypnosisProcess();
        }
        
        Iterator<AfterimageEntity> it = afterimages.iterator();
        while (it.hasNext()) {
            AfterimageEntity afterimage = it.next();
            if (!afterimage.isAlive()) {
                it.remove();
            }
        }
    }
    
    
    public float onStandAttack(float damage) {
        this.lastStandDamage = damage;
        return standInvulnerableTime > 0 ? Math.max(damage - lastStandDamage, 0) : damage;
    }
    
    public void setLastHurtByStand(IStandPower stand, float damage, int invulTicks) {
        this.lastHurtByStand = stand;
        this.lastHurtByStandTicks = 100;
        if (invulTicks > 0) {
            this.standInvulnerableTime = invulTicks;
        }
    }
    
    @Nullable
    public IStandPower getLastHurtByStand() {
        return lastHurtByStand;
    }
    
    public void lastHurtByStandTick() {
        if (lastHurtByStandTicks > 0 && --lastHurtByStandTicks == 0) {
            lastHurtByStand = null;
        }
        
        if (standInvulnerableTime > 0) --standInvulnerableTime;
        if (hurtThroughInvulTime > 0) --hurtThroughInvulTime;
    }
    
    public void setFutureKnockbackFactor(float factor) {
        this.futureKnockbackFactor = MathHelper.clamp(factor, 0, 1);
        this.reduceKnockback = true;
    }
    
    public boolean shouldReduceKnockback() {
        return reduceKnockback;
    }
    
    public float getKnockbackFactorOneTime() {
        reduceKnockback = false;
        return futureKnockbackFactor;
    }
    
    public void setLatestExplosion(Explosion explosion) {
        this.latestExplosion = explosion;
    }
    
    @Nullable
    public Explosion getSourceExplosion(DamageSource damageSource) {
        if (latestExplosion != null && damageSource == latestExplosion.getDamageSource()) {
            return latestExplosion;
        }
        return null;
    }
    
    public void onHurtThroughInvul(IModdedDamageSource dmgSource) {
        if (hurtThroughInvulTime > 0) {
            dmgSource.setPreventDamagingArmor();
        }
        hurtThroughInvulTime = 5;
    }
    
    
    
    public void addAfterimages(int count, int lifespan) {
        if (!entity.level.isClientSide()) {
            int i = 0;
            for (AfterimageEntity afterimage : afterimages) {
                if (afterimage.isAlive()) {
                    afterimage.setLifeSpan(lifespan < 0 ? Integer.MAX_VALUE : afterimage.tickCount + lifespan);
                    i++;
                }
            }
            double minSpeed = entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
            double speed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
            for (; i < count; i++) {
                AfterimageEntity afterimage = new AfterimageEntity(entity.level, entity, i + 1);
                afterimage.setLifeSpan(lifespan < 0 ? Integer.MAX_VALUE :lifespan);
                afterimage.setMinSpeed(minSpeed + (speed - minSpeed) * (double) (i + 1) / (double) count);
                afterimages.add(afterimage);
                entity.level.addFreshEntity(afterimage);
            }
        }
    }
    
    
    
    private static final float[] DAMAGE_FOR_HAMON_SPREAD_EFFECT = new float[] { 5, 12.5F, 25, 50, 125 };
    public void hamonSpread(float damageReceived) {
        receivedHamonDamage += damageReceived;
        JojoMod.LOGGER.debug("taken hamon damage = {}", receivedHamonDamage);
        for (int i = DAMAGE_FOR_HAMON_SPREAD_EFFECT.length - 1; i >= 0; i--) {
            if (receivedHamonDamage >= DAMAGE_FOR_HAMON_SPREAD_EFFECT[i]) {
                int duration = 60 + 40 * i;
                HamonSpreadEffect.giveEffectTo(entity, duration, i);
                break;
            }
        }
    }
    
    private void tickDownHamonDamage() {
        receivedHamonDamage = Math.max(receivedHamonDamage - 0.1F, 0);
    }
    
    
    
    public void onTracking(ServerPlayerEntity tracking) {
    }
    
    
    
    public void setNoLerpTicks(int ticks) {
        this.noLerpTicks = ticks;
    }
    
    private void tickNoLerp() {
        if (noLerpTicks > 0 && CommonReflection.getLerpSteps(entity) > 1) {
            CommonReflection.setLerpSteps(entity, 1);
            noLerpTicks--;
        }
    }
    
    
    
    private void tickHurtAnim() {
        if (!entity.canUpdate()) {
            if (entity.hurtTime > 0) {
                hurtTimeSaved = entity.hurtTime;
                entity.hurtTime = 0;
            }
        }
        else if (hurtTimeSaved > 0) {
            entity.hurtTime = hurtTimeSaved;
            hurtTimeSaved = 0;
        }
    }
    
    
    
    public void setUsingZoomPunch(boolean zoomPunch) {
        this.usedZoomPunch = zoomPunch;
    }
    
    public boolean isUsingZoomPunch() {
        return usedZoomPunch;
    }
    
    
    
    public static HypnosisTargetCheck canBeHypnotized(LivingEntity entity, LivingEntity hypnotizer) {
        if (hypnotizer instanceof PlayerEntity) {
            if (entity instanceof TameableEntity) {
                TameableEntity tameable = (TameableEntity) entity;
                return !hypnotizer.getUUID().equals(tameable.getOwnerUUID()) ? 
                        HypnosisTargetCheck.CORRECT : HypnosisTargetCheck.ALREADY_TAMED_BY_USER;
            }
            if (entity instanceof AbstractHorseEntity) {
                AbstractHorseEntity horse = (AbstractHorseEntity) entity;
                return !hypnotizer.getUUID().equals(horse.getOwnerUUID()) ? 
                        HypnosisTargetCheck.CORRECT : HypnosisTargetCheck.ALREADY_TAMED_BY_USER;
            }
        }
        return HypnosisTargetCheck.INVALID;
    }
    
    public static enum HypnosisTargetCheck {
        CORRECT,
        INVALID,
        ALREADY_TAMED_BY_USER
    }
    
    public void hypnotizeEntity(LivingEntity hypnotizer, int duration) {
        if (!entity.level.isClientSide()) {
            boolean giveEffect = false;
            
            if (hypnotizer instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) hypnotizer;
                if (entity instanceof TameableEntity) {
                    TameableEntity tameable = (TameableEntity) entity;
                    preHypnosisOwner = tameable.getOwnerUUID();
                    tameable.tame(player);
                    giveEffect = true;
                }
                
                else if (entity instanceof AbstractHorseEntity) {
                    AbstractHorseEntity horse = (AbstractHorseEntity) entity;
                    preHypnosisOwner = horse.getOwnerUUID();
                    horse.tameWithName(player);
                    giveEffect = true;
                }
                
                entity.level.broadcastEntityEvent(entity, (byte) 7); // spawn tame heart particles
            }
            
            if (giveEffect) {
                entity.addEffect(new EffectInstance(ModStatusEffects.HYPNOSIS.get(), duration, 0, false, false, true));
            }
        }
    }
    
    public void relieveHypnosis() {
        if (!entity.level.isClientSide()) {
            if (entity instanceof TameableEntity) {
                TameableEntity tameable = (TameableEntity) entity;
                tameable.setOrderedToSit(false);
                tameable.setTame(preHypnosisOwner != null);
                tameable.setOwnerUUID(preHypnosisOwner);
            }
            
            else if (entity instanceof AbstractHorseEntity) {
                AbstractHorseEntity horse = (AbstractHorseEntity) entity;
                horse.setTamed(preHypnosisOwner != null);
                horse.setOwnerUUID(preHypnosisOwner);
                horse.makeMad();
            }
            
            entity.level.broadcastEntityEvent(entity, (byte) 6); // spawn smoke particles
        }
    }
    
    private Goal lookAtHypnotizerGoal;
    private int resetLookGoalTicks = 0;
    public void startedHypnosisProcess(LivingEntity hypnotizer) {
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            lookAtHypnotizerGoal = new LookAtEntityWithoutMovingGoal(mob, hypnotizer);
            mob.goalSelector.addGoal(0, lookAtHypnotizerGoal);
            resetLookGoalTicks = 3;
        }
    }
    
    private void tickHypnosisProcess() {
        if (resetLookGoalTicks > 0 && --resetLookGoalTicks == 0
                && lookAtHypnotizerGoal != null && entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;
            mob.goalSelector.removeGoal(lookAtHypnotizerGoal);
            lookAtHypnotizerGoal = null;
        }
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putFloat("HamonSpread", receivedHamonDamage);
        nbt.putBoolean("UsedTimeStop", hasUsedTimeStopToday);
        if (preHypnosisOwner != null) {
            nbt.putUUID("PreHypnosisOwner", preHypnosisOwner);
        }
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        receivedHamonDamage = nbt.getFloat("HamonSpread");
        hasUsedTimeStopToday = nbt.getBoolean("UsedTimeStop");
        if (nbt.hasUUID("PreHypnosisOwner")) {
            preHypnosisOwner = nbt.getUUID("PreHypnosisOwner");
        }
    }
}
