package com.github.standobyte.jojo.client.ui.screen.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class TextButton extends Button {
    private FontRenderer font;
    
    public TextButton(int pX, int pY, ITextComponent pMessage, 
            IPressable pOnPress, ITooltip pOnTooltip, FontRenderer font) {
        super(pX, pY, font.width(pMessage), font.lineHeight, pMessage, pOnPress, pOnTooltip);
        this.font = font;
    }

    public TextButton(int pX, int pY, ITextComponent pMessage, 
            IPressable pOnPress, FontRenderer font) {
        this(pX, pY, pMessage, pOnPress, NO_TOOLTIP, font);
    }

    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        Minecraft mc = Minecraft.getInstance();
        
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        renderBg(pMatrixStack, mc, pMouseX, pMouseY);
        
        int j = getFGColor();
        ITextComponent text = makeText();
        font.drawShadow(pMatrixStack, text, x, y + (height - 8) / 2, j | MathHelper.ceil(alpha * 255.0F) << 24);
        width = font.width(text);
        
        if (isHovered()) {
            renderToolTip(pMatrixStack, pMouseX, pMouseY);
        }
    }
    
    public ITextComponent makeText() {
        ITextComponent text = getMessage();
        if (isHovered()) {
            text = new TranslationTextComponent("jojo.ui.text_button_hovered", text).withStyle(TextFormatting.GREEN);
        }
        return text;
    }
}
