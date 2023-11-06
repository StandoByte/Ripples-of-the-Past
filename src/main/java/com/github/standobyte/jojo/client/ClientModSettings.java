package com.github.standobyte.jojo.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;

public class ClientModSettings {
    
    private static class Settings {
        private boolean lockedAttacksHotbar;
        private boolean lockedAbilitiesHotbar;
    }
    
    
    public void switchLockedHotbarControls(ActionType hotbar) {
        setLockedHotbarControls(hotbar, !areControlsLockedForHotbar(hotbar));
    }
    
    private void setLockedHotbarControls(ActionType hotbar, boolean value) {
        switch (hotbar) {
        case ATTACK:
            settings.lockedAttacksHotbar = value;
            if (value) settings.lockedAbilitiesHotbar = false;
            break;
        case ABILITY:
            if (value) settings.lockedAttacksHotbar = false;
            settings.lockedAbilitiesHotbar = value;
            break;
        }
        save();
    }
    
    public boolean areControlsLockedForHotbar(ActionType hotbar) {
        if (hotbar == null) return false;
        switch (hotbar) {
        case ATTACK:
            return settings.lockedAttacksHotbar;
        case ABILITY:
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
