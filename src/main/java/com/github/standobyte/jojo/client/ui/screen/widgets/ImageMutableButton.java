package com.github.standobyte.jojo.client.ui.screen.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ImageMutableButton extends Button {
    public ResourceLocation resourceLocation;
    public int xTexStart;
    public int yTexStart;
    public int yDiffTex;
    public int textureWidth;
    public int textureHeight;

    public ImageMutableButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, Button.IPressable pOnPress) {
       this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, 256, 256, pOnPress);
    }

    public ImageMutableButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.IPressable pOnPress) {
       this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pTextureWidth, pTextureHeight, pOnPress, StringTextComponent.EMPTY);
    }

    public ImageMutableButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.IPressable pOnPress, ITextComponent pMessage) {
       this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pTextureWidth, pTextureHeight, pOnPress, NO_TOOLTIP, pMessage);
    }

    public ImageMutableButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.IPressable pOnPress, Button.ITooltip pOnTooltip, ITextComponent pMessage) {
       super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
       this.textureWidth = pTextureWidth;
       this.textureHeight = pTextureHeight;
       this.xTexStart = pXTexStart;
       this.yTexStart = pYTexStart;
       this.yDiffTex = pYDiffTex;
       this.resourceLocation = pResourceLocation;
    }

    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
       Minecraft minecraft = Minecraft.getInstance();
       minecraft.getTextureManager().bind(this.resourceLocation);
       int i = this.yTexStart;
       if (this.isHovered()) {
          i += this.yDiffTex;
       }

       RenderSystem.enableDepthTest();
       blit(pMatrixStack, this.x, this.y, (float)this.xTexStart, (float)i, this.width, this.height, this.textureWidth, this.textureHeight);
       if (this.isHovered()) {
          this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
       }

    }
 }
