package com.github.standobyte.jojo.client.playeranim.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface WallClimbAnim extends BasicToggleAnim {
    
    public void setAnimSpeed(PlayerEntity player, float speed);
    
    public static class NoPlayerAnimator extends BasicToggleAnim.NoPlayerAnimator implements WallClimbAnim {

        @Override
        public void setAnimSpeed(PlayerEntity player, float speed) {}
    }
}
