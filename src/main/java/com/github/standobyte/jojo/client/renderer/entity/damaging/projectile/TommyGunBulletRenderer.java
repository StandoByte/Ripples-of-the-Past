package com.github.standobyte.jojo.client.renderer.entity.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.projectile.TommyGunBulletModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.TommyGunBulletEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TommyGunBulletRenderer extends SimpleEntityRenderer<TommyGunBulletEntity, TommyGunBulletModel> {

    public TommyGunBulletRenderer(EntityRendererManager renderManager) {
        super(renderManager, new TommyGunBulletModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/tommy_gun_bullet.png"));
    }

}
