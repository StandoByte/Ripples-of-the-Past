package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.util.text.ITextComponent;

public class CategoryWithButtonsEntry extends KeyBindingList.CategoryEntry {
    private final List<Button> buttons;

    public CategoryWithButtonsEntry(KeyBindingList keyBindingList, 
            ITextComponent name, Button... buttons) {
        keyBindingList.super(name);
        this.buttons = Arrays.asList(buttons);
    }
    
    @Override
    public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        super.render(pMatrixStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
        for (Button button : buttons) {
            button.y = pTop;
            button.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        }
    }
    
    @Override
    public List<? extends IGuiEventListener> children() {
        return buttons;
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (IGuiEventListener button : children()) {
            if (button.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        for (IGuiEventListener button : children()) {
            if (button.mouseReleased(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        
        return false;
    }
}
