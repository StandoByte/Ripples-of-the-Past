package com.github.standobyte.jojo.client.resources;

import java.util.stream.Stream;

import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;

import net.minecraft.client.renderer.texture.SpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class HamonSkillSpriteUploader extends SpriteUploader {
    public HamonSkillSpriteUploader(TextureManager textureManager) {
        super(textureManager, new ResourceLocation("textures/atlas/hamon_skills.png"), "hamon");
    }

    @Override
    protected Stream<ResourceLocation> getResourcesToLoad() {
        return HamonSkill.iconTextureMap.values().stream();
    }

    public TextureAtlasSprite getSprite(HamonSkill skill) {
        return this.getSprite(HamonSkill.iconTextureMap.get(skill));
    }
}
