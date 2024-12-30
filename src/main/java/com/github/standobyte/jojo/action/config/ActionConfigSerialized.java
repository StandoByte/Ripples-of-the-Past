package com.github.standobyte.jojo.action.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    protected Map<String, Field> configFieldsCacheClient;
    
    public ActionConfigSerialized(A action) {
        this.action = action;
        this.defaultSettings = defaultsToJson();
    }
    
    public JsonObject defaultsToJson() {
        JsonObject json = new JsonObject();
        
        List<Field> configFields = new ArrayList<>();
        addConfigFieldsRecursive(action.getClass(), configFields);
        configFields.stream().distinct().forEach(field -> {
            String name = field.getName();
            try {
                Object fieldContents = field.get(action);
                JsonElement fieldJson = getGson().toJsonTree(fieldContents);
                json.add(name, fieldJson);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                JojoMod.getLogger().error("Failed to generate config for field {} of action {}", name, action.getRegistryName(), e);
            }
        });
        
        return json;
    }
    
    private void addConfigFieldsRecursive(Class<?> actionClass, List<Field> configFields) {
        if (actionClass == null) return;
        Field[] declaredFields = actionClass.getDeclaredFields();
        for (final Field field : declaredFields) {
            if (field.getAnnotation(ActionConfigField.class) != null && isFieldConfigurable(field.getName(), field)) {
                field.setAccessible(true);
                configFields.add(field);
            }
        }
        ActionConfig actionConfig = actionClass.getAnnotation(ActionConfig.class);
        if (actionConfig != null) {
            for (String fieldName : actionConfig.value()) {
                Field field = FieldUtils.getField(actionClass, fieldName, true);
                if (field != null && isFieldConfigurable(fieldName, field)) {
                    configFields.add(field);
                }
            }
        }
        addConfigFieldsRecursive(actionClass.getSuperclass(), configFields);
    }
    
    
    
    public void applyFromJson(JsonObject json) {
        if (configFieldsCacheClient == null) {
            configFieldsCacheClient = new HashMap<>();
        }
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String fieldName = entry.getKey();
            Field field = configFieldsCacheClient.computeIfAbsent(fieldName, name -> FieldUtils.getField(action.getClass(), name, true));
            if (field != null) {
                JsonElement jsonElement = entry.getValue();
                Object value = getGson().fromJson(jsonElement, field.getType());
                try {
                    field.set(action, value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    JojoMod.getLogger().error("Failed to apply config to field {} of action {}", fieldName, action.getRegistryName(), e);
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
            JojoMod.getLogger().error("Failed to read config for action {}", action.getRegistryName(), e);
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
