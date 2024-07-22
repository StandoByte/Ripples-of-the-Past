package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.PillarmanHornModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanHornEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class PillarmanHornRenderer extends ExtendingEntityRenderer<PillarmanHornEntity, PillarmanHornModel> {

    public PillarmanHornRenderer(EntityRendererManager renderManager) {
        super(renderManager, new PillarmanHornModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/pm_horn.png"));
    }

}
