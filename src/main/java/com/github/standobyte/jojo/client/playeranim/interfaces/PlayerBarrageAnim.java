package com.github.standobyte.jojo.client.playeranim.interfaces;

import com.github.standobyte.jojo.client.playeranim.IPlayerBarrageAnimation;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;

public interface PlayerBarrageAnim extends BasicToggleAnim {
    
    IPlayerBarrageAnimation createBarrageAfterimagesAnim(PlayerModel<AbstractClientPlayerEntity> model, BarrageFistAfterimagesLayer layer);
    
    
    
    public static class NoPlayerAnimator extends BasicToggleAnim.NoPlayerAnimator implements PlayerBarrageAnim {
        
        @Override
        public IPlayerBarrageAnimation createBarrageAfterimagesAnim(PlayerModel<AbstractClientPlayerEntity> model,
                BarrageFistAfterimagesLayer layer) {
            return null;
        }
        
    }
}
