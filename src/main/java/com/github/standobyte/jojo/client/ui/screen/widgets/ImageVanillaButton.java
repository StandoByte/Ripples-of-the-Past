package com.github.standobyte.jojo.client.ui.screen.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ImageVanillaButton extends Button {
    private final ResourceLocation resourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final int textureWidth;
    private final int textureHeight;
    private final int iconWidth;
    private final int iconHeight;

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, 
            ResourceLocation pResourceLocation, 
            Button.IPressable pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                pXTexStart, pYTexStart, 
                pResourceLocation, 256, 256, 
                pOnPress);
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.IPressable pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                pXTexStart, pYTexStart, 
                pResourceLocation, pTextureWidth, pTextureHeight, 
                pOnPress, StringTextComponent.EMPTY);
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.IPressable pOnPress, ITextComponent pMessage) {
        this(pX, pY, pWidth, pHeight, 
                pXTexStart, pYTexStart, pWidth, pHeight, 
                pResourceLocation, pTextureWidth, pTextureHeight, 
                pOnPress, NO_TOOLTIP, pMessage);
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.IPressable pOnPress, Button.ITooltip pOnTooltip, ITextComponent pMessage) {
        this(pX, pY, pWidth, pHeight,
                pXTexStart, pYTexStart, pWidth, pHeight,
                pResourceLocation, pTextureWidth, pTextureHeight,
                pOnPress, pOnTooltip, pMessage);
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, int iconWidth, int iconHeight, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.IPressable pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                pXTexStart, pYTexStart, iconWidth, iconHeight, 
                pResourceLocation, pTextureWidth, pTextureHeight, 
                pOnPress, NO_TOOLTIP, StringTextComponent.EMPTY);
    }

    public ImageVanillaButton(int pX, int pY, int pWidth, int pHeight, 
            int pXTexStart, int pYTexStart, int iconWidth, int iconHeight, 
            ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, 
            Button.IPressable pOnPress, Button.ITooltip pOnTooltip, ITextComponent pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.textureWidth = pTextureWidth;
        this.textureHeight = pTextureHeight;
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.resourceLocation = pResourceLocation;
    }

    public void setPosition(int pX, int pY) {
        this.x = pX;
        this.y = pY;
    }

    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        int i = getYImage(isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(pMatrixStack, x, y, 0, 46 + i * 20, width / 2, height);
        blit(pMatrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
        renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
        
        minecraft.getTextureManager().bind(resourceLocation);
        RenderSystem.enableDepthTest();
        int iconX = x + (width - iconWidth) / 2;
        int iconY = y + (height - iconHeight) / 2;
        blit(pMatrixStack, iconX, iconY, (float)xTexStart, (float)yTexStart, 
                iconWidth, iconHeight, textureWidth, textureHeight);
        
        if (isHovered()) {
            renderToolTip(pMatrixStack, pMouseX, pMouseY);
         }
    }

}
