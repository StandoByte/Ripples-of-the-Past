package com.github.standobyte.jojo.client.ui.sprites;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;

public class SpriteUploaders {
    private static ActionSpriteUploader actionSprites;
    private static HamonSkillSpriteUploader hamonSkillSprites;

    public static void initSpriteUploaders(Minecraft mc) {
        actionSprites = new ActionSpriteUploader(mc.textureManager);
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(actionSprites);
        hamonSkillSprites = new HamonSkillSpriteUploader(mc.textureManager);
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(hamonSkillSprites);
    }

    public static ActionSpriteUploader getActionSprites() {
        return actionSprites;
    }

    public static HamonSkillSpriteUploader getHamonSkillSprites() {
        return hamonSkillSprites;
    }

}
