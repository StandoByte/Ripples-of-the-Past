package com.github.standobyte.jojo.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.function.Consumer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.power.layout.ActionsLayout;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;

public class ClientModSettings {
    
    public static class Settings {
        private boolean lockedAttacksHotbar;
        private boolean lockedAbilitiesHotbar;
        public float standStatsTranslucency = 0.75F;
    }
    
    
    public void editSettings(Consumer<Settings> edit) {
        edit.accept(settings);
        save();
    }
    
    public Settings getSettingsReadOnly() {
        return settings;
    }
    
    
    public void switchLockedHotbarControls(ActionsLayout.Hotbar hotbar) {
        setLockedHotbarControls(hotbar, !areControlsLockedForHotbar(hotbar));
    }
    
    private void setLockedHotbarControls(ActionsLayout.Hotbar hotbar, boolean value) {
        editSettings(settings -> {
            switch (hotbar) {
            case LEFT_CLICK:
                settings.lockedAttacksHotbar = value;
                if (value) settings.lockedAbilitiesHotbar = false;
                break;
            case RIGHT_CLICK:
                if (value) settings.lockedAttacksHotbar = false;
                settings.lockedAbilitiesHotbar = value;
                break;
            }
        });
    }
    
    public boolean areControlsLockedForHotbar(ActionsLayout.Hotbar hotbar) {
        if (hotbar == null) return false;
        switch (hotbar) {
        case LEFT_CLICK:
            return settings.lockedAttacksHotbar;
        case RIGHT_CLICK:
            return settings.lockedAbilitiesHotbar;
        }
        return false;
    }
    
    
    
    public void load() {
        if (!this.optionsFile.exists()) {
            return;
        }
        
        try (BufferedReader reader = Files.newReader(optionsFile, Charsets.UTF_8)) {
            Settings deserialized = gson.fromJson(reader, settings.getClass());
            this.settings = deserialized;
        }
        catch (Exception exception) {
            JojoMod.getLogger().error("Failed to load mod client settings", (Throwable) exception);
        }
    }
    
    public void save() {
        try (BufferedWriter writer = Files.newWriter(optionsFile, Charsets.UTF_8)) {
            gson.toJson(settings, writer);
        }
        catch (Exception exception) {
            JojoMod.getLogger().error("Failed to save mod client settings", (Throwable) exception);
        }
    }
    
    
    
    private static ClientModSettings instance;
    private final Minecraft mc;
    private final File optionsFile;
    private final Gson gson;
    private Settings settings = new Settings();
    
    public static void init(Minecraft mc, File optionsFile) {
        if (instance == null) {
            instance = new ClientModSettings(mc, optionsFile);
        }
    }
    
    private ClientModSettings(Minecraft mc, File optionsFile) {
        this.mc = mc;
        this.optionsFile = optionsFile;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }
    
    public static ClientModSettings getInstance() {
        return instance;
    }
}
