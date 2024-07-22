package com.github.standobyte.jojo.client.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class ControlScheme {
    public static final ControlScheme EMPTY = new ControlScheme();
    
    public static final int ARBITRARY_MAX_HOTBAR_LENGTH = 19;
    
    @Nullable
    private DefaultControls defaultState;
    
    private final List<ActionKeybindEntry> serializedKeybinds = new ArrayList<>();
    private final List<ActionKeybindEntry> legalKeybinds = new ArrayList<>();
    
    private final List<ActionKeybindEntry> keybindsView = Collections.unmodifiableList(legalKeybinds);
    
    private final Map<Hotbar, ActionsHotbar> hotbars = Util.make(new EnumMap<>(Hotbar.class), map -> {
        for (Hotbar hotbar : Hotbar.values()) {
            map.put(hotbar, new ActionsHotbar());
        }
    });
    
    private ControlScheme() {}
    
    private ControlScheme(IPowerType<?, ?> powerType) {
        if (powerType != null) {
            this.defaultState = powerType.clCreateDefaultLayout();
        }
    }
    
    private ControlScheme(DefaultControls defaults) {
        this.defaultState = defaults;
    }
    
    private boolean initialized = false;
    public boolean initLoadedFromConfig(IPower<?, ?> power) {
        if (initialized) return false;
        
        for (ActionKeybindEntry keybind : serializedKeybinds) {
            keybind.init();
        }
        
        for (Hotbar hotbarType : Hotbar.values()) {
            ActionsHotbar hotbarObj = hotbars.get(hotbarType);
            hotbarObj.init();
        }
        
        initialized = true;
        return true;
    }
    
    // add legal actions that are missing in serializedKeybinds (to both cache and serializedKeybinds)
    // do not add illegal actions from serializedKeybinds
    //
    // update control scheme when new actions need to be added to HUD
    // ex.: Hamon techniques
    <P extends IPower<P, T>, T extends IPowerType<P, T>> void update(IPower<?, ?> power) {
        if (power.hasPower()) {
            P powerObj = (P) power;
            T type = powerObj.getType();
            sanitizeControls(powerObj, type);
        }
    }
    
    <P extends IPower<P, T>, T extends IPowerType<P, T>> void sanitizeControls(P power, T powerType) {
        this.legalKeybinds.clear();
        for (ActionKeybindEntry keybind : serializedKeybinds) {
           Action<P> action = (Action<P>) keybind.getAction();
           if (keybind.isValid() && action != null && powerType.isActionLegalInHud(action, power)
                   && (ClientModSettings.getSettingsReadOnly().showLockedSlots || action.isUnlocked(power))) {
               legalKeybinds.add(keybind);
           }
        }
        
        
        
        for (ControlScheme.Hotbar hotbarType : ControlScheme.Hotbar.values()) {
            ActionsHotbar hotbar = this.hotbars.get(hotbarType);
            hotbar.legalSwitches.clear();
            
            for (ActionVisibilitySwitch declaredAction : hotbar.serializedSwitches) {
                Action<P> action = (Action<P>) declaredAction.getAction();
                if (action != null && powerType.isActionLegalInHud((Action<P>) action, power)
                        && (ClientModSettings.getSettingsReadOnly().showLockedSlots || action.isUnlocked(power))) {
                    hotbar.legalSwitches.add(declaredAction);
                }
            }
        }
        
        powerType.clAddMissingActions(this, power);
        
        for (ActionsHotbar hotbar : hotbars.values()) {
            hotbar.updateCache();
        }
    }
    
    public void addIfMissing(Hotbar hotbarType, Action<?> action) {
        if (!hotbars.entrySet().stream()
                .flatMap(entry -> entry.getValue().serializedSwitches.stream())
                .filter(sw -> sw.getAction() == action).findAny().isPresent()) {
            ActionVisibilitySwitch actionSwitch = this.getActionsHotbar(hotbarType).addActionAsSerialized(action);
            hotbars.get(hotbarType).legalSwitches.add(actionSwitch);
        }
    }
    
    
    
    public void reset(IPower<?, ?> power) {
        if (defaultState != null) {
            for (Hotbar hotbarType : Hotbar.values()) {
                ActionsHotbar hotbar = hotbars.get(hotbarType);
                hotbar.serializedSwitches.clear();
                for (Action<?> action : defaultState.hotbars.get(hotbarType)) {
                    hotbar.addActionAsSerialized(action);
                }
            }
            
            for (ActionKeybindEntry keybind : this.serializedKeybinds) {
                keybind.removeKeybindFromMap();
            }
            this.serializedKeybinds.clear();
            for (DefaultControls.DefaultKey keyInfo : defaultState.keyBindings) {
                ActionKeybindEntry keyBind = new ActionKeybindEntry(keyInfo.action.getRegistryName(), keyInfo.keyDesc);
                serializedKeybinds.add(keyBind);
                keyBind.init();
            }
            
            update(power);
        }
    }
    
    public static ControlScheme createNewFromDefault(DefaultControls defaultControls) {
        ControlScheme obj = new ControlScheme(defaultControls);
        
        for (Hotbar hotbarType : Hotbar.values()) {
            ActionsHotbar hotbar = obj.hotbars.get(hotbarType);
            for (Action<?> action : defaultControls.hotbars.get(hotbarType)) {
                hotbar.addActionAsSerialized(action);
            }
        }
        
        for (DefaultControls.DefaultKey keyBinding : defaultControls.keyBindings) {
            obj.serializedKeybinds.add(new ActionKeybindEntry(keyBinding.action.getRegistryName(), keyBinding.keyDesc));
        }
        
        return obj;
    }
    
    public static class DefaultControls {
        final Map<Hotbar, Action<?>[]> hotbars = new EnumMap<>(Hotbar.class);
        final List<DefaultKey> keyBindings = new ArrayList<>();
        
        public DefaultControls(
                Action<?>[] leftClickActions, 
                Action<?>[] rightClickActions, 
                DefaultKey... keyBindings) {
            this.hotbars.put(Hotbar.LEFT_CLICK, leftClickActions);
            this.hotbars.put(Hotbar.RIGHT_CLICK, rightClickActions);
            Collections.addAll(this.keyBindings, keyBindings);
        }
        
        public void addKey(DefaultKey key) {
            this.keyBindings.add(key);
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
    
    
    
    public Iterable<ActionKeybindEntry> getCustomKeybinds() {
        return keybindsView;
    }
    
    public ActionKeybindEntry addBlankKeybindEntry() {
        return addKeybindEntry(new ActionKeybindEntry(new ResourceLocation("blank"), InputMappings.Type.KEYSYM, -1));
    }
    
    public ActionKeybindEntry addKeybindEntry(Action<?> action, int key) {
        return addKeybindEntry(action, InputMappings.Type.KEYSYM, key);
    }
    
    public ActionKeybindEntry addKeybindEntry(Action<?> action, InputMappings.Type inputType, int key) {
        return addKeybindEntry(new ActionKeybindEntry(action, inputType, key));
    }
    
    private ActionKeybindEntry addKeybindEntry(ActionKeybindEntry keybind) {
        legalKeybinds.add(keybind);
        serializedKeybinds.add(keybind);
        return keybind;
    }

    // before removing an ActionKeybindEntry, call ActionKeybindEntry#removeKeybindFromMap
    // to not flood the key bindings map with keybinds that will not be used anymore
    public boolean removeKeybindEntry(ActionKeybindEntry keybind) {
        if (legalKeybinds.remove(keybind)) {
            serializedKeybinds.remove(keybind);
            keybind.removeKeybindFromMap();
            return true;
        }
        
        return false;
    }
    

    
    public ActionsHotbar getActionsHotbar(Hotbar hotbarType) {
        return hotbars.get(hotbarType);
    }
    
    public enum Hotbar {
        LEFT_CLICK,
        RIGHT_CLICK
    }
    
    
    
    static ControlScheme fromJson(JsonElement json, @Nullable ResourceLocation powerTypeId) {
        ControlScheme obj;
        if (powerTypeId != null) {
            Optional<IPowerType<?, ?>> powerType = Optional.ofNullable(JojoCustomRegistries.NON_STAND_POWERS.fromId(powerTypeId));
            // Optional#or was only added in Java 9
            if (!powerType.isPresent()) powerType = Optional.ofNullable(JojoCustomRegistries.STANDS.fromId(powerTypeId));
            obj = new ControlScheme(powerType.orElse(null));
        }
        else {
            obj = new ControlScheme();
        }
        
        JsonObject jsonObj = json.getAsJsonObject();

        JsonArray keybindsJson = jsonObj.get("customKeybinds").getAsJsonArray();
        for (JsonElement keybindJson : keybindsJson) {
            ActionKeybindEntry keybind = ActionKeybindEntry.fromJson(keybindJson);
            obj.serializedKeybinds.add(keybind);
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
        for (ActionKeybindEntry keybind : serializedKeybinds) {
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
}
