package com.github.standobyte.jojo.client.ui.screen;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class StandInfoScreen extends Screen {
    private static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/stand_info.png");
    private static final int WINDOW_WIDTH = 230;
    private static final int WINDOW_HEIGHT = 180;
    private IStandPower standCap;

    public StandInfoScreen() {
        super(StringTextComponent.EMPTY);
    }
    
    @Override
    public void init() {
        super.init();
        standCap = IStandPower.getPlayerStandPower(minecraft.player);
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(matrixStack, 0);
        renderWindow(matrixStack);

        JojoStuffScreen.renderStandTabs(matrixStack, 
                JojoStuffScreen.uniformX(minecraft), JojoStuffScreen.uniformY(minecraft), true, 
                mouseX, mouseY, this, JojoStuffScreen.StandTab.GENERAL_INFO, standCap);
    }
    
    private int getWindowX() { return (width - WINDOW_WIDTH) / 2; }
    private int getWindowY() { return (height - WINDOW_HEIGHT) / 2; }
    
    private void renderWindow(MatrixStack matrixStack) {
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(WINDOW);
        blit(matrixStack, getWindowX(), getWindowY(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        if (JojoStuffScreen.mouseClick(mouseX, mouseY, 
                JojoStuffScreen.uniformX(minecraft), JojoStuffScreen.uniformY(minecraft), 
                JojoStuffScreen.TabsEnumType.STAND)) {
            return true;
        }
        
        return false;
    }
    
}
