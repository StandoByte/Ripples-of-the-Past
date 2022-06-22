package com.github.standobyte.jojo.client.resources;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

public class CustomResources {
    private static ActionSpriteUploader actionSprites;
    private static HamonSkillSpriteUploader hamonSkillSprites;
    private static ResolveShadersListManager resolveShadersListManager;
    private static ModSplashes modSplashes;

    public static void initCustomResourceManagers(Minecraft mc) {
        actionSprites = new ActionSpriteUploader(mc.textureManager);
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(actionSprites);
        hamonSkillSprites = new HamonSkillSpriteUploader(mc.textureManager);
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(hamonSkillSprites);
        
        resolveShadersListManager = new ResolveShadersListManager();
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(resolveShadersListManager);
        
        modSplashes = new ModSplashes(mc.getUser(), new ResourceLocation(JojoMod.MOD_ID, "texts/splashes.txt"));
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(modSplashes);
    }

    public static ActionSpriteUploader getActionSprites() {
        return actionSprites;
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

}
