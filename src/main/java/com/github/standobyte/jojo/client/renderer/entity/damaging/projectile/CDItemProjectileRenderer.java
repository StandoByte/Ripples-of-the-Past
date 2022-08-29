package com.github.standobyte.jojo.client.renderer.entity.damaging.projectile;

import com.github.standobyte.jojo.client.model.entity.projectile.CDItemProjectileModel;
import com.github.standobyte.jojo.client.renderer.entity.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.CDItemProjectileEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

// FIXME ! (item projectile) render a blood drop if it's homing
public class CDItemProjectileRenderer extends SimpleEntityRenderer<CDItemProjectileEntity, CDItemProjectileModel> {

    public CDItemProjectileRenderer(EntityRendererManager renderManager) {
        super(renderManager, new CDItemProjectileModel(), null);
    }
    
    @Override
    public ResourceLocation getTextureLocation(CDItemProjectileEntity entity) {
        return entity.getBlockTex();
    }

}
