package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.model.stand.StarPlatinumModel;
import com.github.standobyte.jojo.entity.stand.stands.StarPlatinumEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class StarPlatinumRenderer extends StandEntityRenderer<StarPlatinumEntity, StandEntityModel<StarPlatinumEntity>> {
    
    public StarPlatinumRenderer(EntityRendererManager renderManager) {
        super(renderManager, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "star_platinum"), StarPlatinumModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/star_platinum.png"), 0);
    }
}
