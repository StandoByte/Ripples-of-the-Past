package com.github.standobyte.jojo.client.renderer.entity.damaging.extending;

import com.github.standobyte.jojo.client.model.entity.ownerbound.repeating.MRRedBindModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;

public class MRRedBindRenderer extends ExtendingEntityRenderer<MRRedBindEntity, MRRedBindModel> {

    public MRRedBindRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MRRedBindModel(), null);
    }
    
    @Override
    protected void doRender(MRRedBindEntity entity, MRRedBindModel model, 
            float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(Atlases.cutoutBlockSheet()), packedLight);
    }

}
