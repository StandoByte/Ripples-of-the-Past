package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.PillarmanVeinModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanVeinEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class PillarmanVeinRenderer extends ExtendingEntityRenderer<PillarmanVeinEntity, PillarmanVeinModel> {

    public PillarmanVeinRenderer(EntityRendererManager renderManager) {
        super(renderManager, new PillarmanVeinModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/pillarman_veins.png"));
    }

}
