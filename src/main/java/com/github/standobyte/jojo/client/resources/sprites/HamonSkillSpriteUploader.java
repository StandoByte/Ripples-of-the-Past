package com.github.standobyte.jojo.client.resources.sprites;

import java.util.stream.Stream;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;

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
        return JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues().stream().map(AbstractHamonSkill::getRegistryName);
    }

    public TextureAtlasSprite getSprite(AbstractHamonSkill skill) {
        return this.getSprite(skill.getRegistryName());
    }
}
