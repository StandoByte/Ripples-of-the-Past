package com.github.standobyte.jojo.client.playeranim.anim.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface RebuffOverdriveAnim {
    
    public boolean setWindupAnim(PlayerEntity player);
    public boolean setAttackAnim(PlayerEntity player);
    public void stopAnim(PlayerEntity player);
    
    public static class NoPlayerAnimator implements RebuffOverdriveAnim {

        @Override
        public boolean setWindupAnim(PlayerEntity player) {
            return false;
        }

        @Override
        public boolean setAttackAnim(PlayerEntity player) {
            return false;
        }

        @Override
        public void stopAnim(PlayerEntity player) {}
    }
}
