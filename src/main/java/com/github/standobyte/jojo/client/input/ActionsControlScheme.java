package com.github.standobyte.jojo.client.input;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ui.actionshud.QuickAccess.QuickAccessKeyConflictContext;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.layout.ActionsLayout;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;

public class ActionsControlScheme {
    public final ResourceLocation powerTypeId;
    private OptionalInt savedSchemeNumber = OptionalInt.empty();
    private final List<KeybindEntry> customKeybindEntries = new ArrayList<>();
    
    public ActionsControlScheme(ActionsLayout<?> hotbarsLayout, ResourceLocation powerTypeId) {
        this.hotbarsLayout = hotbarsLayout;
        this.powerTypeId = powerTypeId;
    }
    
    private static ActionsControlScheme createDefault(IPowerType<?, ?> powerType) {
        ActionsControlScheme controlScheme = new ActionsControlScheme(
                powerType.createDefaultLayout(), powerType.getRegistryName());
        
        Action<?> mmbAction = powerType.createDefaultLayout().mmbActionStarting;
        if (mmbAction != null) {
            controlScheme.addKeyBindingEntry(Type.CLICK, mmbAction, 
                    InputMappings.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
        }
        
        return controlScheme;
    }
    
    public void clearInvalidKeybinds() {
        Iterator<KeybindEntry> iter = customKeybindEntries.iterator();
        while (iter.hasNext()) {
            KeybindEntry entry = iter.next();
            if (entry.action == null || entry.keybind.isUnbound() || entry.type == null) {
                iter.remove();
            }
        }
    }

    private static final AtomicInteger KEY_ID = new AtomicInteger();
    public KeybindEntry addKeyBindingEntry(ActionsControlScheme.Type type, Action<?> action, int key) {
        return addKeyBindingEntry(type, action, InputMappings.Type.KEYSYM, key);
    }

    public KeybindEntry addKeyBindingEntry(ActionsControlScheme.Type type, Action<?> action, InputMappings.Type inputType, int key) {
        KeyBinding keyBinding = new KeyBinding(
                JojoMod.MOD_ID + ".key.action." + String.valueOf(KEY_ID.getAndIncrement()), 
                QuickAccessKeyConflictContext.INSTANCE, 
                inputType, key, 
                "key.categories." + JojoMod.MOD_ID + ".custom_keybinds");
        KeybindEntry entry = new KeybindEntry(type, action, keyBinding);
        customKeybindEntries.add(entry);
        return entry;
    }
    
    public Iterable<KeybindEntry> getEntriesView() {
        return customKeybindEntries;
    }
    
    public static class KeybindEntry {
        public Type type;
        public Action<?> action;
        public KeyBinding keybind;
        public int delay;
        
        public KeybindEntry(Type type, Action<?> action, KeyBinding keybind) {
            this.type = type;
            this.action = action;
            this.keybind = keybind;
        }
    }
    
    public enum Type {
        SELECT,
        CLICK
    }
    
    
    
    private final ActionsLayout<?> hotbarsLayout;
    
    public ActionsLayout<?> getHotbarsLayout() {
        return hotbarsLayout;
    }
    
    
    
    
    private static Map<PowerClassification, SavedControlSchemes> controlSchemeCache = new EnumMap<>(PowerClassification.class);
    
    public static SavedControlSchemes getCtrlSchemes(PowerClassification power) {
        return controlSchemeCache.get(power);
    }
    
    public static ActionsControlScheme getCurrentCtrlScheme(PowerClassification power) {
        SavedControlSchemes allSchemes = getCtrlSchemes(power);
        return allSchemes != null ? allSchemes.getCurrentCtrlScheme() : null;
    }
    
    public static <P extends IPower<P, ?>> ActionsLayout<P> getHotbarsLayout(P power) {
        ActionsControlScheme controlScheme = getCurrentCtrlScheme(power.getPowerClassification());
        return controlScheme != null ? (ActionsLayout<P>) controlScheme.getHotbarsLayout() : null;
    }
    
    public static void cacheCtrlScheme(PowerClassification power, IPowerType<?, ?> powerType) {
        if (powerType != null) {
            controlSchemeCache.put(power, getCtrlSchemesFor(powerType));
        }
        else {
            controlSchemeCache.remove(power);
        }
    }
    
    
    
    private static final Map<ResourceLocation, SavedControlSchemes> savedControlSchemes = new HashMap<>();
    
    public static SavedControlSchemes getCtrlSchemesFor(IPowerType<?, ?> type) {
        ResourceLocation id = type.getRegistryName();
        SavedControlSchemes ctrlSchemesHolder = savedControlSchemes.get(id);
        if (ctrlSchemesHolder == null) {
            ctrlSchemesHolder = new SavedControlSchemes(createDefault(type));
            savedControlSchemes.put(id, ctrlSchemesHolder);
        }
        return ctrlSchemesHolder;
    }
    
    public static class SavedControlSchemes {
        private ActionsControlScheme currentControlScheme;
        
        SavedControlSchemes(ActionsControlScheme controlScheme) {
            setCurrentCtrlScheme(controlScheme);
        }
        
        void setCurrentCtrlScheme(ActionsControlScheme controlScheme) {
            Objects.requireNonNull(controlScheme);
            this.currentControlScheme = controlScheme;
        }
        
        public ActionsControlScheme getCurrentCtrlScheme() {
            return currentControlScheme;
        }
    }
}
