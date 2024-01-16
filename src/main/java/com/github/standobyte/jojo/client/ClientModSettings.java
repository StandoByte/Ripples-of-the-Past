package com.github.standobyte.jojo.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.function.Consumer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.HudNamesRender;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.PositionConfig;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.Minecraft;

public class ClientModSettings {
    
    public static class Settings {
        public float standStatsTranslucency = 0.75F;
        
        public PositionConfig barsPosition = PositionConfig.TOP_LEFT;
        public PositionConfig hotbarsPosition = PositionConfig.TOP_LEFT;
        public HudNamesRender hudNamesRender = HudNamesRender.ALWAYS;
        
        public boolean resolveShaders = true;
        public boolean menacingParticles = true;
        public boolean timeStopFreezesVisuals = false;
        public boolean timeStopAnimation = true;
        
        public boolean characterVoiceLines = true;
        
        public boolean toggleLmbHotbar = false;
        public boolean toggleRmbHotbar = false;
        public boolean toggleDisableHotbars = false;
    }
    
    
    public void editSettings(Consumer<Settings> edit) {
        edit.accept(settings);
        save();
    }
    
    public static Settings getSettingsReadOnly() {
        return getInstance().settings;
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
