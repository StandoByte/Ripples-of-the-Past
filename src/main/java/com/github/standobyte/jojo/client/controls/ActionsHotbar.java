package com.github.standobyte.jojo.client.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.power.IPower;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

public class ActionsHotbar {
    final List<ActionVisibilitySwitch> serializedSwitches = new ArrayList<>();
    final List<ActionVisibilitySwitch> legalSwitches = new ArrayList<>();
    private final List<Action<?>> enabledActionsCache = new ArrayList<>();
    
    private final List<ActionVisibilitySwitch> switchesView = Collections.unmodifiableList(legalSwitches);
    private final List<Action<?>> enabledActionsView = Collections.unmodifiableList(enabledActionsCache);
    
    private int selectedSlot = 0;
    
    private boolean isInitialized = false;
    void init() {
        for (ActionVisibilitySwitch actionSwitch : serializedSwitches) {
            actionSwitch.init();
        }
        updateCache();
        isInitialized = true;
    }
    
    void updateCache() {
        enabledActionsCache.clear();
        legalSwitches.stream()
                .filter(ActionVisibilitySwitch::isEnabled)
                .map(ActionVisibilitySwitch::getAction)
                .forEach(enabledActionsCache::add);
    }
    
    ActionVisibilitySwitch addActionAsSerialized(Action<?> action) {
        ActionVisibilitySwitch actionSwitch = new ActionVisibilitySwitch(this, action, action.enabledInHudDefault());
        serializedSwitches.add(actionSwitch);
        return actionSwitch;
    }
    
    public List<Action<?>> getEnabledActions() {
        return enabledActionsView;
    }
    
    public List<ActionVisibilitySwitch> getLegalActionSwitches() {
        return switchesView;
    }
    
    @Nullable
    public Action<?> getBaseActionInSlot(int index) {
        List<Action<?>> actions = getEnabledActions();
        if (index < 0 || index >= actions.size()) {
            return null;
        }
        return actions.get(index);
    }

    public void moveTo(ActionVisibilitySwitch action, int index) {
        int curValidIndex = switchesView.indexOf(action);
        if (curValidIndex > -1 && curValidIndex != index && legalSwitches.remove(action)) {
            serializedSwitches.remove(action);
            addTo(action, index);
        }
    }

    public void remove(ActionVisibilitySwitch action) {
        if (legalSwitches.remove(action)) {
            serializedSwitches.remove(action);
            updateCache();
        }
    }

    public void addTo(ActionVisibilitySwitch action, int index) {
        legalSwitches.add(index, action);
        
        int serializeAtIndex;
        if (index > 0) {
            Object prevElement = legalSwitches.get(index - 1);
            int prevIndex = serializedSwitches.indexOf(prevElement);
            if (prevIndex < 0) { // though this should not happen
                serializeAtIndex = serializedSwitches.size();
            }
            else {
                serializeAtIndex = prevIndex + 1;
            }
        }
        else {
            serializeAtIndex = 0;
        }
        serializedSwitches.add(serializeAtIndex, action);
        
        updateCache();
    }
    
    
    public int getSelectedSlot() {
        return selectedSlot;
    }
    
    public <P extends IPower<P, ?>> void setSelectedSlot(int selectedSlot, P power, ActionTarget target) {
        if (selectedSlot > -1) {
            List<Action<?>> actions = getEnabledActions();
            if (selectedSlot >= actions.size() || ((Action<P>) actions.get(selectedSlot)).getVisibleAction(power, target) == null) {
                selectedSlot = -1;
            }
        }
        else {
            selectedSlot = -1;
        }
        
        this.selectedSlot = selectedSlot;
    }
    
    public <P extends IPower<P, ?>> Action<P> getSelectedAction(P power, boolean shiftVariation, ActionTarget target) {
        int slot = getSelectedSlot();
        if (slot == -1) {
            return null;
        }
        
        Action<P> action = (Action<P>) getBaseActionInSlot(slot);
        action = ActionsOverlayGui.resolveVisibleActionInSlot(
                action, shiftVariation, power, ActionsOverlayGui.getInstance().getMouseTarget());
        if (action == null) {
            setSelectedSlot(-1, power, target);
        }
        return action;
    }
    
    
    void fromJson(JsonObject json) {
        JsonArray actionsJson = json.get("actions").getAsJsonArray();
        for (JsonElement element : actionsJson) {
            JsonObject entry = element.getAsJsonObject();
            ResourceLocation actionId = new ResourceLocation(entry.get("action").getAsString());
            boolean isEnabled = entry.get("enabled").getAsJsonPrimitive().getAsBoolean();
            ActionVisibilitySwitch actionSwitch = new ActionVisibilitySwitch(this, actionId, isEnabled);
            serializedSwitches.add(actionSwitch);
        }
    }
    
    JsonElement toJson() {
        JsonObject json = new JsonObject();
        
        JsonArray actionsJson = new JsonArray();
        json.add("actions", actionsJson);
        for (ActionVisibilitySwitch actionSwitch : serializedSwitches) {
            JsonObject entry = new JsonObject();
            entry.addProperty("action", actionSwitch.getActionId().toString());
            entry.addProperty("enabled", actionSwitch.isEnabled());
            actionsJson.add(entry);
        }
        return json;
    }
}
