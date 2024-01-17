package com.github.standobyte.jojo.client.controls;

import java.util.Objects;

import com.github.standobyte.jojo.power.IPowerType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class PowerTypeControlsEntry {
    private ActionsControlScheme currentControlScheme;
    
    PowerTypeControlsEntry(ActionsControlScheme controlScheme) {
        setCurrentCtrlScheme(controlScheme);
    }
    
    void setCurrentCtrlScheme(ActionsControlScheme controlScheme) {
        Objects.requireNonNull(controlScheme);
        this.currentControlScheme = controlScheme;
    }
    
    public ActionsControlScheme getCurrentCtrlScheme() {
        return currentControlScheme;
    }
    
    public void clearInvalidKeybinds() {
        currentControlScheme.clearInvalidKeybinds();
    }
    
    
    
    public JsonObject toJson(Gson gson) {
        JsonObject json = new JsonObject();
        json.add("currentControlScheme", currentControlScheme.toJson(gson));
        return json;
    }
    
    public static PowerTypeControlsEntry fromJson(IPowerType<?, ?> powerType, JsonObject json, Gson gson) {
        ActionsControlScheme controlScheme = ActionsControlScheme.fromJson(
                powerType, json.get("currentControlScheme").getAsJsonObject(), gson);
        return new PowerTypeControlsEntry(controlScheme);
    }
}
