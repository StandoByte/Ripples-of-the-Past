package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import java.util.Optional;

import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WindupAttackAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.KosmXKeyframeAnimPlayer;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public abstract class KosmXWindupAttackHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> implements WindupAttackAnim {

    public KosmXWindupAttackHandler(ResourceLocation id) {
        super(id);
    }
    
    protected boolean setToSwingTick(PlayerEntity player, int minusTicks, ResourceLocation animId) {
        ModifierLayer<IAnimation> animLayer = getAnimLayer((AbstractClientPlayerEntity) player);
        Optional<KosmXKeyframeAnimPlayer> attackAnim = playingAnim(animLayer, animId);
        if (attackAnim.isPresent()) {
            KosmXKeyframeAnimPlayer anim = attackAnim.get();
            int tick = anim.getKeyframes().returnToTick - minusTicks;
            if (anim.isActive() && anim.getTick() < tick) {
                setAnimFromName(player, animId, a -> new KosmXKeyframeAnimPlayer(a, tick, false));
                return true;
            }
        }
        
        return false;
    }
    
    protected static Optional<KosmXKeyframeAnimPlayer> playingAnim(ModifierLayer<IAnimation> animLayer, ResourceLocation animId) {
        if (animLayer == null) return Optional.empty();
        IAnimation playingAnim = animLayer.getAnimation();
        if (playingAnim instanceof KosmXKeyframeAnimPlayer) {
            KosmXKeyframeAnimPlayer animPlayer = (KosmXKeyframeAnimPlayer) playingAnim;
            KeyframeAnimation jsonAnim = animPlayer.getKeyframes();
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

}
