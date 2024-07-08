package com.github.standobyte.jojo.client.playeranim.kosmx.anim.layers;

import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KosmXTestAnimLayer extends AnimLayerHandler {

    public KosmXTestAnimLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    public ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        return new ModifierLayer<>(null);
    }
    
    @Override
    public boolean isForgeEventHandler() {
        return true;
    }
    
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        String[] msg = event.getMessage().split(" ");
        if (msg.length > 0 && "/animtest".equals(msg[0])) {
            AbstractClientPlayerEntity player = Minecraft.getInstance().player;
            if (msg.length == 2) {
                try {
                    int fadeOut = Integer.parseInt(msg[1]);
                    if (fadeOut > 0) {
                        fadeOutAnim(player, AbstractFadeModifier.standardFadeIn(10, Ease.OUTCUBIC), null);
                    }
                }
                catch (NumberFormatException e) {
                    ResourceLocation animPath = new ResourceLocation(msg[1]);
                    setAnimFromName(player, animPath);
                }
            }
            else if (msg.length == 1) {
                getAnimLayer(player).setAnimation(null);
            }
            event.setCanceled(true);
        }
    }
    
}
