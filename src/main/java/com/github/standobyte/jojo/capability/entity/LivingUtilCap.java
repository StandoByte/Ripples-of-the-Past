package com.github.standobyte.jojo.capability.entity;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.HamonCharge;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class LivingUtilCap {
    private final LivingEntity entity;
    
    private IStandPower lastHurtByStand;
    private int lastHurtByStandTicks;
    
    private boolean reduceKnockback;
    private float futureKnockbackFactor;
    
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
    
    
    
    public void setHamonCharge(float hamonCharge, int chargeTicks, LivingEntity hamonUser, float manaSpent) {
        this.hamonCharge = new HamonCharge(hamonCharge, chargeTicks, hamonUser, manaSpent);
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
            hamonCharge.tick(entity, entity.level, entity.getBoundingBox().inflate(1.0D));
            if (entity.getRandom().nextInt(10) == 0) {
                HamonPowerType.createHamonSparkParticlesEmitter(entity, hamonCharge.getCharge() / 40F);
            }
        }
    }
}
