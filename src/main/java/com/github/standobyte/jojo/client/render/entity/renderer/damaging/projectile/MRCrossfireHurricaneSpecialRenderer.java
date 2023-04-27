package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.client.render.entity.model.projectile.MRCrossfireHurricaneModel;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.EntityRendererManager;

public class MRCrossfireHurricaneSpecialRenderer extends MRCrossfireHurricaneRenderer {

    public MRCrossfireHurricaneSpecialRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }
    
    @Override
    protected void renderModel(MRCrossfireHurricaneEntity entity, MRCrossfireHurricaneModel model, float partialTick, MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight) {
        matrixStack.pushPose();
        matrixStack.scale(0.5F, 0.5F, 0.5F);
        super.renderModel(entity, model, partialTick, matrixStack, vertexBuilder, packedLight);
        matrixStack.popPose();
    }

}
