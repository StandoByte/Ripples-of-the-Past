package com.github.standobyte.jojo.client.controls;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

public class ActionsHotbar {
    private Map<ResourceLocation, ActionVisibilitySwitch> declaredSwitches = new LinkedHashMap<>();
    
    private List<ActionVisibilitySwitch> actionSwitchesCache;
    private List<ActionVisibilitySwitch> switchesView;
    private List<Action<?>> enabledActionsCache;
    private List<Action<?>> actionsView;
    
    private boolean isInitialized = false;
    void init() {
        for (ActionVisibilitySwitch actionSwitch : declaredSwitches.values()) {
            actionSwitch.init();
        }
        updateCache();
        isInitialized = true;
    }
    
    void updateCache() {
        this.actionSwitchesCache = declaredSwitches.values().stream()
                .filter(ActionVisibilitySwitch::isValid)
                .collect(Collectors.toList());
        this.enabledActionsCache = actionSwitchesCache.stream()
                .filter(ActionVisibilitySwitch::isEnabled)
                .map(ActionVisibilitySwitch::getAction)
                .collect(Collectors.toList());
        this.switchesView = Collections.unmodifiableList(actionSwitchesCache);
        this.actionsView = Collections.unmodifiableList(enabledActionsCache);
    }
    
    void addActionPreInit(ActionVisibilitySwitch action) {
        if (isInitialized) {
            throw new IllegalStateException();
        }
        declaredSwitches.put(action.getActionId(), action);
    }
    
    void fromJson(JsonObject json) {
        JsonArray actionsJson = json.get("actions").getAsJsonArray();
        for (JsonElement element : actionsJson) {
            JsonObject entry = element.getAsJsonObject();
            ResourceLocation actionId = new ResourceLocation(entry.get("action").getAsString());
            boolean isEnabled = entry.get("enabled").getAsJsonPrimitive().getAsBoolean();
            ActionVisibilitySwitch actionSwitch = new ActionVisibilitySwitch(this, actionId, isEnabled);
            declaredSwitches.put(actionId, actionSwitch);
        }
    }
    
    JsonElement toJson() {
        JsonObject json = new JsonObject();
        
        JsonArray actionsJson = new JsonArray();
        json.add("actions", actionsJson);
        for (ActionVisibilitySwitch actionSwitch : declaredSwitches.values()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("action", actionSwitch.getActionId().toString());
            entry.addProperty("enabled", actionSwitch.isEnabled());
            actionsJson.add(entry);
        }
        return json;
    }
    
    public List<Action<?>> getEnabledView() {
        return actionsView;
    }
    
    public List<ActionVisibilitySwitch> getActionSwitchesView() {
        return switchesView;
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
