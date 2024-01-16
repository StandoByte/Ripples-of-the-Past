package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ControlSettingToggleButton extends Button {
    private final Supplier<Boolean> settingGetter;

    public ControlSettingToggleButton(int pWidth, int pHeight, 
            Button.IPressable onPress, Supplier<Boolean> settingGetter) {
        super(-1, -1, pWidth, pHeight, StringTextComponent.EMPTY, onPress);
        this.settingGetter = settingGetter;
        setMessageFromSetting(settingGetter.get());
    }
    
    public void setMessageFromSetting(boolean setting) {
        setMessage(new TranslationTextComponent(setting ? "options.key.toggle" : "options.key.hold"));
    }
    
    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        setMessageFromSetting(settingGetter.get());
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
