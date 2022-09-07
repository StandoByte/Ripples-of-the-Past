package com.github.standobyte.jojo.action.stand.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

public class DriedBloodDrops extends StandEffectInstance {
    private int disappearTicks = 0;

    public DriedBloodDrops(StandEffectType<?> effectType) {
        super(effectType);
    }

    @Override
    protected void start() {}

    @Override
    protected void tickTarget(LivingEntity target) {
        if (target.isInWaterOrBubble()) {
            disappearTicks += 29;
        }
        else if (target.isInWaterOrRain()) {
            disappearTicks++;
        }
    }

    @Override
    protected void tick() {
        if (!world.isClientSide() && disappearTicks >= 6000) {
            remove();
        }
    }
    
    public void resetTicks() {
        disappearTicks = 0;
    }

    @Override
    protected void stop() {}
    
    @Override
    protected boolean needsTarget() {
        return true;
    }
    
    @Override
    public boolean removeOnUserLogout() {
        return false;
    }

    @Override
    protected void writeAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("BloodTicks", disappearTicks);
    }

    protected void readAdditionalSaveData(CompoundNBT nbt) {
        disappearTicks = nbt.getInt("BloodTicks");
    }

}
