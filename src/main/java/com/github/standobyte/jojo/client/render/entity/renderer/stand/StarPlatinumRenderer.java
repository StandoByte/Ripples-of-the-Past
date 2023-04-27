package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.StarPlatinumModel;
import com.github.standobyte.jojo.entity.stand.stands.StarPlatinumEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class StarPlatinumRenderer extends AbstractStandRenderer<StarPlatinumEntity, StarPlatinumModel> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/star_platinum.png");

    public StarPlatinumRenderer(EntityRendererManager renderManager) {
        super(renderManager, new StarPlatinumModel(), 0);
    }

    @Override
    public ResourceLocation getTextureLocation(StarPlatinumEntity entity) {
        return TEXTURE;
    }
}
