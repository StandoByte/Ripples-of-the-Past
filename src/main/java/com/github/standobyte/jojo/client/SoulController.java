package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.SoulEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class SoulController {
    private static SoulController instance = null;
    
    private final Minecraft mc;
    private SoulEntity playerSoulEntity = null;
    private boolean showDeathScreen = true;

    private SoulController(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new SoulController(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }
    
    public static SoulController getInstance() {
        return instance;
    }
    
    // FIXME (soul) currently there's no way to make sure it's called BEFORE the death screen opens (in fact it isn't when the player is killed by a mob)
    public void onSoulSpawn(SoulEntity soulEntity) {
        if (!mc.player.isSpectator() && soulEntity.getOriginEntity() == mc.player) {
            mc.setCameraEntity(soulEntity);
            playerSoulEntity = soulEntity;
            showDeathScreen = mc.player.shouldShowDeathScreen();
            mc.player.setShowDeathScreen(true);
        }
    }
    
    private boolean isCameraEntityPlayerSoul() {
        return playerSoulEntity != null && playerSoulEntity.isAlive() && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelRespawnScreen(GuiOpenEvent event) {
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
    public void cancelHandsRender(RenderHandEvent event) {
        if (isCameraEntityPlayerSoul()) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void skipAscension(InputUpdateEvent event) {
        if (isCameraEntityPlayerSoul() && event.getMovementInput().jumping) {
            playerSoulEntity.skipAscension();
        }
    }
}
