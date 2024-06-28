package com.github.standobyte.jojo.action.config;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;

public class ActionConfigSerialized<A extends Action<?>> {
    protected static final Gson GSON = new GsonBuilder().create();
    protected static final Gson NO_PP = new GsonBuilder().create();
    protected final A action;
    public final JsonObject defaultSettings;
    protected JsonObject appliedSettings;
    protected String settingsToSend = "{}";
    
    public ActionConfigSerialized(A action) {
        this.action = action;
        this.defaultSettings = defaultsToJson();
    }
    
    public JsonObject defaultsToJson() {
        JsonObject json = new JsonObject();

        Field[] annotated = FieldUtils.getFieldsWithAnnotation(action.getClass(), ActionConfigField.class);
        if (annotated.length > 0) {
            for (Field field : annotated) {
                String name = field.getName();
                if (isFieldConfigurable(name, field)) {
                    try {
                        field.setAccessible(true);
                        Object fieldContents = field.get(action);
                        JsonElement fieldJson = getGson().toJsonTree(fieldContents);
                        json.add(name, fieldJson);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        JojoMod.getLogger().error("Failed to generate config for field {} of action {}", name, action.getRegistryName());
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return json;
    }
    
    public void applyFromJson(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String fieldName = entry.getKey();
            Field field = FieldUtils.getField(action.getClass(), fieldName, true);
            if (field != null) {
                JsonElement jsonElement = entry.getValue();
                Object value = getGson().fromJson(jsonElement, field.getType());
                try {
                    field.set(action, value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    JojoMod.getLogger().error("Failed to apply config to field {} of action {}", fieldName, action.getRegistryName());
                    e.printStackTrace();
                }
            }
        }
        this.appliedSettings = json;
        this.settingsToSend = NO_PP.toJson(json);
    }
    

    public void toBuf(PacketBuffer buf) {
        buf.writeUtf(settingsToSend);
    }

    public void applyFromBuf(PacketBuffer buf) {
        String read = buf.readUtf();
        try {
            JsonObject json = JSONUtils.parse(read);
            applyFromJson(json);
        }
        catch (JsonParseException e) {
            JojoMod.getLogger().error("Failed to read config for action {}", action.getRegistryName());
            e.printStackTrace();
        }
    }
    
    
    public void restoreFromDefaults() {
        applyFromJson(defaultSettings);
    }
    
    
    protected Gson getGson() {
        return GSON;
    }
    
    /**
     * Can be overriden to prevent certain fields which have {@link ActionConfigField} 
     * annotation from being generated with "/jojoconfig ability_config" command.
     */
    protected boolean isFieldConfigurable(String name, Field field) {
        return true;
    }
}
