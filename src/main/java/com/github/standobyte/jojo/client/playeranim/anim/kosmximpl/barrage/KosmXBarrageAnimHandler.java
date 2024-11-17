package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.barrage;

import com.github.standobyte.jojo.client.playeranim.IPlayerBarrageAnimation;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.PlayerBarrageAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXArmsRotationModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHeadRotationModifier;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public class KosmXBarrageAnimHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> implements PlayerBarrageAnim {

    public KosmXBarrageAnimHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null, new KosmXHeadRotationModifier(), new KosmXArmsRotationModifier(player, HandSide.LEFT, HandSide.RIGHT));
    }
    
    
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        boolean res;
        if (enabled) {
            res = setAnim(player, createSwingAnim(null));
        }
        else {
            res = fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(4, Ease.OUTCUBIC), null);
        }
        
        BarrageFistAfterimagesLayer.setIsBarraging(player, res && enabled);
        return res;
    }

    @Override
    public IPlayerBarrageAnimation createBarrageAfterimagesAnim(
            PlayerModel<AbstractClientPlayerEntity> model, BarrageFistAfterimagesLayer layer) {
        return new KosmXPlayerBarrageAfterimagesAnim(model, createSwingAnim(model), layer);
    }
    
    public KosmXPlayerBarrageAnim createSwingAnim(PlayerModel<AbstractClientPlayerEntity> model) {
        return new KosmXPlayerBarrageAnim(model);
    }
    
}
