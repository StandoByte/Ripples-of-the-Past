package com.github.standobyte.jojo.client.controls;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.ui.actionshud.QuickAccess.QuickAccessKeyConflictContext;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.KeyModifier;

public class ActionKeybindEntry {
    private PressActionType type;
    private ResourceLocation actionId;
    private KeyModifier keyModifier;
    private InputMappings.Input keyCode;
    
    private Action<?> action;
    private KeyBinding keybind;
    
    public transient int delay;
    
    ActionKeybindEntry(PressActionType type, ResourceLocation actionId, String keySaveDesc) {
        this.type = type;
        this.actionId = actionId;
        this.action = null;
        if (keySaveDesc.indexOf(':') != -1) {
            String[] pts = keySaveDesc.split(":");
            keyModifier = KeyModifier.valueFromString(pts[1]);
            keyCode = InputMappings.getKey(pts[0]);
        } else {
            keyModifier = KeyModifier.NONE;
            keyCode = InputMappings.getKey(keySaveDesc);
        }
    }
    
    ActionKeybindEntry(PressActionType type, Action<?> action, InputMappings.Type inputType, int key) {
        this.type = type;
        this.actionId = action.getRegistryName();
        this.action = action;
        this.keyModifier = KeyModifier.NONE;
        this.keyCode = inputType.getOrCreate(key);
        this.keybind = createNewKey(keyModifier, keyCode);
    }
    
    ActionKeybindEntry(PressActionType type, ResourceLocation actionId, InputMappings.Type inputType, int key) {
        this.type = type;
        this.actionId = actionId;
        this.action = null;
        this.keyModifier = KeyModifier.NONE;
        this.keyCode = inputType.getOrCreate(key);
        this.keybind = createNewKey(keyModifier, keyCode);
    }
    
    void init() {
        Action<?> action = JojoCustomRegistries.ACTIONS.fromId(this.actionId);
        if (action != null) {
            KeyBinding keyBinding = createNewKey(keyModifier, keyCode);
            this.action = action;
            this.keybind = keyBinding;
        }
    }
    
    public boolean isValid() {
        return action != null && keybind != null;
    }
    
    public void setAction(Action<?> action) {
        this.action = action;
        this.actionId = action.getRegistryName();
    }
    
    public void setKeybind(InputMappings.Type inputType, int key) {
        this.keyModifier = KeyModifier.NONE;
        this.keyCode = inputType.getOrCreate(key);
        removeKeybindFromMap();
        this.keybind = createNewKey(keyModifier, keyCode);
    }
    
    public void removeKeybindFromMap() {
        if (keybind != null) {
            ClientReflection.getAllKeybindingMap().remove(keybind.getName());
            InputHandler.getInstance().keyBindingMap.removeKey(keybind);
        }
    }
    
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.name());
        json.addProperty("action", actionId.toString());
        json.addProperty("keybind", keyCode.getName() + (keyModifier != KeyModifier.NONE ? ":" + keyModifier : ""));
        return json;
    }
    
    public static ActionKeybindEntry fromJson(JsonElement json) {
        JsonObject jsonObj = json.getAsJsonObject();
        PressActionType type = Enum.valueOf(PressActionType.class, jsonObj.get("type").getAsString());
        ResourceLocation action = new ResourceLocation(jsonObj.get("action").getAsString());
        String keySaveDesc = jsonObj.get("keybind").getAsString();
        return new ActionKeybindEntry(type, action, keySaveDesc);
    }
    
    public Action<?> getAction() {
        return action;
    }
    
    public ResourceLocation getActionId() {
        return actionId;
    }
    
    public KeyBinding getKeybind() {
        return keybind;
    }
    
    public void setKeyModifierAndCode(KeyModifier keyModifier, InputMappings.Input keyCode) {
        this.keyModifier = keyModifier;
        this.keyCode = keyCode;
        keybind.setKeyModifierAndCode(keyModifier, keyCode);
    }
    
    public PressActionType getType() {
        return type;
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
    
//    static KeyBinding createNewKeyBlank() {
//        return createNewKey(InputMappings.Type.KEYSYM, -1);
//    }
    
}
