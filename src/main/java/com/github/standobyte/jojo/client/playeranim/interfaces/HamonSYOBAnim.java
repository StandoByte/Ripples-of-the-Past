package com.github.standobyte.jojo.client.playeranim.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface HamonSYOBAnim {
    
    public boolean setStartingAnim(PlayerEntity player);
    public boolean setFinisherAnim(PlayerEntity player);
    public void stopAnim(PlayerEntity player);
    
    public static class NoPlayerAnimator implements HamonSYOBAnim {

        @Override
        public boolean setStartingAnim(PlayerEntity player) {
            return false;
        }

        @Override
        public boolean setFinisherAnim(PlayerEntity player) {
            return false;
        }

        @Override
        public void stopAnim(PlayerEntity player) {}
    }
}
