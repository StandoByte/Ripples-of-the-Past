package com.github.standobyte.jojo.power.bowcharge;

import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

public class BowChargeEffectInstance<P extends IPower<P, T>, T extends IPowerType<P, T>> {
    private static final int CHARGE_TICKS = 20;
    private final LivingEntity user;
    private final P power;
    private final T powerType;
    private int tick = 0;
    
    public BowChargeEffectInstance(LivingEntity user, P power, T powerType) {
        this.user = user;
        this.power = power;
        this.powerType = powerType;
    }
    
    public float getProgress(float partialTick) {
        if (user.isUsingItem()) {
            if (itemFits(user.getUseItem())) {
                float time = user.getTicksUsingItem() + partialTick;
                return Math.min(time, CHARGE_TICKS) / CHARGE_TICKS;
            }
        }
        return -1F;
    }
    
    public int getTicksAfterFullCharge() {
        return tick - CHARGE_TICKS;
    }
    
    public void tick() {
        tick++;
    }
    
    public boolean isActive() {
        return user.isUsingItem() && itemFits(user.getUseItem());
    }
    
    public void onStart() {}
    
    public boolean isFullyCharged() {
        return isActive() && user.getTicksUsingItem() >= CHARGE_TICKS;
    }
    
    public void onRelease(boolean fullyCharged) {}
    
//    public void affectArrow(boolean fullyCharged) {}
    
    public T getPowerType() {
        return powerType;
    }
    
    public static boolean itemFits(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof BowItem;
    }
}
