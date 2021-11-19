package com.github.standobyte.jojo.client.renderer.entity.itemprojectile;

import com.github.standobyte.jojo.JojoMod;
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
    public static final ResourceLocation RES_KNIFE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/knife.png");

    public KnifeRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getTextureLocation(KnifeEntity entity) {
        return RES_KNIFE;
    }

    @Override
    public void render(KnifeEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTick, entity.yRotO, entity.yRot) - 90.0F));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTick, entity.xRotO, entity.xRot)));
        float tex1x1 = 0.0F;
        float tex1x2 = 0.5F;
        float tex1x2h = 0.125F;
        float tex1y1 = 0.0F;
        float tex1y2 = 0.15625F;
        float tex2x1 = 0.0F;
        float tex2x2 = 0.15625F;
        float tex2y1 = 0.15625F;
        float tex2y2 = 0.3125F;
        float scale = 0.0375F;
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(-4.0D, 0.0D, 0.0D);
        IVertexBuilder ivertexbuilder = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        MatrixStack.Entry matrixstack$entry = matrixStack.last();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, -2, tex2x1, tex2y1, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, 2, tex2x2, tex2y1, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, 2, tex2x2, tex2y2, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, -2, tex2x1, tex2y2, -1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, -2, tex2x1, tex2y1, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, 2, 2, tex2x2, tex2y1, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, 2, tex2x2, tex2y2, 1, 0, 0, packedLight);
        vertex(matrix4f, matrix3f, ivertexbuilder, -4, -2, -2, tex2x1, tex2y2, 1, 0, 0, packedLight);
        for(int j = 0; j < 4; ++j) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            vertex(matrix4f, matrix3f, ivertexbuilder, -8, -2, 0, tex1x1, tex1y1, 0, 1, 0, packedLight);
            vertex(matrix4f, matrix3f, ivertexbuilder, j % 2 == 1 ? 8 : -4, -2, 0, j % 2 == 1 ? tex1x2 : tex1x2h, tex1y1, 0, 1, 0, packedLight);
            vertex(matrix4f, matrix3f, ivertexbuilder, j % 2 == 1 ? 8 : -4, 2, 0, j % 2 == 1 ? tex1x2 : tex1x2h, tex1y2, 0, 1, 0, packedLight);
            vertex(matrix4f, matrix3f, ivertexbuilder, -8, 2, 0, tex1x1, tex1y2, 0, 1, 0, packedLight);
        }
        matrixStack.popPose();
    }
 }