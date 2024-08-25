package com.github.standobyte.jojo.client.ui.screen.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ItemButton extends Button {
    private final ItemStack item;

    public ItemButton(int pX, int pY, int pWidth, int pHeight, 
            ItemStack item, 
            Button.IPressable pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                item, 
                pOnPress, NO_TOOLTIP, StringTextComponent.EMPTY);
    }

    public ItemButton(int pX, int pY, int pWidth, int pHeight, 
            ItemStack item, 
            Button.IPressable pOnPress, Button.ITooltip pOnTooltip) {
        this(pX, pY, pWidth, pHeight, 
                item, 
                pOnPress, pOnTooltip, StringTextComponent.EMPTY);
    }

    public ItemButton(int pX, int pY, int pWidth, int pHeight, 
            ItemStack item, 
            Button.IPressable pOnPress, Button.ITooltip pOnTooltip, ITextComponent pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.item = item;
    }

    public void setPosition(int pX, int pY) {
        this.x = pX;
        this.y = pY;
    }

    @SuppressWarnings("deprecation")
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
        
        minecraft.getItemRenderer().renderGuiItem(item, x + (width - 16) / 2, y + (height - 16) / 2);
        
        if (isHovered()) {
            renderToolTip(pMatrixStack, pMouseX, pMouseY);
         }
    }

}
