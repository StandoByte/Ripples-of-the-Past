package com.github.standobyte.jojo.client.renderer.entity.itemprojectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.projectile.BladeHatEntityModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class BladeHatRenderer extends SimpleEntityRenderer<BladeHatEntity, BladeHatEntityModel> {

    public BladeHatRenderer(EntityRendererManager renderManager) {
        super(renderManager, new BladeHatEntityModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/blade_hat.png"));
    }

}
