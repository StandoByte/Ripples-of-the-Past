package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.render.entity.StarPlatinumModelConvertExample;
import com.github.standobyte.jojo.entity.stand.stands.StarPlatinumEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class StarPlatinumRenderer extends StandEntityRenderer<StarPlatinumEntity, StarPlatinumModelConvertExample> {
    
    public StarPlatinumRenderer(EntityRendererManager renderManager) {
        super(renderManager, BlockbenchStandModelHelper.organizeHumanoidModelParts(new StarPlatinumModelConvertExample()), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/star_platinum.png"), 0);
    }
}
