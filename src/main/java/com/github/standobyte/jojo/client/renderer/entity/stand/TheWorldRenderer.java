package com.github.standobyte.jojo.client.renderer.entity.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.stand.TheWorldModel;
import com.github.standobyte.jojo.entity.stand.stands.TheWorldEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TheWorldRenderer extends AbstractStandRenderer<TheWorldEntity, TheWorldModel> {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/the_world.png");

    public TheWorldRenderer(EntityRendererManager renderManager) {
        super(renderManager, new TheWorldModel(), 0);
    }

    @Override
    public ResourceLocation getTextureLocation(TheWorldEntity entity) {
        return TEXTURE;
    }
}
