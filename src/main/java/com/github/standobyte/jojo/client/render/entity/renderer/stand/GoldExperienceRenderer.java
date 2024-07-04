package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.GoldExperienceModel;
import com.github.standobyte.jojo.entity.stand.stands.GoldExperienceEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class GoldExperienceRenderer extends StandEntityRenderer<GoldExperienceEntity, GoldExperienceModel> {

    public GoldExperienceRenderer(EntityRendererManager renderManager) {
        super(renderManager, new GoldExperienceModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/gold_experience.png"), 0);
    }
}
