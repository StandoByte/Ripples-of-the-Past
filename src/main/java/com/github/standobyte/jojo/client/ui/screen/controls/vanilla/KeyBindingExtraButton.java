package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.util.text.ITextComponent;

public class KeyBindingExtraButton extends Button {
//    private final KeyBindingList.KeyEntry keyEntry;
    private final Widget keyEntryButton;

    public KeyBindingExtraButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage,
            IPressable pOnPress, KeyBindingList.KeyEntry keyEntry) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
//        this.keyEntry = keyEntry;
        this.keyEntryButton = ClientReflection.getChangeButton(keyEntry);
    }
    
    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        this.y = keyEntryButton.y;
        super.renderButton(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
    
}
