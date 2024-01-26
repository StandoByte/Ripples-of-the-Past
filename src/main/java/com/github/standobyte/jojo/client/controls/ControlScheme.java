package com.github.standobyte.jojo.client.controls;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.power.IPower;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class ControlScheme {
    public static final int ARBITRARY_MAX_HOTBAR_LENGTH = 16;
    
    private final SaveState clientSaveState;
    
    private final Map<ResourceLocation, ActionKeybindEntry> customKeybinds = new LinkedHashMap<>();
    private final Map<Hotbar, ActionsHotbar> hotbars = Util.make(new EnumMap<>(Hotbar.class), map -> {
        for (Hotbar hotbar : Hotbar.values()) {
            map.put(hotbar, new ActionsHotbar());
        }
    });
    
    private ControlScheme(SaveState clientSaveState, IPower<?, ?> power) {
        this.clientSaveState = clientSaveState;
        // add legal actions that are missing in save state
        // do not add illegal actions from save state (but keep them there)
        InputHandler.toDoDeleteMe();
        
        for (ActionKeybindEntry.SaveInfo keybindData : clientSaveState.customKeybinds.values()) {
            ActionKeybindEntry keybind = keybindData.createEntry();
            if (keybind != null) {
                customKeybinds.put(keybindData.action, keybind);
            }
        }
        
        for (Hotbar hotbarType : Hotbar.values()) {
            ActionsHotbar hotbarObj = hotbars.get(hotbarType);
            for (ActionVisibilitySwitch.SaveInfo actionData : clientSaveState.hotbars.get(hotbarType).actionsOrder.values()) {
                ActionVisibilitySwitch actionSwitch = actionData.createSwitch();
                if (actionSwitch != null) {
                    hotbarObj.actions.put(actionData.action, actionSwitch);
                }
            }
            hotbarObj.updateCache();
        }
    }
    
    public void update(IPower<?, ?> power) {
        // update control scheme when new actions need to be added to HUD
        // ex.: Hamon techniques
        // always update clientSaveState when editing this object as well
        InputHandler.toDoDeleteMe();
    }
    
    public void reset() {
        // reset button - reset the collections to their default state
    }
    
    
    public Iterable<ActionKeybindEntry> getCustomKeybinds() {
        return customKeybinds.values();
    }
    
    
    
    public ActionsHotbar getActionsHotbar(Hotbar hotbarType) {
        return hotbars.get(hotbarType);
    }
    
    public enum Hotbar {
        LEFT_CLICK,
        RIGHT_CLICK
    }
    
    
    
    public static class SaveState {
        private final Map<ResourceLocation, ActionKeybindEntry.SaveInfo> customKeybinds = new LinkedHashMap<>();
        private final Map<Hotbar, ActionsHotbar.SaveState> hotbars = Util.make(new EnumMap<>(Hotbar.class), map -> {
            for (Hotbar hotbar : Hotbar.values()) {
                map.put(hotbar, new ActionsHotbar.SaveState());
            }
        });
        
        private SaveState() {}
        
        
        public static SaveState fromJson(JsonElement json) {
            JsonObject jsonObj = json.getAsJsonObject();
            SaveState obj = new SaveState();
            
            JsonArray keybindsJson = jsonObj.get("customKeybinds").getAsJsonArray();
            for (JsonElement keybindJson : keybindsJson) {
                ActionKeybindEntry.SaveInfo keybind = ActionKeybindEntry.SaveInfo.fromJson(keybindJson);
                obj.customKeybinds.put(keybind.action, keybind);
            }
            
            JsonObject hotbarsJson = jsonObj.get("hotbars").getAsJsonObject();
            for (ControlScheme.Hotbar hotbar : ControlScheme.Hotbar.values()) {
                JsonObject hotbarJson = hotbarsJson.get(hotbar.name()).getAsJsonObject();
                obj.hotbars.get(hotbar).fromJson(hotbarJson);
            }
            
            return obj;
        }
        
        public JsonElement toJson() {
            JsonObject json = new JsonObject();
            
            JsonArray keybindsJson = new JsonArray();
            json.add("customKeybinds", keybindsJson);
            for (ActionKeybindEntry.SaveInfo keybind : customKeybinds.values()) {
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
        
        public ControlScheme createControlScheme(IPower<?, ?> power) {
            return new ControlScheme(this, power);
        }
        
        
        
        public static SaveState defaultFromPowerType(
                Action<?>[] leftClickActions, 
                Action<?>[] rightClickActions, 
                DefaultKey... keyBindings) {
            SaveState obj = new SaveState();

            ActionsHotbar.SaveState lmbHotbar = obj.hotbars.get(Hotbar.LEFT_CLICK);
            for (Action<?> action : leftClickActions) {
                lmbHotbar.actionsOrder.put(action.getRegistryName(), 
                        new ActionVisibilitySwitch.SaveInfo(action.getRegistryName(), action.enabledInHudDefault()));
            }
            
            ActionsHotbar.SaveState rmbHotbar = obj.hotbars.get(Hotbar.RIGHT_CLICK);
            for (Action<?> action : rightClickActions) {
                rmbHotbar.actionsOrder.put(action.getRegistryName(), 
                        new ActionVisibilitySwitch.SaveInfo(action.getRegistryName(), action.enabledInHudDefault()));
            }
            
            for (DefaultKey keyBinding : keyBindings) {
                obj.customKeybinds.put(keyBinding.action.getRegistryName(), 
                        new ActionKeybindEntry.SaveInfo(ActionKeybindEntry.PressActionType.CLICK, 
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
}
