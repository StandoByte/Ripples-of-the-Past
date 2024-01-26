package com.github.standobyte.jojo.client.controls;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ui.actionshud.QuickAccess.QuickAccessKeyConflictContext;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.KeyModifier;

public class ActionKeybindEntry {
    public PressActionType type;
    public Action<?> action;
    public KeyBinding keybind;
    public transient int delay;
    
    public ActionKeybindEntry(PressActionType type, Action<?> action, KeyBinding keybind) {
        this.type = type;
        this.action = action;
        this.keybind = keybind;
    }
    
    public enum PressActionType {
        SELECT,
        CLICK
    }
    
    private static final AtomicInteger KEY_ID = new AtomicInteger();
    static KeyBinding createNewKey(InputMappings.Type inputType, int key) {
        return createNewKey(KeyModifier.NONE, inputType, key);
    }
    
    static KeyBinding createNewKey(KeyModifier modifier, InputMappings.Type inputType, int key) {
        return createNewKey(modifier, inputType.getOrCreate(key));
    }
    
    static KeyBinding createNewKey(KeyModifier modifier, InputMappings.Input keyCode) {
        KeyBinding keyBinding = new KeyBinding(
                JojoMod.MOD_ID + ".key.action." + String.valueOf(KEY_ID.getAndIncrement()), 
                QuickAccessKeyConflictContext.INSTANCE, keyCode, 
                "key.categories." + JojoMod.MOD_ID + ".custom_keybinds");
        return keyBinding;
    }
    
    static KeyBinding createNewKeyBlank() {
        return createNewKey(InputMappings.Type.KEYSYM, -1);
    }
    
    
    
    static class SaveInfo {
        public PressActionType type;
        public ResourceLocation action;
        public KeyModifier keyModifier;
        public InputMappings.Input keyCode;
        
        public SaveInfo(PressActionType type, ResourceLocation action, 
                String keySaveDesc) {
            this.type = type;
            this.action = action;
            if (keySaveDesc.indexOf(':') != -1) {
                String[] pts = keySaveDesc.split(":");
                keyModifier = KeyModifier.valueFromString(pts[1]);
                keyCode = InputMappings.getKey(pts[0]);
            } else {
                keyModifier = KeyModifier.NONE;
                keyCode = InputMappings.getKey(keySaveDesc);
            }
        }
        
        public SaveInfo(PressActionType type, ResourceLocation action, 
                KeyModifier keyModifier, Input keyCode) {
            this.type = type;
            this.action = action;
            this.keyModifier = keyModifier;
            this.keyCode = keyCode;
        }
        
        public JsonElement toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", type.name());
            json.addProperty("action", action.toString());
            json.addProperty("keybind", keyCode.getName() + (keyModifier != KeyModifier.NONE ? ":" + keyModifier : ""));
            return json;
        }
        
        public static SaveInfo fromJson(JsonElement json) {
            JsonObject jsonObj = json.getAsJsonObject();
            PressActionType type = Enum.valueOf(PressActionType.class, jsonObj.get("type").getAsString());
            ResourceLocation action = new ResourceLocation(jsonObj.get("action").getAsString());
            String keySaveDesc = jsonObj.get("keybind").getAsString();
            return new SaveInfo(type, action, keySaveDesc);
        }
        
        @Nullable
        ActionKeybindEntry createEntry() {
            Action<?> action = JojoCustomRegistries.ACTIONS.fromId(this.action);
            if (action != null) {
                KeyBinding keyBinding = createNewKey(keyModifier, keyCode);
                return new ActionKeybindEntry(type, action, keyBinding);
            }
            return null;
        }
    }
}
