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
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.KeyModifier;

public class ActionKeybindEntry {
    private ResourceLocation actionId;
    private KeyModifier keyModifier;
    private InputMappings.Input keyCode;
    
    private OnKeyPress onKeyPress = OnKeyPress.PERFORM;
    private KeyActiveType hudInteraction = KeyActiveType.INSIDE_HUD;
    private boolean isVisibleInHud = false;
    
    private Action<?> action;
    private KeyBinding keybind;
    
    public transient int delay;
    
    ActionKeybindEntry(ResourceLocation actionId, String keySaveDesc) {
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
    
    ActionKeybindEntry(Action<?> action, InputMappings.Type inputType, int key) {
        this.actionId = action.getRegistryName();
        this.action = action;
        this.keyModifier = KeyModifier.NONE;
        this.keyCode = inputType.getOrCreate(key);
        this.keybind = createNewKey(keyModifier, keyCode);
    }
    
    ActionKeybindEntry(ResourceLocation actionId, InputMappings.Type inputType, int key) {
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
    
    public void setOnPress(OnKeyPress onKeyPress) {
        if (onKeyPress != null) {
            this.onKeyPress = onKeyPress;
        }
    }
    
    public void setHudInteraction(KeyActiveType hudInteraction) {
        if (hudInteraction != null) {
            this.hudInteraction = hudInteraction;
        }
    }
    
    public void setVisibleInHud(boolean visible) {
        this.isVisibleInHud = visible;
    }
    
    public boolean isVisibleInHud() {
        return isVisibleInHud;
    }
    
    public void removeKeybindFromMap() {
        if (keybind != null) {
            ClientReflection.getAllKeybindingMap().remove(keybind.getName());
            InputHandler.getInstance().keyBindingMap.removeKey(keybind);
        }
    }
    
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("action", actionId.toString());
        json.addProperty("keybind", keyCode.getName() + (keyModifier != KeyModifier.NONE ? ":" + keyModifier : ""));
        json.addProperty("onKeyPress", onKeyPress.name());
        json.addProperty("withHud", hudInteraction.name());
        json.addProperty("hudIcon", isVisibleInHud);
        return json;
    }
    
    public static ActionKeybindEntry fromJson(JsonElement json) {
        JsonObject jsonObj = json.getAsJsonObject();
        ResourceLocation action = new ResourceLocation(jsonObj.get("action").getAsString());
        String keySaveDesc = jsonObj.get("keybind").getAsString();
        ActionKeybindEntry entry = new ActionKeybindEntry(action, keySaveDesc);
        try { entry.setOnPress(Enum.valueOf(OnKeyPress.class, jsonObj.get("onKeyPress").getAsString())); } catch (Exception notSpecified) {}
        try { entry.setHudInteraction(Enum.valueOf(KeyActiveType.class, jsonObj.get("withHud").getAsString())); } catch (Exception notSpecified) {}
        try { entry.setVisibleInHud(JSONUtils.getAsBoolean(jsonObj, "hudIcon")); } catch (Exception notSpecified) {}
        return entry;
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
    
    public OnKeyPress getOnKeyPress() {
        return onKeyPress;
    }
    
    public KeyActiveType getHudInteraction() {
        return hudInteraction;
    }
    
    public enum OnKeyPress {
        PERFORM,
        SELECT
    }
    
    public enum KeyActiveType {
        INSIDE_HUD  { @Override public boolean canTrigger(boolean isHudActive) { return isHudActive; }},
        OUTSIDE_HUD { @Override public boolean canTrigger(boolean isHudActive) { return !isHudActive; }},
        ALWAYS      { @Override public boolean canTrigger(boolean isHudActive) { return true; }};
        
        public abstract boolean canTrigger(boolean isHudActive);
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
