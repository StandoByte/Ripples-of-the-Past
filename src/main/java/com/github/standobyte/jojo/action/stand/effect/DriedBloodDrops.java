package com.github.standobyte.jojo.action.stand.effect;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;

public class DriedBloodDrops extends StandEffectInstance {
    private int disappearTicks = 0;

    public DriedBloodDrops(StandEffectType<?> effectType) {
        super(effectType);
    }

    @Override
    protected void start() {}

    @Override
    protected void tick() {
        if (!world.isClientSide()) {
            Entity target = getTarget();
            if (target != null) {
                ++disappearTicks;
                if (target.isInWaterOrBubble()) {
                    disappearTicks += 29;
                }
                else if (target.isInWaterOrRain()) {
                    ++disappearTicks;
                }
            }
            if (disappearTicks >= 6000) {
                remove();
            }
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

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        disappearTicks = nbt.getInt("BloodTicks");
    }

}
