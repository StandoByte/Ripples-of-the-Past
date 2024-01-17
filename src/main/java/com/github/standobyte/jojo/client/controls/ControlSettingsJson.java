package com.github.standobyte.jojo.client.controls;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.KeyModifier;

public class ControlSettingsJson {
    private final File saveFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Action.class, Action.JsonSerialization.INSTANCE)
            .registerTypeAdapter(KeyBinding.class, KeyBindingJson.SERIALIZATION)
            .registerTypeAdapter(ResourceLocation.class, MCUtil.ResLocJson.SERIALIZATION)
            .create();
    private final JsonParser jsonParser = new JsonParser();
    private final Map<String, JsonElement> missingTypesData = new HashMap<>();
    
    ControlSettingsJson(File saveFile) {
        this.saveFile = saveFile;
    }
    
    public void save(Map<ResourceLocation, PowerTypeControlsEntry> savedControlSchemes) {
        if (saveFile == null) return;

        try (BufferedWriter writer = Files.newWriter(saveFile, Charsets.UTF_8)) {
            JsonObject json = new JsonObject();
            
            for (Map.Entry<ResourceLocation, PowerTypeControlsEntry> powerTypeEntry : savedControlSchemes.entrySet()) {
                json.add(powerTypeEntry.getKey().toString(), powerTypeEntry.getValue().toJson(gson));
            }
            
            missingTypesData.forEach((key, value) -> json.add(key, value));
            
            gson.toJson(json, writer);
        }
        catch (Exception exception) {
            JojoMod.getLogger().error("Failed to save mod control settings", exception);
        }
    }
    
    public Map<ResourceLocation, PowerTypeControlsEntry> load() {
        if (saveFile == null || !saveFile.exists()) {
            return null;
        }
        
        try (BufferedReader reader = Files.newReader(saveFile, Charsets.UTF_8)) {
            Map<ResourceLocation, PowerTypeControlsEntry> mainMap = new HashMap<>();
            missingTypesData.clear();
            
            JsonObject jsonRead = jsonParser.parse(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entryRead : jsonRead.entrySet()) {
                ResourceLocation powerId = new ResourceLocation(entryRead.getKey());
                
                IPowerType<?, ?> powerType = JojoCustomRegistries.STANDS.fromId(powerId);
                if (powerType == null) powerType = JojoCustomRegistries.NON_STAND_POWERS.fromId(powerId);
                if (powerType != null) {
                    PowerTypeControlsEntry ctrlSettings = PowerTypeControlsEntry.fromJson(powerType, entryRead.getValue().getAsJsonObject(), gson);
                    ctrlSettings.clearInvalidKeybinds();
                    mainMap.put(powerId, ctrlSettings);
                }
                else {
                    missingTypesData.put(entryRead.getKey(), entryRead.getValue());
                }
            }
            
            return mainMap;
        }
        catch (Exception exception) {
            JojoMod.getLogger().error("Failed to load mod control settings", (Throwable) exception);
        }
        
        return null;
    }
    
    
    
    public static class KeyBindingJson implements JsonSerializer<KeyBinding>, JsonDeserializer<KeyBinding> {
        public static final KeyBindingJson SERIALIZATION = new KeyBindingJson();
        
        protected KeyBindingJson() {}

        @Override
        public KeyBinding deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            KeyBinding keyBinding = ActionsControlScheme.createBlankKeyBinding();
            
            String str = json.getAsString();
            if (str.indexOf(':') != -1) {
                String[] pts = str.split(":");
                keyBinding.setKeyModifierAndCode(KeyModifier.valueFromString(pts[1]), InputMappings.getKey(pts[0]));
            } else {
                keyBinding.setKeyModifierAndCode(KeyModifier.NONE, InputMappings.getKey(str));
            }
            
            return keyBinding;
        }

        @Override
        public JsonElement serialize(KeyBinding src, Type typeOfSrc, JsonSerializationContext context) {
            String save = src.saveString() + (src.getKeyModifier() != KeyModifier.NONE ? ":" + src.getKeyModifier() : "");
            return new JsonPrimitive(save);
        }
    }

}
