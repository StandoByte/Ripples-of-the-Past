package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.client.render.entity.model.projectile.MRCrossfireHurricaneModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;

public class MRCrossfireHurricaneRenderer extends SimpleEntityRenderer<MRCrossfireHurricaneEntity, MRCrossfireHurricaneModel> {

    public MRCrossfireHurricaneRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MRCrossfireHurricaneModel(), null);
    }
    
    @Override
    protected void doRender(MRCrossfireHurricaneEntity entity, MRCrossfireHurricaneModel model, 
            float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        float scale = entity.getScale();
        matrixStack.pushPose();
        matrixStack.scale(scale, scale, scale);
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(Atlases.cutoutBlockSheet()), packedLight);
        matrixStack.popPose();
    }

}
