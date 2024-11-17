package com.github.standobyte.jojo.util.mod;

import net.minecraft.entity.Entity;

public interface IPlayerLeap {
    boolean _isEntityOnGround();
    boolean isDoingLeap();
    void setIsDoingLeap(boolean isDoingLeap);
    
    default void leapFlagTick() {
        if (isDoingLeap() && _isEntityOnGround()) {
            setIsDoingLeap(false);
        }
    }
    
    public static void onLeapFixWrongMovement(Entity entity) {
        if (entity instanceof IPlayerLeap) {
            ((IPlayerLeap) entity).setIsDoingLeap(true);
        }
    }
}
