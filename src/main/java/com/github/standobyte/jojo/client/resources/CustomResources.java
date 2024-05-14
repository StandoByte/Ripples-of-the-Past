package com.github.standobyte.jojo.client.resources;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.resources.models.GeckoAnimLoader;
import com.github.standobyte.jojo.client.resources.models.StandModelOverrides;
import com.github.standobyte.jojo.client.resources.sprites.HamonSkillSpriteUploader;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

public class CustomResources {
    private static HamonSkillSpriteUploader hamonSkillSprites;
    private static ResolveShadersListManager resolveShadersListManager;
    private static ModSplashes modSplashes;
    private static StandSkinsManager standSkinsLoader;
    private static StandModelOverrides standModelOverrides;
    private static GeckoAnimLoader standModelAnimations;

    public static void initCustomResourceManagers(Minecraft mc) {
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) mc.getResourceManager();
        
        resourceManager.registerReloadListener(hamonSkillSprites = new HamonSkillSpriteUploader(mc.textureManager));
        resourceManager.registerReloadListener(resolveShadersListManager = new ResolveShadersListManager());
        resourceManager.registerReloadListener(modSplashes = new ModSplashes(mc.getUser(), new ResourceLocation(JojoMod.MOD_ID, "texts/splashes.txt")));
        resourceManager.registerReloadListener(new ResourceReloadNotifier());
        resourceManager.registerReloadListener(standSkinsLoader = new StandSkinsManager());
        resourceManager.registerReloadListener(standModelOverrides = new StandModelOverrides(new Gson()));
        resourceManager.registerReloadListener(standModelAnimations = new GeckoAnimLoader(new Gson()));
    }
    
    public static HamonSkillSpriteUploader getHamonSkillSprites() {
        return hamonSkillSprites;
    }
    
    public static ResolveShadersListManager getResolveShadersListManager() {
        return resolveShadersListManager;
    }
    
    public static ModSplashes getModSplashes() {
        return modSplashes;
    }
    
    public static StandSkinsManager getStandSkinsLoader() {
        return standSkinsLoader;
    }
    
    public static StandModelOverrides getStandModelOverrides() {
        return standModelOverrides;
    }
    
    public static GeckoAnimLoader getStandModelAnimations() {
        return standModelAnimations;
    }

}
