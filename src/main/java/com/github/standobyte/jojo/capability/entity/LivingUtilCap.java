package com.github.standobyte.jojo.capability.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.AfterimageEntity;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonCharge;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.IModdedDamageSource;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
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
    
    private HamonCharge hamonCharge;
    private float receivedHamonDamage = 0;
    private final List<AfterimageEntity> afterimages = new ArrayList<>();
    public boolean hasUsedTimeStopToday = false;
    private int noLerpTicks = 0;
    private int hurtTimeSaved;
    
    private boolean usedZoomPunch = false;
    
    public LivingUtilCap(LivingEntity entity) {
        this.entity = entity;
    }
    
    public void tick() {
        lastHurtByStandTick();
        hamonChargeTick();
        tickNoLerp();
        tickHurtAnim();
        tickDownHamonDamage();
        
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
            dmgSource.preventDamagingArmor();
        }
        hurtThroughInvulTime = 5;
    }
    
    
    
    public void setHamonCharge(float tickDamage, int chargeTicks, LivingEntity hamonUser, float energySpent) {
        this.hamonCharge = new HamonCharge(tickDamage, chargeTicks, hamonUser, energySpent);
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
                HamonPowerType.createHamonSparkParticlesEmitter(entity, hamonCharge.getTickDamage() / 40F);
            }
        }
    }
    
    public boolean hasHamonCharge() {
        return hamonCharge != null && !hamonCharge.shouldBeRemoved();
    }
    
    @Nullable
    public HamonCharge getHamonCharge() {
        return hasHamonCharge() ? hamonCharge : null;
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
    
    
    
    // FIXME !!! (hamon) hamon spread perk rework
    public void hamonSpread(float damageReceived) {
        receivedHamonDamage += damageReceived;
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
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (hamonCharge != null) {
            nbt.put("HamonCharge", hamonCharge.writeNBT());
        }
        nbt.putFloat("HamonSpread", receivedHamonDamage);
        nbt.putBoolean("UsedTimeStop", hasUsedTimeStopToday);
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("HamonCharge", 10)) {
            hamonCharge = new HamonCharge(nbt.getCompound("HamonCharge"));
        }
        receivedHamonDamage = nbt.getFloat("HamonSpread");
        hasUsedTimeStopToday = nbt.getBoolean("UsedTimeStop");
    }
}
