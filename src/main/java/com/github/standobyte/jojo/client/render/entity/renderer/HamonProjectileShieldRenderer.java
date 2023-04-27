package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.HamonProjectileShieldModel;
import com.github.standobyte.jojo.entity.HamonProjectileShieldEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HamonProjectileShieldRenderer extends SimpleEntityRenderer<HamonProjectileShieldEntity, HamonProjectileShieldModel> {

    public HamonProjectileShieldRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HamonProjectileShieldModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectile_shield.png"));
    }
}
