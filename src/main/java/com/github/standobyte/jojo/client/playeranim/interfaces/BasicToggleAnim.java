package com.github.standobyte.jojo.client.playeranim.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface BasicToggleAnim {
    
    boolean setAnimEnabled(PlayerEntity player, boolean enabled);
    
    public static class NoPlayerAnimator implements BasicToggleAnim {

        @Override
        public boolean setAnimEnabled(PlayerEntity player, boolean enabled) { return false; }
        
    }
    
}
