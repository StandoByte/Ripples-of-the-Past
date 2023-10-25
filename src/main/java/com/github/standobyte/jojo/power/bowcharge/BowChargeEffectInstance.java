package com.github.standobyte.jojo.power.bowcharge;

import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

public class BowChargeEffectInstance<P extends IPower<P, T>, T extends IPowerType<P, T>> {
    private static final int CHARGE_TICKS = 20;
    private final LivingEntity user;
    private final P power;
    private int tick = 0;
    
    private boolean wasFullyCharged;
    private int arrowWait = -1;
    
    public BowChargeEffectInstance(LivingEntity user, P power) {
        this.user = user;
        this.power = power;
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
        if (arrowWait > -1) {
            arrowWait++;
        }
    }
    
    public boolean isBeingCharged() {
        return user.isUsingItem() && itemFits(user.getUseItem());
    }
    
    public void onStart() {}
    
    public boolean isFullyCharged() {
        return isBeingCharged() && user.getTicksUsingItem() >= CHARGE_TICKS;
    }
    
    public void onRelease(boolean fullyCharged) {
        this.wasFullyCharged = fullyCharged;
        if (fullyCharged) {
            this.arrowWait = 0;
        }
    }
    
    public void onArrowShot(AbstractArrowEntity arrow) {
        if (wasFullyCharged && arrowWait >= 0) {
            if (!power.getUser().level.isClientSide()) {
                modifyArrow(arrow);
            }
            wasFullyCharged = false;
            arrowWait = 5;
        }
    }
    
    protected void modifyArrow(AbstractArrowEntity arrow) {
        
    }
    
    public boolean shouldBeRemoved() {
        return !isBeingCharged() && (!wasFullyCharged || arrowWait >= 5);
    }
    
    public P getPower() {
        return power;
    }
    
    public static boolean itemFits(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof BowItem;
    }
}
