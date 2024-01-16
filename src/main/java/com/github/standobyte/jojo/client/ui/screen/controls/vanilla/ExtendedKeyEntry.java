package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;

public class ExtendedKeyEntry extends KeyBindingList.Entry {
    private final KeyBindingList.KeyEntry wrappedEntry;
    private final Button holdToggleButton;
    private final Button changeButton;

    public ExtendedKeyEntry(KeyBindingList.KeyEntry entry, Button holdToggleButton) {
        this.wrappedEntry = entry;
        this.holdToggleButton = holdToggleButton;
        changeButton = ClientReflection.getChangeButton(entry);
        changeButton.setWidth(changeButton.getWidth() - holdToggleButton.getWidth() - 0);
    }
    
    @Override
    public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, 
            int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        holdToggleButton.x = pLeft + 105 + changeButton.getWidth() - 1;
        holdToggleButton.y = pTop;
        holdToggleButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        
        wrappedEntry.render(pMatrixStack, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTicks);
    }
    
    @Override
    public List<? extends IGuiEventListener> children() {
        List<? extends IGuiEventListener> vanillaButtons = wrappedEntry.children();
        List<Button> buttons = new ArrayList<>();
        for (IGuiEventListener button : vanillaButtons) {
            buttons.add((Button) button);
        }
        buttons.add(holdToggleButton);
        return buttons;
    }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        List<? extends IGuiEventListener> buttons = children();
        for (IGuiEventListener button : buttons) {
            if (button.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        List<? extends IGuiEventListener> buttons = children();
        for (IGuiEventListener button : buttons) {
            if (button.mouseReleased(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        
        return false;
    }
}
