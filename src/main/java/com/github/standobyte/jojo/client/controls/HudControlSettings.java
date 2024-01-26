package com.github.standobyte.jojo.client.controls;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.util.ResourceLocation;

public class HudControlSettings {
    private final File saveDir;
    private final JsonParser jsonParser = new JsonParser();
    
    private Map<ResourceLocation, SavedPowerTypeControlsState> fullStateMap = new HashMap<>();
    private ControlScheme standControlsCache;
    private ControlScheme nonStandControlsCache;
    
    public ControlScheme getControlScheme(PowerClassification power) {
        switch (power) {
        case STAND: return standControlsCache;
        case NON_STAND: return nonStandControlsCache;
        default: throw new IllegalArgumentException();
        }
    }
    
    public void cacheControlsScheme(IPower<?, ?> power) {
        ControlScheme controlScheme;
        if (!power.hasPower()) {
            controlScheme = null;
        }
        else {
            IPowerType<?, ?> powerType = power.getType();
            ResourceLocation powerTypeId = powerType.getRegistryName();
            SavedPowerTypeControlsState saveState = fullStateMap.computeIfAbsent(powerTypeId, id -> {
                return new SavedPowerTypeControlsState(powerType.clCreateDefaultLayout());
            });
            controlScheme = saveState.createControlScheme(power);
        }
        
        switch (power.getPowerClassification()) {
        case STAND:
            this.standControlsCache = controlScheme;
            break;
        case NON_STAND:
            this.nonStandControlsCache = controlScheme;
            break;
        default:
            throw new IllegalStateException();
        }
    }
    
    
    
    private static HudControlSettings instance;
    
    public static void init(File saveDir) {
        if (instance == null) {
            instance = new HudControlSettings(saveDir);
            instance.load();
        }
    }
    
    HudControlSettings(File saveDir) {
        this.saveDir = saveDir;
    }
    
    public static HudControlSettings getInstance() {
        return instance;
    }
    
    
    
//    private final Gson gson = new GsonBuilder().setPrettyPrinting()
//            .registerTypeAdapter(Action.class, Action.JsonSerialization.INSTANCE)
//            .registerTypeAdapter(KeyBinding.class, KeyBindingJson.SERIALIZATION)
//            .registerTypeAdapter(ResourceLocation.class, MCUtil.ResLocJson.SERIALIZATION)
//            .create();
    
    void save() {
        InputHandler.toDoDeleteMe();
//        if (saveFile == null || !saveFile.exists()) {
//            return;
//        }
//        
//        try (BufferedWriter writer = Files.newWriter(saveFile, Charsets.UTF_8)) {
//            JsonObject json = new JsonObject();
//            
//            for (Map.Entry<ResourceLocation, PowerTypeControlsEntry> powerTypeEntry : savedControlSchemes.entrySet()) {
//                json.add(powerTypeEntry.getKey().toString(), powerTypeEntry.getValue().toJson(gson));
//            }
//            
//            missingTypesData.forEach((key, value) -> json.add(key, value));
//            
//            gson.toJson(json, writer);
//        }
//        catch (Exception exception) {
//            JojoMod.getLogger().error("Failed to save mod control settings to {}", saveFile, exception);
//        }
    }
    
    void load() {
        File[] subDirs = saveDir.listFiles(File::isDirectory);
        for (File powerTypeDir : subDirs) {
            ResourceLocation powerId = new ResourceLocation(powerTypeDir.getName());
            File ctrlSchemeFile = new File(powerTypeDir, "current.json");
            if (ctrlSchemeFile.exists()) {
                try (BufferedReader reader = Files.newReader(ctrlSchemeFile, Charsets.UTF_8)) {
                    JsonElement jsonRead = jsonParser.parse(reader);
                    ControlScheme.SaveState currentCtrlScheme = ControlScheme.SaveState.fromJson(jsonRead);
                    SavedPowerTypeControlsState savedState = new SavedPowerTypeControlsState(currentCtrlScheme);
                    fullStateMap.put(powerId, savedState);
                }
                catch (Exception exception) {
                    JojoMod.getLogger().error("Failed to load mod control settings from {}", ctrlSchemeFile, exception);
                }
            }
        }
    }
}
