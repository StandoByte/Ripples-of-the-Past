package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.entity.HamonBlockChargeEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HamonBlockChargeRenderer extends EntityRenderer<HamonBlockChargeEntity> {

    public HamonBlockChargeRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(HamonBlockChargeEntity entity) {
        return null;
    }

    @Override
    public void render(HamonBlockChargeEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {}

}
