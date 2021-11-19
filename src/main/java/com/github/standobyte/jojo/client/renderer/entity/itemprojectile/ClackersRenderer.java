package com.github.standobyte.jojo.client.renderer.entity.itemprojectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.projectile.ClackersModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.itemprojectile.ClackersEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class ClackersRenderer extends SimpleEntityRenderer<ClackersEntity, ClackersModel> {

    public ClackersRenderer(EntityRendererManager renderManager) {
        super(renderManager, new ClackersModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/clackers.png"));
    }

}
