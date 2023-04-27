package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.TommyGunBulletModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.TommyGunBulletEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TommyGunBulletRenderer extends SimpleEntityRenderer<TommyGunBulletEntity, TommyGunBulletModel> {

    public TommyGunBulletRenderer(EntityRendererManager renderManager) {
        super(renderManager, new TommyGunBulletModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/tommy_gun_bullet.png"));
    }

    // FIXME bullets seem to fly to another direction on such high velocity
    @Override
    public void render(TommyGunBulletEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        
    }
}
