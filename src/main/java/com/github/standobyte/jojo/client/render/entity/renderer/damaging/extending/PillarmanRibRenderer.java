package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.PillarmanRibModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanRibEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class PillarmanRibRenderer extends ExtendingEntityRenderer<PillarmanRibEntity, PillarmanRibModel> {

    public PillarmanRibRenderer(EntityRendererManager renderManager) {
        super(renderManager, new PillarmanRibModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/pillarman_ribs.png"));
    }

}
