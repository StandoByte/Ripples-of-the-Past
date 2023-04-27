package com.github.standobyte.jojo.client.render.entity.renderer.damaging.stretching;

import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.OwnerBoundProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public abstract class StretchingEntityRenderer<T extends OwnerBoundProjectileEntity, M extends EntityModel<T>> extends SimpleEntityRenderer<T, M> {

    public StretchingEntityRenderer(EntityRendererManager renderManager, M entityModel, ResourceLocation texture) {
        super(renderManager, entityModel, texture);
    }

    protected abstract float getModelLength();
    
    protected abstract float getModelRotationPointOffset();
    
    protected float getAlpha(T entity, float partialTick) {
        if (entity.standDamage() && entity.isBodyPart()) {
            LivingEntity owner = entity.getOwner();
            if (owner instanceof StandEntity) {
                return ((StandEntity) owner).getAlpha(partialTick);
            }
        }
        return 1.0F;
    }

    @Override
    protected void rotateModel(M model, T entity, float partialTick, float yRotation, float xRotation, MatrixStack matrixStack) {
        Vector3d originPos = entity.getOriginPoint(partialTick);
        Vector3d entityPos = new Vector3d(
                MathHelper.lerp((double) partialTick, entity.xo, entity.getX()), 
                MathHelper.lerp((double) partialTick, entity.yo, entity.getY()), 
                MathHelper.lerp((double) partialTick, entity.zo, entity.getZ()));
        Vector3d stretchVec = entityPos.subtract(originPos);
        yRotation = MathUtil.yRotDegFromVec(stretchVec);
        xRotation = MathUtil.xRotDegFromVec(stretchVec);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRotation));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(xRotation));
        float modelLength = getModelLength() / 16F;
        float modelOffset = getModelRotationPointOffset() / 16F;
        float stretchLength = (float) stretchVec.length();
        matrixStack.translate(0.0F, 0.0F, -modelOffset);
        matrixStack.scale(1.0F, 1.0F, (stretchLength + 2 * modelOffset) / modelLength);
        matrixStack.translate(0.0F, 0.0F, modelOffset);
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-xRotation));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-yRotation));
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
    }
    
    @Override
    protected void doRender(T entity, M model, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            packedLight = entityRenderDispatcher.getPackedLightCoords(entity.getOwner(), partialTick);
        }
        super.doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
    }
    
    @Override
    protected void renderModel(T entity, M model, float partialTick, MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight) {
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, getAlpha(entity, partialTick));
    }
}
