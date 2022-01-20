package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.SoulEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class SoulController {
    private static SoulEntity playerSoulEntity = null;
    private static boolean showDeathScreen = true;
    
    public static void onSoulSpawn(SoulEntity soulEntity) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.player.isSpectator() && soulEntity.getOriginEntity() == mc.player) {
            mc.setCameraEntity(soulEntity);
            playerSoulEntity = soulEntity;
            showDeathScreen = mc.player.shouldShowDeathScreen();
            mc.player.setShowDeathScreen(true);
        }
    }
    
    private static boolean isCameraEntityPlayerSoul() {
        Minecraft mc = Minecraft.getInstance();
        return playerSoulEntity != null && playerSoulEntity.isAlive() && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelRespawnScreen(GuiOpenEvent event) {
        Minecraft mc = Minecraft.getInstance();
        boolean soul = isCameraEntityPlayerSoul();
        if (event.getGui() instanceof DeathScreen && (soul || !showDeathScreen)) {
            event.setGui(null);
            if (!soul) {
                mc.player.setShowDeathScreen(showDeathScreen);
                mc.player.respawn();
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelHandsRender(RenderHandEvent event) {
        if (isCameraEntityPlayerSoul()) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void skipAscension(InputUpdateEvent event) {
        if (isCameraEntityPlayerSoul() && event.getMovementInput().jumping) {
            playerSoulEntity.skipAscension();
        }
    }
}
