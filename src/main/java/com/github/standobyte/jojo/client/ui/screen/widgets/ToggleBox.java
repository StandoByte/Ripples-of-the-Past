package com.github.standobyte.jojo.client.ui.screen.widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.screen.widgets.utils.IExtendedWidget;
import com.github.standobyte.jojo.client.ui.screen.widgets.utils.WidgetExtension;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class ToggleBox extends Widget implements IExtendedWidget {
    private final WidgetExtension extension;
    
    @Nullable private final Supplier<Boolean> stateGet;
    @Nullable private final Consumer<Boolean> stateSet;
    private boolean stateDefault;

    public ToggleBox(int x, int y, int width, int height, ITextComponent name, 
            Supplier<Boolean> stateGet, Consumer<Boolean> stateSet) {
        super(x, y, width, height, name);
        this.extension = new WidgetExtension(this);
        this.stateGet = stateGet;
        this.stateSet = stateSet;
        this.stateDefault = getState();
    }

    public ToggleBox(int x, int y, int width, int height, ITextComponent name, 
            boolean startingState) {
        this(x, y, width, height, name, null, null);
        this.stateDefault = startingState;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        toggle();
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
    
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        drawCenteredString(matrixStack, Minecraft.getInstance().font, getMessage(), 
                x + width / 2, y + (height - 8) / 2, getFGColor() | MathHelper.ceil(alpha * 255.0F) << 24);
    }

    @Override
    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {}
    
    
    
    @Override
    public WidgetExtension getWidgetExtension() {
        return extension;
    }
    
    @Override
    public Widget thisAsWidget() {
        return this;
    }
}
