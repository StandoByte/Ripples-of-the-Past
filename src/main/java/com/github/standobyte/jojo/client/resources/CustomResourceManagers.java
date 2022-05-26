package com.github.standobyte.jojo.client.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;

public class CustomResourceManagers {
    private static ActionSpriteUploader actionSprites;
    private static HamonSkillSpriteUploader hamonSkillSprites;
    private static ResolveShadersListManager resolveShadersListManager;

    public static void initCustomResourceManagers(Minecraft mc) {
        actionSprites = new ActionSpriteUploader(mc.textureManager);
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(actionSprites);
        hamonSkillSprites = new HamonSkillSpriteUploader(mc.textureManager);
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(hamonSkillSprites);
        
        resolveShadersListManager = new ResolveShadersListManager();
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(resolveShadersListManager);
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

}
