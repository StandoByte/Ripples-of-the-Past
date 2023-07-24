package com.github.standobyte.jojo.capability.entity.hamonutil;

import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonCharge;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public class EntityHamonChargeCap {
    private static final Random RANDOM = new Random();
    private final Entity entity;
    private HamonCharge hamonCharge;
    
    public EntityHamonChargeCap(Entity entity) {
        this.entity = entity;
    }
    
    public void tick() {
        hamonChargeTick();
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
            if (RANDOM.nextInt(10) == 0) {
                HamonUtil.createHamonSparkParticlesEmitter(entity, hamonCharge.getTickDamage() / 40F);
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
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (hamonCharge != null) {
            nbt.put("HamonCharge", hamonCharge.toNBT());
        }
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("HamonCharge", 10)) {
            hamonCharge = HamonCharge.fromNBT(nbt.getCompound("HamonCharge"));
        }
    }
}
