package com.github.standobyte.jojo.client.renderer.player;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerRenderHandler {
    private static PlayerRenderHandler instance = null;
    
    private final Map<String, ModdedPlayerRenderer> playerRenderers = Maps.newHashMap();
    
    private PlayerRenderHandler() {}

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new PlayerRenderHandler();
            MinecraftForge.EVENT_BUS.register(instance);
            instance.playerRenderers.put("default", new ModdedPlayerRenderer(mc.getEntityRenderDispatcher(), false));
            instance.playerRenderers.put("slim", new ModdedPlayerRenderer(mc.getEntityRenderDispatcher(), true));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) event.getPlayer();
        ModdedPlayerRenderer renderer = playerRenderers.get(player.getModelName());
        if (renderer != null && renderer != event.getRenderer() && renderer.shouldChangeRender(player)) {
            event.setCanceled(true);
            float partialTick = event.getPartialRenderTick();
            renderer.render(player, MathHelper.lerp(partialTick, player.yRotO, player.yRot), partialTick, event.getMatrixStack(), event.getBuffers(), event.getLight());
        }
    }
}
