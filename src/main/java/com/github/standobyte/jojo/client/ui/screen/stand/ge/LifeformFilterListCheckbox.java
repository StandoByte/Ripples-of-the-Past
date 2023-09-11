package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class LifeformFilterListCheckbox extends CheckboxButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private final Supplier<Boolean> getState;
    private final Consumer<Boolean> setState;

    public LifeformFilterListCheckbox(int pX, int pY, int pWidth, int pHeight, 
            ITextComponent pMessage, Supplier<Boolean> getState, Consumer<Boolean> setState) {
        super(pX, pY, pWidth, pHeight, pMessage, getState.get(), false);
        this.getState = getState;
        this.setState = setState;
    }
    
    @Override
    public void onPress() {
        setState.accept(!this.selected());
    }
    
    @Override
    public boolean selected() {
       return getState.get();
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        blit(pMatrixStack, x, y, isFocused() ? 20.0F : 0.0F, selected() ? 20.0F : 0.0F, 20, height, 64, 64);
        renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
//        if (showLabel) {
//            drawString(pMatrixStack, minecraft.font, getMessage(), x + 24, y + (height - 8) / 2, 14737632 | MathHelper.ceil(alpha * 255.0F) << 24);
//        }
    }
}
