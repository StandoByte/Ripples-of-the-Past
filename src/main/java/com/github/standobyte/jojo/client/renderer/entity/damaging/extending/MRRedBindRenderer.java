package com.github.standobyte.jojo.client.renderer.entity.damaging.extending;

import com.github.standobyte.jojo.client.model.entity.ownerbound.repeating.MRRedBindModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.math.vector.Vector3d;

public class MRRedBindRenderer extends ExtendingEntityRenderer<MRRedBindEntity, MRRedBindModel> {
    private boolean second = false;

    public MRRedBindRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MRRedBindModel(), null);
    }

    @Override
    public void render(MRRedBindEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        if (entity.isInKickCombo()) {
            second = !second;
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
            second = !second;
        }
    }
    
    @Override
    protected Vector3d getOriginPos(MRRedBindEntity entity, float partialTick) {
//        if (entity.isInKickCombo()) {
//            // FIXME (!!!!!!!!!!!!) (MR RB kick) origin pos at arms
//            return second ? super.getOriginPos(entity, partialTick).add(0, 0.5, 0) : super.getOriginPos(entity, partialTick).add(0, -0.5, 0);
//        }
        return super.getOriginPos(entity, partialTick);
    }
    
    @Override
    protected void doRender(MRRedBindEntity entity, MRRedBindModel model, 
            float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(Atlases.cutoutBlockSheet()), packedLight);
    }

}
