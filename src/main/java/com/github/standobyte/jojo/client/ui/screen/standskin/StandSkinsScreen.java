package com.github.standobyte.jojo.client.ui.screen.standskin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.standskin.StandSkin;
import com.github.standobyte.jojo.client.render.entity.standskin.StandSkinsManager;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class StandSkinsScreen extends Screen {
    private static final ResourceLocation TEXTURE_MAIN_WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_skins.png");
    
    private static final int WINDOW_WIDTH = 195;
    private static final int WINDOW_HEIGHT = 180;
    public static final int WINDOW_INSIDE_X = 7;
    public static final int WINDOW_INSIDE_WIDTH = 165;
    public static final int WINDOW_INSIDE_Y = 20;
    public static final int WINDOW_INSIDE_HEIGHT = 153;
    
    private static ResourceLocation latestStand = null;
    private static int latestScroll;
    
    private IStandPower standCap;
    private List<SkinView> skins;
    private int scroll;
    
    private StandSkinsScreen() {
        super(StringTextComponent.EMPTY);
    }
    
    public static void openScreen() {
        IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(playerStand -> {
            if (playerStand.hasPower()) {
                StandSkinsScreen screen = new StandSkinsScreen();
                screen.setStandCap(playerStand);
                Minecraft.getInstance().setScreen(screen);
            }
        });
    }
    
    private void setStandCap(IStandPower standCap) {
        this.standCap = standCap;
        this.skins = Streams.mapWithIndex(StandSkinsManager.getInstance()
                .getStandSkinsView(standCap.getType().getRegistryName())
                .stream(), (skin, index) -> {
                    int x = 3 + (int) (index % 3) * (SkinView.width + 3);
                    int y = 3 + (int) (index / 3) * (SkinView.height + 3);
                    return new SkinView(skin, x, y);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public void init() {
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (!standCap.hasPower()) {
            onClose();
            return;
        }
        renderBackground(matrixStack, 0);
        RenderSystem.enableBlend();
        
        minecraft.getTextureManager().bind(TEXTURE_MAIN_WINDOW);
        blit(matrixStack, getWindowX(), getWindowY(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        Optional<SkinView> hoveredSkin = getSkinAt(mouseX, mouseY);
        matrixStack.pushPose();
        matrixStack.translate(getWindowX() + WINDOW_INSIDE_X, getWindowY() + WINDOW_INSIDE_Y, 0);
        for (SkinView skin : skins) {
            skin.render(matrixStack, mouseX, mouseY, partialTick, 
                    hoveredSkin.map(hovered -> skin == hovered).orElse(false));
        }
        matrixStack.popPose();
        
        RenderSystem.disableBlend();
        
        for (Widget button : buttons) {
            button.render(matrixStack, mouseX, mouseY, partialTick);
        }
    }
    
    private boolean isSkinSelected(StandSkin skin) {
        StandInstance stand = standCap.getStandInstance().get();
        ResourceLocation skinId = stand.getSelectedSkin()
                .orElseGet(() -> stand.getType().getRegistryName());
        return skinId.equals(skin.resLoc);
    }
    
    private Optional<SkinView> getSkinAt(int mouseX, int mouseY) {
        int x = mouseX - (getWindowX() + WINDOW_INSIDE_X);
        int y = mouseY - (getWindowY() + WINDOW_INSIDE_Y);
        return skins.stream().filter(skin -> 
        x > skin.x && x <= skin.x + SkinView.width && 
        y > skin.y && y <= skin.y + SkinView.height)
                .findFirst();
    }
    
    private int getWindowX() { return (width - WINDOW_WIDTH) / 2; }
    private int getWindowY() { return (height - WINDOW_HEIGHT) / 2; }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!super.mouseClicked(mouseX, mouseY, mouseButton)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_1) {
            Optional<SkinView> hoveredBox = getSkinAt((int) mouseX, (int) mouseY);
            if (hoveredBox.isPresent()) {
                StandSkin skin = hoveredBox.get().skin;
                PacketManager.sendToServer(new ClSetStandSkinPacket(Optional.of(skin.resLoc), skin.standTypeId));
                return true;
            }
        }
        
        return false;
    }
    
    
    private class SkinView {
        public final StandSkin skin;
        public final int x;
        public final int y;
        public static final int width = 51;
        public static final int height = 72;
        
        public SkinView(StandSkin skin, int x, int y) {
            this.skin = skin;
            this.x = x;
            this.y = y;
        }
        
        @SuppressWarnings("deprecation")
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick, boolean isHovered) {
            blit(matrixStack, x, y, 98, 182, width, height);
            if (isSkinSelected(skin)) {
                blit(matrixStack, x + width - 18, y + 2, 0, 192, 16, 16);
            }
            if (isHovered) {
                float[] color = ClientUtil.rgb(skin.color);
                RenderSystem.color3f(color[0], color[1], color[2]);
                blit(matrixStack, x - 2, y - 2, 32, 180, width + 4, height + 4);
                RenderSystem.color3f(1, 1, 1);
            }
        }
    }
}
