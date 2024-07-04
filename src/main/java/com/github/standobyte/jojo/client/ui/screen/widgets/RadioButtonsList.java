package com.github.standobyte.jojo.client.ui.screen.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.screen.stand.ge.ChooseLifeformScreen;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class RadioButtonsList<V> implements INestedGuiEventHandler {
    protected List<Button> radioButtons = new ArrayList<>();
    protected V selectedValue;
    protected Consumer<V> onNewValue;
    
    public RadioButtonsList() {}
    
    public RadioButtonsList(V defaultValue) {
        this.selectedValue = defaultValue;
    }
    
    public RadioButtonsList(V defaultValue, Consumer<V> onNewValue) {
        this(defaultValue);
        this.onNewValue = onNewValue;
    }
    
    public RadioButtonsList<V> addButton(int x, int y, ITextComponent name, V value) {
        RadioButton button = new RadioButton(x, y, name, b -> {
            this.selectedValue = value;
            onNewValue(value);
        }, this, value);
        radioButtons.add(button);
        return this;
    }
    
    protected void onNewValue(V value) {
        if (onNewValue != null) {
            onNewValue.accept(value);
        }
    }
    
    public V getSelectedValue() {
        return selectedValue;
    }
    
    
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (Button button : radioButtons) {
            button.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
    
    
    
    private static class RadioButton extends Button {
        private RadioButtonsList<?> list;
        private Object value;

        public RadioButton(int x, int y, ITextComponent pMessage, IPressable pOnPress, 
                RadioButtonsList<?> list, Object value) {
            super(x, y, 13, 13, pMessage, pOnPress);
            this.list = list;
            this.value = value;
        }
        
        @Override
        public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getTextureManager().bind(ChooseLifeformScreen.LIFEFORM_CHOOSE_LOCATION);
            int texY = list.getSelectedValue() == value ? 40 : 53;
            blit(pMatrixStack, x, y, 115, texY, width, height, 128, 128);
            minecraft.font.drawShadow(pMatrixStack, getMessage(), x + 16, y + (height - minecraft.font.lineHeight) / 2, 0xFFFFFF);
            if (isHovered()) {
                renderToolTip(pMatrixStack, pMouseX, pMouseY);
            }
        }
        
    }
    
    @Override
    public List<? extends IGuiEventListener> children() {
        return radioButtons;
    }
    
    
    @Nullable
    private IGuiEventListener focused;
    private boolean dragging;
    
    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean pDragging) {
        this.dragging = pDragging;
    }

    @Override
    public void setFocused(@Nullable IGuiEventListener pListener) {
        this.focused = pListener;
    }
    
    @Override
    public IGuiEventListener getFocused() {
        return this.focused;
    }
}
