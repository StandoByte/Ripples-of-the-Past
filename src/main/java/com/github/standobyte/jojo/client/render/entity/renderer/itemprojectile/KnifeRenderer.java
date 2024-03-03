package com.github.standobyte.jojo.client.render.entity.renderer.itemprojectile;

import com.github.standobyte.jojo.entity.itemprojectile.KnifeEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class KnifeRenderer extends ArrowRenderer<KnifeEntity> {

    public KnifeRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getTextureLocation(KnifeEntity entity) {
        return entity.getKnifeTexture();
    }

    @Override
    public void render(KnifeEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();
        float yRot = MathHelper.lerp(partialTick, entity.yRotO, entity.yRot) - 90.0F;
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
//        matrixStack.translate(0, 0.0625 * (1 - MathHelper.sin(yRot * MathUtil.DEG_TO_RAD)), 0);
        matrixStack.translate(0, 0.125, 0);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTick, entity.xRotO, entity.xRot)));
        float scale = 0.0375F;
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(1, 0, 0);
        IVertexBuilder ivertexbuilder = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        MatrixStack.Entry matrixstack$entry = matrixStack.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        
        
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, -2, 
                2f/32, 27f/32, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, 2, 
                5f/32, 27f/32, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, 2, 
                5f/32, 30f/32, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, -2, 
                2f/32, 30f/32, -1, 0, 0, packedLight);
        
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, -2, 
                2f/32, 27f/32, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, 2, 
                5f/32, 27f/32, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, 2, 
                5f/32, 30f/32, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, -2, 
                2f/32, 30f/32, 1, 0, 0, packedLight);
        
        
        
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, -3, 0, 
                0,      12f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, -3, 0, 
                16f/32, 12f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, 3, 0, 
                16f/32, 21f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, 3, 0, 
                0,      21f/32, 0, 1, 0, packedLight);
        
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, -3, 0, 
                0,      0f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, -3, 0, 
                16f/32, 0f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, 3, 0, 
                16f/32, 9f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, 3, 0, 
                0,      9f/32, 0, 1, 0, packedLight);
        
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, -3, 0, 
                0,      21f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, -3, 0, 
                16f/32, 21f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, 3, 0, 
                16f/32, 12f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, 3, 0, 
                0,      12f/32, 0, 1, 0, packedLight);
        
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, -3, 0, 
                0,      9f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, -3, 0, 
                16f/32, 9f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, 8, 3, 0, 
                16f/32, 0f/32, 0, 1, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -8, 3, 0, 
                0,      0f/32, 0, 1, 0, packedLight);
        
        matrixStack.popPose();
    }
 }