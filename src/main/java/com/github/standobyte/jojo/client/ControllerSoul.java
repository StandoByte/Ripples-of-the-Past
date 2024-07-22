package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ControllerSoul {
    private static ControllerSoul instance = null;
    
    private final Minecraft mc;
    private SoulEntity playerSoulEntity = null;
    private IStandPower standPower = null;
    private boolean firstDeathFrame = false;
    private boolean soulEntityWaiting = false;

    private ControllerSoul(Minecraft mc) {
        this.mc = mc;
    }

    public static void init(Minecraft mc) {
        if (instance == null) {
            instance = new ControllerSoul(mc);
            MinecraftForge.EVENT_BUS.register(instance);
        }
    }
    
    public static ControllerSoul getInstance() {
        return instance;
    }
    
    @SubscribeEvent
    public void tick(ClientTickEvent event) {
        if (mc.player != null) {
            if (mc.player.isDeadOrDying()) {
                if (soulEntityWaiting && playerSoulEntity != null) {
                    soulEntityWaiting = false;
                }
                if (soulEntityWaiting || isCameraEntityPlayerSoul()) {
                    mc.gui.setOverlayMessage(new TranslationTextComponent("jojo.message.skip_soul_ascension", new KeybindTextComponent("key.jump")), false);
                }
                mc.player.deathTime = Math.min(mc.player.deathTime, 18);
            }
            else {
                if (!firstDeathFrame) {
                    firstDeathFrame = true;
                }
                soulEntityWaiting = false;
                if (playerSoulEntity != null && !playerSoulEntity.isAlive()) {
                    ClientUtil.setCameraEntityPreventShaderSwitch(mc.player);
                    playerSoulEntity = null;
                    mc.gui.setOverlayMessage(StringTextComponent.EMPTY, false);
                }
                
                if (standPower == null) {
                    updateStandCache();
                }
            }
        }
    }
    
    public void onSoulSpawn(SoulEntity soulEntity) {
        if (!mc.player.isSpectator() && soulEntity.getOriginEntity() == mc.player) {
            ClientUtil.setCameraEntityPreventShaderSwitch(soulEntity);
            playerSoulEntity = soulEntity;
        }
    }
    
    public boolean isCameraEntityPlayerSoul() {
        return playerSoulEntity != null && playerSoulEntity.isAlive() && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelHandsRender(RenderHandEvent event) {
        if (playerSoulEntity != null && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator() && mc.player.isDeadOrDying()) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelUIRender(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.EXPERIENCE && 
                playerSoulEntity != null && playerSoulEntity == mc.getCameraEntity() && !mc.player.isSpectator() && mc.player.isDeadOrDying()) {
            event.setCanceled(true);
        }
    }
    
    public void skipAscension() {
        if (isCameraEntityPlayerSoul()) {
            playerSoulEntity.skipAscension();
            mc.gui.setOverlayMessage(StringTextComponent.EMPTY, false);
        }
        else if (soulEntityWaiting) {
            soulEntityWaiting = false;
            mc.gui.setOverlayMessage(StringTextComponent.EMPTY, false);
        }
    }
    
    public void updateStandCache() {
        standPower = IStandPower.getPlayerStandPower(mc.player);
    }
    
    
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void cancelRespawnScreen(GuiOpenEvent event) {
        boolean soul = isCameraEntityPlayerSoul();
        if (event.getGui() instanceof DeathScreen) {
            if (!soulEntityWaiting && firstDeathFrame && standPower.willSoulSpawn()) {
                soulEntityWaiting = true;
                firstDeathFrame = false;
            }
            if (soul || soulEntityWaiting) {
                event.setGui(null);
                if (playerSoulEntity != null && !playerSoulEntity.isAlive() && !soulEntityWaiting) {
                    mc.player.respawn();
                }
            }
        }
    }
    
    public void onSoulFailedSpawn() {
        soulEntityWaiting = false;
    }
}
