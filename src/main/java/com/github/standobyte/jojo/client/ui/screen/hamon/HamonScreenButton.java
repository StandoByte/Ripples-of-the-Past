package com.github.standobyte.jojo.client.ui.screen.hamon;

import com.github.standobyte.jojo.client.ui.screen.CustomButton;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;

@SuppressWarnings("deprecation")
public class HamonScreenButton extends CustomButton {
    private int yStarting;
    private boolean mouseInWindow;

    public HamonScreenButton(int x, int y, int width, int height, 
            ITextComponent message, IPressable onPress) {
        super(x, y, width, height, message, onPress);
        this.yStarting = y;
    }

    public HamonScreenButton(int x, int y, int width, int height, 
            ITextComponent message, IPressable onPress, ITooltip tooltip) {
        super(x, y, width, height, message, onPress, tooltip);
        this.yStarting = y;
    }
    
    public void setMouseInWindow(boolean mouseInWindow) {
        this.mouseInWindow = mouseInWindow;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (!mouseInWindow) mouseY = -1;
        super.render(matrixStack, mouseX, mouseY, partialTick);
    }
    
    @Override
    protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        int i = getYImage(isHovered());
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.enableDepthTest();
        blit(matrixStack, x, y, 0, 46 + i * 20, width / 2, height);
        blit(matrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
    }
    
    public void setY(int y) {
        this.y = y;
        this.yStarting = y;
    }
    
    public int getYStarting() {
        return yStarting;
    }
    
    public void updateY(int scrollY) {
        this.y = this.yStarting + scrollY;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return mouseInWindow ? super.mouseClicked(mouseX, mouseY, mouseButton) : false;
    }
}
