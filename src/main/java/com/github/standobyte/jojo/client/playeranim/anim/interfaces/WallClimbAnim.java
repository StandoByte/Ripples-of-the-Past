package com.github.standobyte.jojo.client.playeranim.anim.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface WallClimbAnim extends BasicToggleAnim {
    
    public void tickAnimProperties(PlayerEntity player, boolean isMoving, 
            double movementUp, double movementLeft, float speed);
    
    public static class NoPlayerAnimator extends BasicToggleAnim.NoPlayerAnimator implements WallClimbAnim {

        @Override
        public void tickAnimProperties(PlayerEntity player, boolean isMoving, 
                double movementUp, double movementLeft, float speed) {}
    }
}
