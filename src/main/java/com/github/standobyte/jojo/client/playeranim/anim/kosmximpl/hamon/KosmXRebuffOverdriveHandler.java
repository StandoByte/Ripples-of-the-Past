package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import java.util.Optional;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.RebuffOverdriveAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class KosmXRebuffOverdriveHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> implements RebuffOverdriveAnim {

    public KosmXRebuffOverdriveHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }
    
    
    private static final ResourceLocation REBUFF_OVERDRIVE = new ResourceLocation(JojoMod.MOD_ID, "rebuff_overdrive");
    
    @Override
    public boolean setWindupAnim(PlayerEntity player) {
        return setAnimFromName(player, REBUFF_OVERDRIVE);
    }

    @Override
    public boolean setAttackAnim(PlayerEntity player) {
        return setToSwingTick(player, 0);
    }
    
    private boolean setToSwingTick(PlayerEntity player, int minusTicks) {
        ModifierLayer<IAnimation> animLayer = getAnimLayer((AbstractClientPlayerEntity) player);
        Optional<KeyframeAnimationPlayer> rebuffAnim = playingAnim(animLayer, REBUFF_OVERDRIVE);
        if (rebuffAnim.isPresent()) {
            KeyframeAnimationPlayer anim = rebuffAnim.get();
            int tick = anim.getData().returnToTick - minusTicks;
            if (anim.isActive() && anim.getTick() < tick) {
                setAnimFromName(player, REBUFF_OVERDRIVE, 
                        a -> new KeyframeAnimationPlayer(a, tick, false));
                return true;
            }
        }
        
        return false;
    }
    
    
    protected static Optional<KeyframeAnimationPlayer> playingAnim(ModifierLayer<IAnimation> animLayer, ResourceLocation animId) {
        if (animLayer == null) return Optional.empty();
        IAnimation playingAnim = animLayer.getAnimation();
        if (playingAnim instanceof KeyframeAnimationPlayer) {
            KeyframeAnimationPlayer animPlayer = (KeyframeAnimationPlayer) playingAnim;
            KeyframeAnimation jsonAnim = animPlayer.getData();
            Object jsonAnimName = jsonAnim.extraData.get("name");
            if (jsonAnimName instanceof String) {
                String name = (String) jsonAnimName;
                name = name.replace("\"", "");
                if (animId.getPath().equals(name)) {
                    return Optional.of(animPlayer);
                }
            }
        }
        return Optional.empty();
    }
    
    
    @Override
    public void stopAnim(PlayerEntity player) {
        fadeOutAnim(player, AbstractFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
    }
    
}
