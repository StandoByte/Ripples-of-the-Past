package com.github.standobyte.jojo.port_bridge.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.port_bridge.jojo.client.ui.utils.GuiIcon;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class IconButton extends Button {
    public GuiIcon icon;

    public IconButton(int pX, int pY, int pWidth, int pHeight, 
            GuiIcon icon, 
            Button.IPressable pOnPress) {
        this(pX, pY, pWidth, pHeight, 
                icon, 
                pOnPress, null, StringTextComponent.EMPTY);
    }

    public IconButton(int pX, int pY, int pWidth, int pHeight, 
            GuiIcon icon, 
            Button.IPressable pOnPress, Button.ITooltip pOnTooltip) {
        this(pX, pY, pWidth, pHeight, 
                icon, 
                pOnPress, pOnTooltip, StringTextComponent.EMPTY);
    }

    public IconButton(int pX, int pY, int pWidth, int pHeight, 
            GuiIcon icon, 
            Button.IPressable pOnPress, Button.ITooltip pOnTooltip, ITextComponent pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.icon = icon;
    }

    @Override
    public void renderButton(MatrixStack guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderButton(guiGraphics, mouseX, mouseY, partialTick);
        if (icon != null) {
            float iconX = x + (width - icon.width) / 2;
            float iconY = y + (height - icon.height) / 2;
            icon.render(guiGraphics, iconX, iconY);
        }
    }

//    @Override
//    public void renderString(MatrixStack guiGraphics, Font font, int color) {}


    public static final GuiIcon CHECKMARK = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/checkmark.png"), 16, 16);
    public static final GuiIcon CROSS = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/cross.png"), 16, 16);
    public static void renderCheckmarkOrCross(Widget button, boolean value, MatrixStack poseStack) {
        int x = getRight(button) - 9;
        int y = getBottom(button) - 15;
        GuiIcon icon = value ? CHECKMARK : CROSS;
        icon.render(poseStack, x, y);
    }
    
    // I wish Java had C#'s extension methods, just a syntactic sugar but it'd be so convenient
    static int getRight(Widget button) { return button.x + button.getWidth(); }
    static int getBottom(Widget button) { return button.y + button.getHeight(); }

}
