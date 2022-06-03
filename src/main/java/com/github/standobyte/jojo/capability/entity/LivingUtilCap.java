package com.github.standobyte.jojo.capability.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.AfterimageEntity;
import com.github.standobyte.jojo.power.nonstand.type.HamonCharge;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.reflection.CommonReflection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LivingUtilCap {
    private final LivingEntity entity;
    
    private IStandPower lastHurtByStand;
    private int lastHurtByStandTicks;
    public float lastStandDamage;
    public int standInvulnerableTime;
    
    private boolean reduceKnockback;
    private float futureKnockbackFactor;
    private Vector3d latestExplosionPos = null;
    
    HamonCharge hamonCharge;
    
    private final List<AfterimageEntity> afterimages = new ArrayList<>();
    
    private int noLerpTicks = 0;
    
    public LivingUtilCap(LivingEntity entity) {
        this.entity = entity;
    }
    
    public void tick() {
        lastHurtByStandTick();
        hamonChargeTick();
        tickNoLerp();
        
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
        if (lastHurtByStandTicks > 0) {
            lastHurtByStandTicks--;
            if (lastHurtByStandTicks == 0) {
                lastHurtByStand = null;
            }
        }
        if (standInvulnerableTime > 0) {
            standInvulnerableTime--;
        }
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
    
    public void setLatestExplosionPos(Vector3d pos) {
        this.latestExplosionPos = pos;
    }
    
    public Vector3d popLatestExplosionPos() {
        Vector3d pos = this.latestExplosionPos;
        this.latestExplosionPos = null;
        return pos;
    }
    
    
    
    public void setHamonCharge(float hamonCharge, int chargeTicks, LivingEntity hamonUser, float energySpent) {
        this.hamonCharge = new HamonCharge(hamonCharge, chargeTicks, hamonUser, energySpent);
    }
    
    private void hamonChargeTick() {
        if (!entity.level.isClientSide()) {
            if (hamonCharge == null) {
                return;
            }
            if (hamonCharge.shouldBeRemoved()) {
                hamonCharge = null;
                return;
            }
            hamonCharge.tick(entity, null, entity.level, entity.getBoundingBox().inflate(1.0D));
            if (entity.getRandom().nextInt(10) == 0) {
                HamonPowerType.createHamonSparkParticlesEmitter(entity, hamonCharge.getCharge() / 40F);
            }
        }
    }
    
    public boolean hasHamonCharge() {
        return hamonCharge != null && !hamonCharge.shouldBeRemoved();
    }
    
    
    
    public void addAfterimages(int count, int lifespan) {
        if (!entity.level.isClientSide()) {
            int i = 0;
            for (AfterimageEntity afterimage : afterimages) {
                if (afterimage.isAlive()) {
                    afterimage.setLifeSpan(afterimage.tickCount + lifespan);
                    i++;
                }
            }
            double minSpeed = entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
            double speed = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
            for (; i < count; i++) {
                AfterimageEntity afterimage = new AfterimageEntity(entity.level, entity, i);
                afterimage.setLifeSpan(lifespan);
                afterimage.setMinSpeed(minSpeed + (speed - minSpeed) * (double) (i + 1) / (double) count);
                afterimages.add(afterimage);
                entity.level.addFreshEntity(afterimage);
            }
        }
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
}
