package com.github.standobyte.jojo.client.renderer.entity;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.CrimsonBubbleModel;
import com.github.standobyte.jojo.entity.CrimsonBubbleEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class CrimsonBubbleRenderer extends SimpleEntityRenderer<CrimsonBubbleEntity, CrimsonBubbleModel> {

    public CrimsonBubbleRenderer(EntityRendererManager renderManager) {
        super(renderManager, new CrimsonBubbleModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/crimson_bubble.png"));
    }
    
}
