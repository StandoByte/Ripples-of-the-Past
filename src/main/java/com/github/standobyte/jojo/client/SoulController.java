package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.GameplayEventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class SoulController {
    private static SoulController instance = null;
    
    private final Minecraft mc;
    private SoulEntity playerSoulEntity = null;
    private int soulEntityWaitingTimer = -1;
    private IStandPower standPower = null;
    private boolean willSoulSpawn = false;

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
    
    @SubscribeEvent
    public void tick(ClientTickEvent event) {
        if (mc.player != null) {
            if (mc.player.isDeadOrDying()) {
                if (soulEntityWaitingTimer > 0) {
                    if (playerSoulEntity != null) {
                        soulEntityWaitingTimer = 0;
                    }
                    else {
                        soulEntityWaitingTimer--;
                    }
                }
            }
            else {
                soulEntityWaitingTimer = -1;
                if (playerSoulEntity != null && !playerSoulEntity.isAlive()) {
                    ClientUtil.setCameraEntityPreventShaderSwitch(mc, mc.player);
                    playerSoulEntity = null;
                }
                
                if (standPower == null) {
                    updateStandCache();
                }
                willSoulSpawn = GameplayEventHandler.getSoulAscensionTicks(mc.player, standPower) > 0;
            }
        }
    }
    
    public void onSoulSpawn(SoulEntity soulEntity) {
        if (!mc.player.isSpectator() && soulEntity.getOriginEntity() == mc.player) {
            ClientUtil.setCameraEntityPreventShaderSwitch(mc, soulEntity);
            playerSoulEntity = soulEntity;
        }
    }
    
    private boolean isCameraEntityPlayerSoul() {
        return playerSoulEntity != null && playerSoulEntity.isAlive() && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelRespawnScreen(GuiOpenEvent event) {
        boolean soul = isCameraEntityPlayerSoul();
        if (event.getGui() instanceof DeathScreen) {
            if (soulEntityWaitingTimer == -1 && willSoulSpawn) {
                soulEntityWaitingTimer = 100;
            }
            if (soul || soulEntityWaitingTimer > 0) {
                event.setGui(null);
                if (playerSoulEntity != null && !playerSoulEntity.isAlive() && soulEntityWaitingTimer <= 0) {
                    mc.player.respawn();
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelHandsRender(RenderHandEvent event) {
        if (playerSoulEntity != null && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator() && mc.player.isDeadOrDying()) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void skipAscension(InputUpdateEvent event) {
        if (isCameraEntityPlayerSoul() && event.getMovementInput().jumping) {
            playerSoulEntity.skipAscension();
        }
    }
    
    public void updateStandCache() {
        standPower = IStandPower.getPlayerStandPower(mc.player);
    }
}
