package com.github.standobyte.jojo.client.input;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.OptionalInt;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.input.ActionsControlScheme.SavedControlSchemes;
import com.github.standobyte.jojo.power.layout.ActionHotbarLayout;
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
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.KeyModifier;

public class ControlSchemesJson {
    private final File saveFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Action.class, Action.JsonSerialization.INSTANCE)
            .registerTypeAdapter(ActionHotbarLayout.class, ActionHotbarLayout.JsonSerialization.INSTANCE)
            .registerTypeAdapter(KeyBinding.class, KeyBindingJson.SERIALIZATION)
            .registerTypeAdapter(ResourceLocation.class, MCUtil.ResLocJson.SERIALIZATION)
            // will list be mutable tho?
            .create();
    
    ControlSchemesJson(File saveFile) {
        this.saveFile = saveFile;
    }
    
    private final Type mainMapType = new TypeToken<Map<ResourceLocation, SavedControlSchemes>>() {}.getType();
    
    public void save(Map<ResourceLocation, SavedControlSchemes> savedControlSchemes) {
        if (saveFile == null) return;

        try (BufferedWriter writer = Files.newWriter(saveFile, Charsets.UTF_8)) {
            gson.toJson(savedControlSchemes, writer);
        }
        catch (Exception exception) {
            JojoMod.getLogger().error("Failed to save mod control settings", exception);
        }
    }
    
    public Map<ResourceLocation, SavedControlSchemes> load() {
        if (saveFile == null || !saveFile.exists()) {
            return null;
        }
        
        try (BufferedReader reader = Files.newReader(saveFile, Charsets.UTF_8)) {
            Map<ResourceLocation, SavedControlSchemes> load = gson.fromJson(reader, mainMapType);
            load.values().forEach(SavedControlSchemes::clearInvalidKeybinds);
            return load;
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
