package com.github.standobyte.jojo.client.playeranim.playeranimator.anim.hamon;

import com.github.standobyte.jojo.client.playeranim.playeranimator.PlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.playeranimator.anim.modifier.ArmsRotationModifier;
import com.github.standobyte.jojo.client.playeranim.playeranimator.anim.modifier.HeadRotationModifier;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.barrage.BarrageFistAfterimagesLayer;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BarrageAnimLayer extends AnimLayerHandler {

    public BarrageAnimLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null, new HeadRotationModifier(), new ArmsRotationModifier(player, HandSide.LEFT, HandSide.RIGHT));
    }

    public void setAnimEnabled(AbstractClientPlayerEntity player, boolean enabled) {
        if (enabled) {
            setAnim(player, createSwingAnim(null));
        }
        else {
            fadeOutAnim(player, AbstractFadeModifier.standardFadeIn(4, Ease.OUTCUBIC), null);
        }

        BarrageFistAfterimagesLayer.setIsBarraging(player, enabled);
    }

    public PlayerBarrageAnim createSwingAnim(PlayerModel<AbstractClientPlayerEntity> model) {
        return new PlayerBarrageAnim(model);
    }
    
    @Override
    public boolean isForgeEventHandler() {
        return true;
    }

    @SubscribeEvent
    public void preRender(RenderPlayerEvent.Pre event) {
        preventCrouch((AbstractClientPlayerEntity) event.getPlayer(), event.getRenderer());
    }
}
