package com.github.standobyte.jojo.capability.entity;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.HamonCharge;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LivingUtilCap {
    private final LivingEntity entity;
    
    private IStandPower lastHurtByStand;
    private int lastHurtByStandTicks;
    
    private boolean reduceKnockback;
    private float futureKnockbackFactor;
    private Vector3d latestExplosionPos = null;
    
    HamonCharge hamonCharge;
    
    public LivingUtilCap(LivingEntity entity) {
        this.entity = entity;
    }
    
    public void tick() {
        lastHurtByStandTick();
        hamonChargeTick();
    }
    
    
    
    public void setLastHurtByStand(IStandPower stand) {
        this.lastHurtByStand = stand;
        this.lastHurtByStandTicks = 100;
    }
    
    @Nullable
    public IStandPower getLastHurtByStand() {
        return lastHurtByStand;
    }
    
    private void lastHurtByStandTick() {
        if (lastHurtByStandTicks > 0) {
            lastHurtByStandTicks--;
            if (lastHurtByStandTicks == 0) {
                lastHurtByStand = null;
            }
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
}
