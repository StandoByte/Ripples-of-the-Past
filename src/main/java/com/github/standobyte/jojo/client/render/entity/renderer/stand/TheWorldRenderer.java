package com.github.standobyte.jojo.client.render.entity.renderer.stand;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.model.stand.TheWorldModel;
import com.github.standobyte.jojo.entity.stand.stands.TheWorldEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TheWorldRenderer extends StandEntityRenderer<TheWorldEntity, StandEntityModel<TheWorldEntity>> {
    
    public TheWorldRenderer(EntityRendererManager renderManager) {
        super(renderManager, 
                StandModelRegistry.registerModel(new ResourceLocation(JojoMod.MOD_ID, "the_world"), TheWorldModel::new), 
                new ResourceLocation(JojoMod.MOD_ID, "textures/entity/stand/the_world.png"), 0);
    }
}
