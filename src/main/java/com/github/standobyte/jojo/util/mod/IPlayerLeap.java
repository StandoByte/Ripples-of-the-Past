package com.github.standobyte.jojo.util.mod;

import net.minecraft.entity.Entity;

public interface IPlayerLeap {
    boolean isOnGround();
    boolean isDoingLeap();
    void setIsDoingLeap(boolean isDoingLeap);
    
    default void leapFlagTick() {
        if (isDoingLeap() && isOnGround()) {
            setIsDoingLeap(false);
        }
    }
    
    public static void onLeapFixWrongMovement(Entity entity) {
        if (entity instanceof IPlayerLeap) {
            ((IPlayerLeap) entity).setIsDoingLeap(true);
        }
    }
}
