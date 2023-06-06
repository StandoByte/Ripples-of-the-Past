package com.github.standobyte.jojo.client.playeranim.playeranimator.anim.hamon;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.playeranimator.PlayerAnimatorInstalled.AnimLayerHandler;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MeditationPoseLayer extends AnimLayerHandler {

    public MeditationPoseLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }

    private static final ResourceLocation SIT_DOWN_PATH = new ResourceLocation(JojoMod.MOD_ID, "meditation");
//    private static final ResourceLocation STAND_UP_PATH = new ResourceLocation(JojoMod.MOD_ID, "meditation_stand_up");
    public void setAnimEnabled(AbstractClientPlayerEntity player, boolean enabled) {
        if (enabled) {
            setAnimFromName(player, SIT_DOWN_PATH);
        }
        else {
//            setAnimFromName(player, STAND_UP_PATH);
            fadeOutAnim(player, AbstractFadeModifier.standardFadeIn(4, Ease.OUTCUBIC), null);
        }
    }
    
    public IAnimation getHamonMasterAnim() {
        KeyframeAnimation keyframes = PlayerAnimationRegistry.getAnimation(SIT_DOWN_PATH);
        if (keyframes != null) {
            ModifierLayer<IAnimation> anim = new ModifierLayer<>(new KeyframeAnimationPlayer(keyframes, keyframes.returnToTick));
            anim.addModifierLast(new SpeedModifier(0.5F));
            return anim;
        }
        return null;
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
