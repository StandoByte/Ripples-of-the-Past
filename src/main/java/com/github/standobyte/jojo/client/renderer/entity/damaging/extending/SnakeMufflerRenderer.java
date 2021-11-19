package com.github.standobyte.jojo.client.renderer.entity.damaging.extending;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.ownerbound.repeating.SnakeMufflerModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SnakeMufflerEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SnakeMufflerRenderer extends ExtendingEntityRenderer<SnakeMufflerEntity, SnakeMufflerModel> {

    public SnakeMufflerRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SnakeMufflerModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/satiporoja_scarf.png"));
    }
}
