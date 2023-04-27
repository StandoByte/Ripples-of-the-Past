package com.github.standobyte.jojo.client.render.entity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public abstract class SimpleEntityRenderer<T extends Entity, M extends EntityModel<T>> extends EntityRenderer<T> {
    protected final M model;
    protected final ResourceLocation texPath;

    public SimpleEntityRenderer(EntityRendererManager renderManager, M model, ResourceLocation texPath) {
        super(renderManager);
        this.model = model;
        this.texPath = texPath;
    }
    
    protected M getEntityModel() {
        return model;
    }
    
    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texPath;
    }

    @Override
    public void render(T entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (shouldRender(entity)) {
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            M model = getEntityModel();
            float xRotation = MathHelper.lerp(partialTick, entity.xRotO, entity.xRot);
            rotateModel(model, entity, partialTick, yRotation, xRotation, matrixStack);
            doRender(entity, model, partialTick, matrixStack, buffer, packedLight);
            matrixStack.popPose();
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    protected boolean shouldRender(T entity) {
        return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
    }
    
    protected void rotateModel(M model, T entity, float partialTick, float yRotation, float xRotation, MatrixStack matrixStack) {
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
    }
    
    protected void doRender(T entity, M model, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(model.renderType(getTextureLocation(entity))), packedLight);
    }
    
    protected void renderModel(T entity, M model, float partialTick, MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight) {
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

}
