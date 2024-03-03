package com.github.standobyte.jojo.client.ui.screen.hamon;

import com.github.standobyte.jojo.client.ui.screen.widgets.CustomButton;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

@SuppressWarnings("deprecation")
public class HamonScreenButton extends CustomButton {
    
    public HamonScreenButton(int x, int y, int width, int height, 
            ITextComponent message, IPressable onPress) {
        super(x, y, width, height, message, onPress);
    }
    
    public HamonScreenButton(int x, int y, int width, int height, 
            ITextComponent message, IPressable onPress, ITooltip tooltip) {
        super(x, y, width, height, message, onPress, tooltip);
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
    
    public void drawName(MatrixStack matrixStack) {
        // shadow is gone for whatever reason so it's rendered here
        drawCenteredString(matrixStack, Minecraft.getInstance().font, this.getMessage(), 
                this.x + this.getWidth() / 2 + 1, this.y + (this.getHeight() - 8) / 2 + 1, 
                0x3E3E3E | MathHelper.ceil(this.alpha * 255.0F) << 24);
        
        drawCenteredString(matrixStack, Minecraft.getInstance().font, this.getMessage(), 
                this.x + this.getWidth() / 2, this.y + (this.getHeight() - 8) / 2, 
                getFGColor() | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }
}
