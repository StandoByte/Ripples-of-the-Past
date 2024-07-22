package com.github.standobyte.jojo.client.controls;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.util.ResourceLocation;

public class HudControlSettings {
    private final File saveDir;
    private final JsonParser jsonParser = new JsonParser();
    
    private Map<ResourceLocation, PowerTypeControlSchemes> fullStateMap = new HashMap<>();
    private PowerTypeControlSchemes standControlsCache;
    private PowerTypeControlSchemes nonStandControlsCache;
    
    public ControlScheme getControlScheme(IPower<?, ?> power) {
        if (!power.hasPower()) {
            throw new IllegalStateException();
        }
        return getControlScheme(power.getPowerClassification());
    }
    
    public ControlScheme getControlScheme(PowerClassification power) {
        PowerTypeControlSchemes controls = getCachedControls(power);
        return controls != null ? controls.getCurrentCtrlScheme() : ControlScheme.EMPTY;
    }
    
    public PowerTypeControlSchemes getCachedControls(PowerClassification power) {
        switch (power) {
        case STAND: return standControlsCache;
        case NON_STAND: return nonStandControlsCache;
        default: throw new IllegalArgumentException();
        }
    }
    
    public void refreshControls(IPower<?, ?> power) {
        PowerTypeControlSchemes controls;
        if (!power.hasPower()) {
            controls = null;
        }
        else {
            IPowerType<?, ?> powerType = power.getType();
            ResourceLocation powerTypeId = powerType.getRegistryName();
            controls = fullStateMap.computeIfAbsent(powerTypeId, id -> {
                ControlScheme.DefaultControls defaultControls = powerType.clCreateDefaultLayout();
                return new PowerTypeControlSchemes(id, ControlScheme.createNewFromDefault(defaultControls));
            });
            controls.update(power);
        }
        
        switch (power.getPowerClassification()) {
        case STAND:
            this.standControlsCache = controls;
            break;
        case NON_STAND:
            this.nonStandControlsCache = controls;
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
    
    
    
    private final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
    public void saveForPowerType(ResourceLocation id) {
        save(id, fullStateMap.get(id));
    }
    
    public void saveAll() {
        fullStateMap.forEach(this::save);
    }
    
    private void save(ResourceLocation entryKey, PowerTypeControlSchemes entryValue) {
        File powerTypeDir = new File(saveDir + "/" + entryKey.getNamespace(), entryKey.getPath());
        File ctrlSchemeFile = new File(powerTypeDir, "current.json");
        try (BufferedWriter writer = GeneralUtil.newWriterMkDir(ctrlSchemeFile, Charsets.UTF_8)) {
            JsonElement json = entryValue.currentControlScheme.toJson();
            if (json.isJsonObject()) {
                gson.toJson(json, writer);
            }
        }
        catch (Exception exception) {
            JojoMod.getLogger().error("Failed to save mod control settings to {}", ctrlSchemeFile, exception);
        }
    }
    
    void load() {
        File[] subDirs = saveDir.listFiles(File::isDirectory);
        if (subDirs == null) return;
        
        for (File namespaceDir : subDirs) {
            // TODO remove beta legacy controls loading
            if (namespaceDir.getName().contains(",")) {
                ResourceLocation powerId = new ResourceLocation(namespaceDir.getName().replace(",", ":"));
                loadControls(namespaceDir, powerId);
                try {
                    saveForPowerType(powerId);
                    FileUtils.deleteDirectory(namespaceDir);
                } catch (IOException e) {
                    JojoMod.getLogger().error("Failed to delete legacy controls folder");
                    e.printStackTrace();
                }
            }
            //
            else {
                File[] powerTypePathDirs = namespaceDir.listFiles(File::isDirectory);
                if (powerTypePathDirs != null) {
                    for (File pathDir : powerTypePathDirs) {
                        ResourceLocation powerId = new ResourceLocation(namespaceDir.getName(), pathDir.getName());
                        loadControls(pathDir, powerId);
                    }
                }
            }
        }
    }
    
    private boolean loadControls(File directory, ResourceLocation powerId) {
        File ctrlSchemeFile = new File(directory, "current.json");
        if (ctrlSchemeFile.exists()) {
            try (BufferedReader reader = Files.newReader(ctrlSchemeFile, Charsets.UTF_8)) {
                JsonElement jsonRead = jsonParser.parse(reader);
                ControlScheme currentCtrlScheme = ControlScheme.fromJson(jsonRead, powerId);
                PowerTypeControlSchemes savedState = new PowerTypeControlSchemes(powerId, currentCtrlScheme);
                fullStateMap.put(powerId, savedState);
                return true;
            }
            catch (Exception exception) {
                JojoMod.getLogger().error("Failed to load mod control settings from {}", ctrlSchemeFile, exception);
            }
        }
        
        return false;
    }
}
