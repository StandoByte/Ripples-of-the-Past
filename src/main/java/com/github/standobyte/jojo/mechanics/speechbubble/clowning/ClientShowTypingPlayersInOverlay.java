package com.github.standobyte.jojo.mechanics.speechbubble.clowning;

import java.util.UUID;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientTimeStopHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientShowTypingPlayersInOverlay {

    @SubscribeEvent
    public static void renderOverlayMessage(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.SUBTITLES) return;
        
        Minecraft mc = Minecraft.getInstance();
        World world = mc.level;
        if (!mc.options.hideGui && world != null) {
            IngameGui vanillaGui = mc.gui;
            FontRenderer font = vanillaGui.getFont();
            MatrixStack matrixStack = event.getMatrixStack();
            int color = 0xFFFFFFFF;
            MainWindow window = mc.getWindow();
            float width = window.getGuiScaledWidth();
            float height = window.getGuiScaledHeight();
            int y = -4;
            
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)(width / 2), 0, 0);
            
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0, (float)(height - 68 - 13), 0);
            WorldTypingPlayers tracker = WorldTypingPlayers.get(world);
            if (tracker != null && !tracker.players.isEmpty()) {
                for (UUID playerId : tracker.players) {
                    Entity player = world.getPlayerByUUID(playerId);
                    if (player != null) {
                        ITextComponent line = player.getDisplayName();
                        line = new TranslationTextComponent("jojo_ripples.yap." + tracker.notificationIndex.getInt(playerId), line);
                        int lineWidth = font.width(line);
                        ClientUtil.drawBackdrop(matrixStack, -lineWidth / 2, y, lineWidth, 1);
                        font.draw(matrixStack, line.getVisualOrderText(), -lineWidth / 2, y, color);
                        RenderSystem.translatef(0, -13, 0);
                    }
                }
            }
            RenderSystem.popMatrix();
            
            ClientTimeStopHandler clientTS = ClientTimeStopHandler.getInstance();
            if (clientTS != null && clientTS.isTimeStopped()) {
                int seconds = (clientTS.timeStopTicks + 2 /* so that "5 seconds" shows up for a bit */) / 20;
                if (seconds > 0) {
                    y = 28;
                    String key = "jojo_ripples.dio_seconds.";
                    ITextComponent line = seconds <= 11 ? 
                            new TranslationTextComponent(key + String.valueOf(seconds)) :
                            new TranslationTextComponent(key + "and_so_on", seconds);
                    int lineWidth = font.width(line);
                    ClientUtil.drawBackdrop(matrixStack, -lineWidth / 2, y, lineWidth, 1);
                    font.draw(matrixStack, line.getVisualOrderText(), -lineWidth / 2, y, color);
                    
                }
            }
            
            RenderSystem.popMatrix();
        }
    }
}
