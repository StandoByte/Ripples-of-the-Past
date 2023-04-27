package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.SatiporojaScarfModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SatiporojaScarfEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SatiporojaScarfRenderer extends ExtendingEntityRenderer<SatiporojaScarfEntity, SatiporojaScarfModel> {

    public SatiporojaScarfRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SatiporojaScarfModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/satiporoja_scarf.png"));
    }
}
