package com.github.standobyte.jojo.client.playeranim.anim.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface BasicToggleAnim {
    
    boolean setAnimEnabled(PlayerEntity player, boolean enabled);
    
    public static class NoPlayerAnimator implements BasicToggleAnim {
        public static final BasicToggleAnim DUMMY = new NoPlayerAnimator();

        @Override
        public boolean setAnimEnabled(PlayerEntity player, boolean enabled) { return false; }
        
    }
    
}
