package com.github.standobyte.jojo.client.controls;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.power.IPower;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class ControlScheme {
    public static final int ARBITRARY_MAX_HOTBAR_LENGTH = 16;
    private final Map<ResourceLocation, ActionKeybindEntry> declaredKeybinds = new LinkedHashMap<>();
    private final List<ActionKeybindEntry> validKeybindsCache = new ArrayList<>();
    
    private final Map<Hotbar, ActionsHotbar> hotbars = Util.make(new EnumMap<>(Hotbar.class), map -> {
        for (Hotbar hotbar : Hotbar.values()) {
            map.put(hotbar, new ActionsHotbar());
        }
    });

    private boolean initialized = false;
    public boolean initLoadedFromConfig(IPower<?, ?> power) {
        if (initialized) return false;
        
        for (ActionKeybindEntry keybind : declaredKeybinds.values()) {
            keybind.init();
        }
        
        for (Hotbar hotbarType : Hotbar.values()) {
            ActionsHotbar hotbarObj = hotbars.get(hotbarType);
            hotbarObj.init();
        }
        
        initialized = true;
        updateCache();
        return true;
    }

    private void updateCache() {
        validKeybindsCache.clear();
        for (ActionKeybindEntry keybind : declaredKeybinds.values()) {
            if (keybind.isValid()) {
                validKeybindsCache.add(keybind);
            }
        }
    }

    // add legal actions that are missing in declaredKeybinds (to both cache and declaredKeybinds)
    // do not add illegal actions from declaredKeybinds
    public void update(IPower<?, ?> power) {
        // update control scheme when new actions need to be added to HUD
        // ex.: Hamon techniques
        InputHandler.toDoDeleteMe();
        updateCache();
    }
    
    static ControlScheme fromJson(JsonElement json) {
        JsonObject jsonObj = json.getAsJsonObject();
        ControlScheme obj = new ControlScheme();

        JsonArray keybindsJson = jsonObj.get("customKeybinds").getAsJsonArray();
        for (JsonElement keybindJson : keybindsJson) {
            ActionKeybindEntry keybind = ActionKeybindEntry.fromJson(keybindJson);
            obj.declaredKeybinds.put(keybind.getActionId(), keybind);
        }

        JsonObject hotbarsJson = jsonObj.get("hotbars").getAsJsonObject();
        for (ControlScheme.Hotbar hotbar : ControlScheme.Hotbar.values()) {
            JsonObject hotbarJson = hotbarsJson.get(hotbar.name()).getAsJsonObject();
            obj.hotbars.get(hotbar).fromJson(hotbarJson);
        }

        return obj;
    }

    JsonElement toJson() {
        JsonObject json = new JsonObject();

        JsonArray keybindsJson = new JsonArray();
        json.add("customKeybinds", keybindsJson);
        for (ActionKeybindEntry keybind : declaredKeybinds.values()) {
            keybindsJson.add(keybind.toJson());
        }

        JsonObject hotbarsJson = new JsonObject();
        json.add("hotbars", hotbarsJson);
        for (Hotbar hotbar : Hotbar.values()) {
            JsonElement hotbarJson = hotbars.get(hotbar).toJson();
            hotbarsJson.add(hotbar.name(), hotbarJson);
        }
        return json;
    }
    
    
    
    public void reset() {
        // reset the underlying collections to their default state
        updateCache();
    }
    
    
    
    public Iterable<ActionKeybindEntry> getCustomKeybinds() {
        return validKeybindsCache;
    }
    
    public ActionsHotbar getActionsHotbar(Hotbar hotbarType) {
        return hotbars.get(hotbarType);
    }
    
    public ActionKeybindEntry addKeybindEntry(ActionKeybindEntry.PressActionType pressType, 
            Action<?> action, int key) {
        return addKeybindEntry(pressType, action, InputMappings.Type.KEYSYM, key);
    }
    
    public ActionKeybindEntry addKeybindEntry(ActionKeybindEntry.PressActionType pressType, 
            Action<?> action, InputMappings.Type inputType, int key) {
        ActionKeybindEntry keybind = new ActionKeybindEntry(pressType, action, inputType, key);
        declaredKeybinds.put(action.getRegistryName(), keybind);
        updateCache();
        return keybind;
    }
    
    
    
    public enum Hotbar {
        LEFT_CLICK,
        RIGHT_CLICK
    }
    
    
    
    public static ControlScheme defaultFromPowerType(
            Action<?>[] leftClickActions, 
            Action<?>[] rightClickActions, 
            DefaultKey... keyBindings) {
        ControlScheme obj = new ControlScheme();

        ActionsHotbar lmbHotbar = obj.hotbars.get(Hotbar.LEFT_CLICK);
        for (Action<?> action : leftClickActions) {
            lmbHotbar.addActionPreInit(new ActionVisibilitySwitch(
                    lmbHotbar, action.getRegistryName(), action.enabledInHudDefault()));
        }
        
        ActionsHotbar rmbHotbar = obj.hotbars.get(Hotbar.RIGHT_CLICK);
        for (Action<?> action : rightClickActions) {
            rmbHotbar.addActionPreInit(new ActionVisibilitySwitch(
                    rmbHotbar, action.getRegistryName(), action.enabledInHudDefault()));
        }
        
        for (DefaultKey keyBinding : keyBindings) {
            obj.declaredKeybinds.put(keyBinding.action.getRegistryName(), 
                    new ActionKeybindEntry(ActionKeybindEntry.PressActionType.CLICK, 
                            keyBinding.action.getRegistryName(), keyBinding.keyDesc));
        }
        
        return obj;
    }
    
    public static class DefaultKey {
        final Action<?> action;
        final String keyDesc;
        
        private DefaultKey(Action<?> action, String keyDesc) {
            this.action = action;
            this.keyDesc = keyDesc;
        }
        
        public static DefaultKey of(Action<?> action, String keyDesc) {
            return new DefaultKey(action, keyDesc);
        }
        
        public static DefaultKey mmb(Action<?> action) {
            return new DefaultKey(action, "key.mouse.middle");
        }
    }
}
