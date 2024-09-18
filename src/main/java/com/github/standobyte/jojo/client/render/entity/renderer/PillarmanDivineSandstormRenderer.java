package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.entity.damaging.projectile.PillarmanDivineSandstormEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class PillarmanDivineSandstormRenderer extends EntityRenderer<PillarmanDivineSandstormEntity> {

    public PillarmanDivineSandstormRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(PillarmanDivineSandstormEntity p_110775_1_) {
        return null;
    }

    @Override
    public void render(PillarmanDivineSandstormEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {}
    
}
