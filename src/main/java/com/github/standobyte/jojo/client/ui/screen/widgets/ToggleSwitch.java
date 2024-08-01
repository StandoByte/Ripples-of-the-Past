package com.github.standobyte.jojo.client.ui.screen.widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class ToggleSwitch extends Button {
    protected static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/toggle_switch.png");
    
    private final Orientation orientation;
    private final Supplier<Boolean> stateGet;
    private final Consumer<Boolean> stateSet;
    private boolean stateDefault;
    
    protected ToggleSwitch(int pX, int pY, int pWidth, int pHeight, Orientation orientation,
            Button.IPressable onPress, ITooltip tooltip, Supplier<Boolean> stateGet, Consumer<Boolean> stateSet) {
        super(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY, onPress, tooltip);
        this.orientation = orientation;
        this.stateGet = stateGet;
        this.stateSet = stateSet;
        this.stateDefault = stateGet.get();
    }
    
    public static ToggleSwitch create(int x, int y, Orientation orientation, 
            Supplier<Boolean> stateGet, Consumer<Boolean> stateSet, 
            Button.IPressable onPress, Button.ITooltip tooltip) {
        int width;
        int height;
        switch (orientation) {
        case HORIZONTAL:
            width = 26;
            height = 16;
            break;
        case VERTICAL:
            width = 16;
            height = 26;
            break;
        default:
            throw new IllegalArgumentException("goddammit java");
        }
        return new ToggleSwitch(x, y, width, height, orientation, onPress, tooltip, stateGet, stateSet);
    }
    
    
    
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL
    }
    
    
    @Override
    public void onPress() {
        toggle();
        super.onPress();
    }
    
    public void toggle() {
        if (stateSet != null) {
            stateSet.accept(!getState());
        }
        stateDefault = !stateDefault;
    }
    
    public boolean getState() {
        return stateGet != null ? stateGet.get() : stateDefault;
    }
    
    public void updateFromState() {
        if (stateSet != null) {
            stateSet.accept(getState());
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bind(new ResourceLocation(JojoMod.MOD_ID, "textures/gui/toggle_switch.png"));
        alpha = active ? 1 : 0.5f;
        RenderSystem.color4f(1, 1, 1, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int texX;
        int texY;
        switch (orientation) {
        case HORIZONTAL:
            texX = 0;
            texY = (stateGet.get() ? 32 : 0) + (isHovered() ? 16 : 0);
            break;
        case VERTICAL:
            texX = isHovered() ? 48 : 32;
            texY = stateGet.get() ? 32 : 0;
            break;
        default:
            throw new IllegalStateException("goddammit java");
        }
        blit(matrixStack, x, y, texX, texY, width, height, 64, 64);
        renderBg(matrixStack, mc, mouseX, mouseY);
        if (isHovered()) {
            renderToolTip(matrixStack, mouseX, mouseY);
        }
    }
    
}
