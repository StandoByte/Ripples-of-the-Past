package com.github.standobyte.jojo.client.controls;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

public class ActionsHotbar {
    private Map<ResourceLocation, ActionVisibilitySwitch> declaredSwitches = new LinkedHashMap<>();
    
    private List<ActionVisibilitySwitch> actionSwitchesCache;
    private List<Action<?>> enabledActionsCache;
    
    private boolean isInitialized = false;
    void init() {
        for (ActionVisibilitySwitch actionSwitch : declaredSwitches.values()) {
            actionSwitch.init();
        }
        updateCache();
        isInitialized = true;
    }
    
    void updateCache() {
        this.enabledActionsCache = declaredSwitches.values().stream()
                .filter(ActionVisibilitySwitch::isValid)
                .filter(ActionVisibilitySwitch::isEnabled)
                .map(ActionVisibilitySwitch::getAction)
                .collect(Collectors.toList());
        this.actionSwitchesCache = declaredSwitches.values().stream()
                .filter(ActionVisibilitySwitch::isValid)
                .collect(Collectors.toList());
    }
    
    void addActionPreInit(ActionVisibilitySwitch action) {
        if (isInitialized) {
            throw new IllegalStateException();
        }
        declaredSwitches.put(action.getActionId(), action);
    }
    
    void fromJson(JsonObject json) {
        JsonObject actionsJson = json.get("actions").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : actionsJson.entrySet()) {
            ResourceLocation actionId = new ResourceLocation(entry.getKey());
            boolean isEnabled = entry.getValue().getAsJsonPrimitive().getAsBoolean();
            ActionVisibilitySwitch actionSwitch = new ActionVisibilitySwitch(this, actionId, isEnabled);
            declaredSwitches.put(actionId, actionSwitch);
        }
    }
    
    JsonElement toJson() {
        JsonObject json = new JsonObject();
        
        JsonObject actionsJson = new JsonObject();
        json.add("actions", actionsJson);
        for (ActionVisibilitySwitch actionSwitch : declaredSwitches.values()) {
            actionsJson.addProperty(actionSwitch.getActionId().toString(), actionSwitch.isEnabled());
        }
        return json;
    }
    
    public List<Action<?>> getEnabledView() {
        return enabledActionsCache;
    }
    
    public List<ActionVisibilitySwitch> getActionSwitchesView() {
        return actionSwitchesCache;
    }
    
    @Nullable
    public Action<?> getBaseActionInSlot(int index) {
        List<Action<?>> actions = getEnabledView();
        if (index < 0 || index >= actions.size()) {
            return null;
        }
        return actions.get(index);
    }

}
