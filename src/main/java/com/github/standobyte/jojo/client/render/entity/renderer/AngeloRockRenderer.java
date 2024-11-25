package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.client.render.entity.model.AngeloRockModel;
import com.github.standobyte.jojo.entity.AngeloRockEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class AngeloRockRenderer extends SimpleEntityRenderer<AngeloRockEntity, AngeloRockModel> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/block/stone.png");

    public AngeloRockRenderer(EntityRendererManager renderManager) {
        super(renderManager, new AngeloRockModel(), TEXTURE);
    }
    
//    @Override
//    public ResourceLocation getTextureLocation(AngeloRockEntity entity) {
//        
//    }
    
}
