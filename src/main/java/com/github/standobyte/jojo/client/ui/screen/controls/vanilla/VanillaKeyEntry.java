package com.github.standobyte.jojo.client.ui.screen.controls.vanilla;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class VanillaKeyEntry extends KeyBindingList.Entry {
    private final KeyBinding key;
    private final ITextComponent name;
    final Button changeButton;
    private final Button resetButton;
    
    private final Minecraft mc;
    private final int maxNameWidth;
    
    private final Supplier<KeyBinding> getSelectedKey;
//    private final Consumer<KeyBinding> setSelectedKey;
    
    public VanillaKeyEntry(KeyBinding key, 
            ControlsScreen controlsScreen, KeyBindingList controlsList) {
        this(key, () -> controlsScreen.selectedKey, k -> controlsScreen.selectedKey = k, 
                ClientReflection.getMaxNameWidth(controlsList));
    }

    public VanillaKeyEntry(KeyBinding key, 
            Supplier<KeyBinding> getSelectedKey, Consumer<KeyBinding> setSelectedKey, int maxNameWidth) {
        this(key, new TranslationTextComponent(key.getName()), getSelectedKey, setSelectedKey, maxNameWidth);
    }

    public VanillaKeyEntry(KeyBinding key, ITextComponent name, 
            Supplier<KeyBinding> getSelectedKey, Consumer<KeyBinding> setSelectedKey, int maxNameWidth) {
        this.key = key;
        this.name = name;
        this.getSelectedKey = getSelectedKey;
//        this.setSelectedKey = setSelectedKey;
        this.maxNameWidth = maxNameWidth;
        this.mc = Minecraft.getInstance();
        this.changeButton = new Button(0, 0, 75 + 20, 20, name, button -> {
            setSelectedKey.accept(key);
        }) {
            protected IFormattableTextComponent createNarrationMessage() {
                return key.isUnbound() ? new TranslationTextComponent("narrator.controls.unbound", name) : new TranslationTextComponent("narrator.controls.bound", name, super.createNarrationMessage());
            }
        };
        this.resetButton = new Button(0, 0, 50, 20, new TranslationTextComponent("controls.reset"), (p_214387_2_) -> {
            key.setToDefault();
            mc.options.setKey(key, key.getDefaultKey());
            KeyBinding.resetMapping();
        }) {
            protected IFormattableTextComponent createNarrationMessage() {
                return new TranslationTextComponent("narrator.controls.reset", name);
            }
        };
    }

    public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, 
            int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        boolean isSelected = getSelectedKey.get() == this.key;
        mc.font.draw(pMatrixStack, name, 
                (float)(pLeft + 90 - maxNameWidth), (float)(pTop + pHeight / 2 - 9 / 2), 
                0xFFFFFF);
        this.resetButton.x = pLeft + 190 + 20;
        this.resetButton.y = pTop;
        this.resetButton.active = !this.key.isDefault();
        this.resetButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.changeButton.x = pLeft + 105;
        this.changeButton.y = pTop;
        this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
        boolean isConflicting = false;
        boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
        if (!this.key.isUnbound()) {
            for(KeyBinding keybinding : mc.options.keyMappings) {
                if (keybinding != this.key && this.key.same(keybinding)) {
                    isConflicting = true;
                    keyCodeModifierConflict &= keybinding.hasKeyCodeModifierConflict(this.key);
                }
            }
        }

        if (isSelected) {
            this.changeButton.setMessage(
                    new StringTextComponent("> ")
                    .append(this.changeButton.getMessage().copy().withStyle(TextFormatting.YELLOW))
                    .append(" <").withStyle(TextFormatting.YELLOW));
        } else if (isConflicting) {
            this.changeButton.setMessage(
                    this.changeButton.getMessage().copy()
                    .withStyle(keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED));
        }

        this.changeButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    public List<? extends IGuiEventListener> children() {
        return ImmutableList.of(this.changeButton, this.resetButton);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.changeButton.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        } else {
            return this.resetButton.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return this.changeButton.mouseReleased(pMouseX, pMouseY, pButton)
                || this.resetButton.mouseReleased(pMouseX, pMouseY, pButton);
    }

}
