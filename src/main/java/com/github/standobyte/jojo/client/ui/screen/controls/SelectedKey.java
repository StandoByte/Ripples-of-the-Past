package com.github.standobyte.jojo.client.ui.screen.controls;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.controls.ActionKeybindEntry;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyModifier;

public class SelectedKey {
    private ActionKeybindEntry customActionKeybind;
    private KeyBinding registeredKeybind;
    
    void setKeybind(ActionKeybindEntry customActionKeybind) {
        this.customActionKeybind = customActionKeybind;
        this.registeredKeybind = null;
    }
    
    void setKeybind(KeyBinding registeredKeybind) {
        this.customActionKeybind = null;
        this.registeredKeybind = registeredKeybind;
    }
    
    void clear() {
        this.customActionKeybind = null;
        this.registeredKeybind = null;
    }
    
    boolean isEmpty() {
        return getKeybind() == null;
    }
    
    void setKeyModifierAndCode(KeyModifier keyModifier, InputMappings.Input keyCode) {
        if (customActionKeybind != null) {
            customActionKeybind.setKeyModifierAndCode(keyModifier, keyCode);
        }
        else if (registeredKeybind != null) {
            registeredKeybind.setKeyModifierAndCode(keyModifier, keyCode);
        }
    }
    
    @Nullable
    ActionKeybindEntry getCustomActionKeybind() {
        return customActionKeybind;
    }
    
    @Nullable
    KeyBinding getKeybind() {
        if (customActionKeybind != null) {
            return customActionKeybind.getKeybind();
        }
        return registeredKeybind;
    }
    
}
