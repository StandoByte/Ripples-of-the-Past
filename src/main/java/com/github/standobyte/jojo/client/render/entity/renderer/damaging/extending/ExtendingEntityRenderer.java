package com.github.standobyte.jojo.client.render.entity.renderer.damaging.extending;

import com.github.standobyte.jojo.client.render.entity.model.ownerbound.repeating.RepeatingModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.OwnerBoundProjectileEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public abstract class ExtendingEntityRenderer<T extends OwnerBoundProjectileEntity, M extends RepeatingModel<T>> extends SimpleEntityRenderer<T, M> {

    public ExtendingEntityRenderer(EntityRendererManager renderManager, M model, ResourceLocation texPath) {
        super(renderManager, model, texPath);
    }
    
    protected float getAlpha(T entity, float partialTick) {
        if (entity.standDamage()) {
            LivingEntity owner = entity.getOwner();
            if (owner instanceof StandEntity) {
                return ((StandEntity) owner).getAlpha(partialTick);
            }
        }
        return 1.0F;
    }

    @Override
    protected void rotateModel(M model, T entity, float partialTick, float yRotation, float xRotation, MatrixStack matrixStack) {
        Vector3d originPos = getOriginPos(entity, partialTick);
        Vector3d entityPos = new Vector3d(
                MathHelper.lerp((double) partialTick, entity.xo, entity.getX()), 
                MathHelper.lerp((double) partialTick, entity.yo, entity.getY()), 
                MathHelper.lerp((double) partialTick, entity.zo, entity.getZ()));
        Vector3d extentVec = entityPos.subtract(originPos);
        yRotation = MathUtil.yRotDegFromVec(extentVec);
        xRotation = MathUtil.xRotDegFromVec(extentVec);
        model.setLength((float) extentVec.length());
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
    }
    
    protected Vector3d getOriginPos(T entity, float partialTick) {
        return entity.getOriginPoint(partialTick);
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
