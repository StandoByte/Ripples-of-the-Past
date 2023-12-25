package com.github.standobyte.jojo.client.render.entity.renderer;

import java.util.function.Consumer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

public class SpriteObjectEntityRenderer extends EntityRenderer<ObjectEntity> {

    public SpriteObjectEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    private static final ResourceLocation TOOTH_PARTICLE = new ResourceLocation(JojoMod.MOD_ID, "textures/particle/tooth.png");
    @Override
    public ResourceLocation getTextureLocation(ObjectEntity pEntity) {
        return TOOTH_PARTICLE;
    }
    
    @Override
    public void render(ObjectEntity entity, float entityYaw, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();

        renderSprite(matrixStack, 
                stack -> {
//                    float height = entity.getBbHeight();
//                    matrixStack.translate(0, height / 2, 0);
                }, 
                stack -> {
                    float height = entity.getBbHeight();
                    float width = entity.getBbWidth();
                    float scale = width * 0.4F;
                    matrixStack.scale(scale, scale, scale);
                    matrixStack.translate(width * -2, height * -2, 0);
                }, 
                buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))), 
                packedLight, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();
        super.render(entity, entityYaw, partialTick, matrixStack, buffer, packedLight);
    }
    
    public void renderSprite(MatrixStack matrixStack, Consumer<MatrixStack> beforeRotate, Consumer<MatrixStack> afterRotate, 
            IVertexBuilder vertexBuilder, int packedLight, int packedOverlay) {
        beforeRotate.accept(matrixStack);
        matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
        afterRotate.accept(matrixStack);
        
        MatrixStack.Entry pose = matrixStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        
        Vector3f normalVec = new Vector3f(0, 0, -1);
        normalVec.transform(matrix3f);

        for (ModelRenderer.PositionTextureVertex vertex : VERTICES) {
            float vertexX = vertex.pos.x();
            float vertexY = vertex.pos.y();
            float vertexZ = vertex.pos.z();
            Vector4f vector4f = new Vector4f(vertexX, vertexY, vertexZ, 1.0F);
            vector4f.transform(matrix4f);

            vertexBuilder.vertex(
                    vector4f.x(), vector4f.y(), vector4f.z(), 
                    1, 1, 1, 1, 
                    vertex.u, vertex.v,
                    packedOverlay, 
                    packedLight, 
                    normalVec.x(), normalVec.y(), normalVec.z());
        }
    }
    
    private static final ModelRenderer.PositionTextureVertex[] VERTICES = new ModelRenderer.PositionTextureVertex[] {
            new ModelRenderer.PositionTextureVertex(0, 1, 0, 1, 0),
            new ModelRenderer.PositionTextureVertex(1, 1, 0, 0, 0),
            new ModelRenderer.PositionTextureVertex(1, 0, 0, 0, 1),
            new ModelRenderer.PositionTextureVertex(0, 0, 0, 1, 1)
    };
}
