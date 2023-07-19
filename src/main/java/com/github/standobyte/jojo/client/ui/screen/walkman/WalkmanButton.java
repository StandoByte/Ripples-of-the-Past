package com.github.standobyte.jojo.client.ui.screen.walkman;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ui.screen.widgets.CustomButton;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

@SuppressWarnings("deprecation")
public class WalkmanButton extends CustomButton {
    private final Supplier<ITextComponent> message;
    private final int texX;

    public WalkmanButton(int x, int y, int width, int height, IPressable onPress, Supplier<ITextComponent> message, Screen screen, int texX) {
        this(x, y, width, height, onPress, (button, matrixStack, mouseX, mouseY) -> screen.renderTooltip(matrixStack, button.getMessage(), mouseX, mouseY), message, texX);
    }

    public WalkmanButton(int x, int y, int width, int height, IPressable onPress, ITooltip tooltip, Supplier<ITextComponent> message, int texX) {
        super(x, y, width, height, StringTextComponent.EMPTY, onPress, tooltip);
        this.message = message;
        this.texX = texX;
    }

    @Override
    protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(WalkmanScreen.WALKMAN_SCREEN_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(matrixStack, x, y, texX, active && isHovered() ? 240 : 227, width, height);
    }

    @Override
    public void playDownSound(SoundHandler soundManager) {
        soundManager.play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.5F, 0.1F));
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (active && isHovered()) {
            super.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
    
    @Override
    public ITextComponent getMessage() {
        ITextComponent message = this.message.get();
        return message != null ? message : super.getMessage();
    }
}
