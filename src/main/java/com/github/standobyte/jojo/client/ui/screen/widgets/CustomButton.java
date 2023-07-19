package com.github.standobyte.jojo.client.ui.screen.widgets;

import com.github.standobyte.jojo.client.ui.screen.widgets.utils.IExtendedWidget;
import com.github.standobyte.jojo.client.ui.screen.widgets.utils.WidgetExtension;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

@SuppressWarnings("deprecation")
public class CustomButton extends Button implements IExtendedWidget {
    private final WidgetExtension extension;

    public CustomButton(int x, int y, int width, int height, Button.IPressable onPress) {
        this(x, y, width, height, StringTextComponent.EMPTY, onPress);
    }

    public CustomButton(int x, int y, int width, int height, Button.IPressable onPress, Button.ITooltip tooltip) {
        this(x, y, width, height, StringTextComponent.EMPTY, onPress, tooltip);
    }

    public CustomButton(int x, int y, int width, int height, ITextComponent message, Button.IPressable onPress) {
        super(x, y, width, height, message, onPress);
        this.extension = new WidgetExtension(this);
    }

    public CustomButton(int x, int y, int width, int height, ITextComponent message, Button.IPressable onPress, Button.ITooltip tooltip) {
        super(x, y, width, height, message, onPress, tooltip);
        this.extension = new WidgetExtension(this);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        renderCustomButton(matrixStack, mouseX, mouseY, partialTick);
        if (isHovered()) {
            renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
    
    protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        int i = getYImage(isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(matrixStack, x, y, 0, 46 + i * 20, width / 2, height);
        blit(matrixStack, x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
        renderBg(matrixStack, minecraft, x, y);
    }
    
    
    
    @Override
    public WidgetExtension getWidgetExtension() {
        return extension;
    }
    
    @Override
    public Widget thisAsWidget() {
        return this;
    }
}
