package com.github.standobyte.jojo.client.ui.screen.walkman;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

@SuppressWarnings("deprecation")
public class WalkmanVolumeWheel extends Widget {
    private static final float FULL_WHEEL_LENGTH = 100;
    private final WalkmanScreen screen;
    private float value;

    public WalkmanVolumeWheel(WalkmanScreen screen, int x, int y, int width, int height) {
        super(x, y, width, height, new StringTextComponent("walkman.volume"));
        this.screen = screen;
    }
    
    void setValue(float value) {
        value = MathHelper.clamp(value, 0, 1);
        if (this.value != value) {
            this.value = value;
            screen.onVolumeChanged(value);
        }
    }
    
    float getValue() {
        return value;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        setValue(value - (float) dragY / FULL_WHEEL_LENGTH);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        setValue(value + (float) scroll * 0.05F);
        return true;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft.getInstance().getTextureManager().bind(WalkmanScreen.WALKMAN_SCREEN_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        blit(matrixStack, x, y, isHovered() ? 229 : 245 , 61 + (int) (value * FULL_WHEEL_LENGTH), width, height);
    }

    @Override
    public void playDownSound(SoundHandler soundManager) {}

}
