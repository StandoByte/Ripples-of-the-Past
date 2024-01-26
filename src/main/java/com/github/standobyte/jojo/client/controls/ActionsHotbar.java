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
    Map<ResourceLocation, ActionVisibilitySwitch> actions = new LinkedHashMap<>();
    private List<Action<?>> enabledActionsCache;
    
    void updateCache() {
        this.enabledActionsCache = actions.values().stream()
                .filter(action -> action.isVisible)
                .map(actionSwitch -> actionSwitch.action)
                .collect(Collectors.toList());
    }
    
    public List<Action<?>> getEnabledView() {
        return enabledActionsCache;
    }
    
    @Nullable
    public Action<?> getBaseActionInSlot(int index) {
        List<Action<?>> actions = getEnabledView();
        if (index < 0 || index >= actions.size()) {
            return null;
        }
        return actions.get(index);
    }
    
    
    static class SaveState {
        Map<ResourceLocation, ActionVisibilitySwitch.SaveInfo> actionsOrder = new LinkedHashMap<>();
        
        void fromJson(JsonObject json) {
            JsonObject actionsJson = json.get("actions").getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : actionsJson.entrySet()) {
                ResourceLocation actionId = new ResourceLocation(entry.getKey());
                boolean isVisible = entry.getValue().getAsJsonPrimitive().getAsBoolean();
                ActionVisibilitySwitch.SaveInfo actionSwitch = new ActionVisibilitySwitch.SaveInfo(actionId, isVisible);
                actionsOrder.put(actionId, actionSwitch);
            }
        }
        
        JsonElement toJson() {
            JsonObject json = new JsonObject();
            
            JsonObject actionsJson = new JsonObject();
            json.add("actions", actionsJson);
            for (ActionVisibilitySwitch.SaveInfo actionSwitch : actionsOrder.values()) {
                json.addProperty(actionSwitch.action.toString(), actionSwitch.isVisible);
            }
            return json;
        }
    }

}
