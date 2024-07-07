package com.github.standobyte.jojo.client.playeranim.kosmx.anim;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KosmXBusyArmsLayer extends AnimLayerHandler {

    public KosmXBusyArmsLayer(ResourceLocation id) {
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
    public void preRender(RenderPlayerEvent.Pre event) {
        preventCrouch((AbstractClientPlayerEntity) event.getPlayer(), event.getRenderer());
    }
    
    public void setGliderGrabEnabled(AbstractClientPlayerEntity player, boolean enabled) {
        if (enabled) {
            setAnimFromName(player, new ResourceLocation(JojoMod.MOD_ID, "glider_hold"));
        }
        else {
            setAnim(player, null);
        }
    }

}
