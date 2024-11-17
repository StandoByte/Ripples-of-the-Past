package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl;

import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXFixedFadeModifier;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KosmXTestAnimHandler extends AnimLayerHandler<ModifierLayer<IAnimation>> {
    private SpeedModifier animSpeed = new SpeedModifier(1);

    public KosmXTestAnimHandler(ResourceLocation id) {
        super(id);
    }

    @Override
    public ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        ModifierLayer<IAnimation> layer = new ModifierLayer<>(null);
        layer.addModifierLast(animSpeed);
        return layer;
    }
    
    @Override
    public boolean isForgeEventHandler() {
        return true;
    }
    
    /* 
     *   /animtest <name>
     *   /animtest <name> <speed>
     *   /animtest <fadeOutTicks>
     */
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        String[] msg = event.getMessage().split(" ");
        if (msg.length > 0 && "/animtest".equals(msg[0])) {
            AbstractClientPlayerEntity player = Minecraft.getInstance().player;
            ResourceLocation animName = null;
            int fadeOutTicks = -1;
            float speedModifier = -1;
            switch (msg.length) {
            case 3:
                animName = new ResourceLocation(msg[1]);
                try {
                    speedModifier = Float.parseFloat(msg[2]);
                }
                catch (NumberFormatException e) {}
                break;
            case 2:
                try {
                    fadeOutTicks = Integer.parseInt(msg[1]);
                }
                catch (NumberFormatException e) {
                    animName = new ResourceLocation(msg[1]);
                }
                break;
            case 1:
                break;
            }
            
            if (animName != null) {
                setAnimFromName(player, animName);
                if (speedModifier > 0) {
                    animSpeed.speed = speedModifier;
                }
            }
            else if (fadeOutTicks > 0) {
                fadeOutAnim(player, KosmXFixedFadeModifier.standardFadeIn(fadeOutTicks, Ease.OUTCUBIC), null);
            }
            else {
                setAnim(player, null);
            }
            event.setCanceled(true);
            Minecraft.getInstance().gui.getChat().addRecentChat(event.getMessage());
        }
    }
    
}
