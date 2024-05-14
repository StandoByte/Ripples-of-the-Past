package com.github.standobyte.jojo.client.render.entity.renderer.damaging.beam;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SpaceRipperStingyEyesEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class SpaceRipperStingyEyesRenderer extends EntityRenderer<SpaceRipperStingyEyesEntity> {
    private static final ResourceLocation BEAM_TEX = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/space_ripper_stingy_eyes.png");

    public SpaceRipperStingyEyesRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(SpaceRipperStingyEyesEntity entity) {
        return BEAM_TEX;
    }

    @Override
    public void render(SpaceRipperStingyEyesEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();
        packedLight = ClientUtil.MAX_MODEL_LIGHT;
        Vector3d beamVec = entity.getOriginPoint(partialTick).subtract(entity.getPosition(partialTick));
        float yRot = MathUtil.yRotDegFromVec(beamVec);
        float xRot = MathUtil.xRotDegFromVec(beamVec);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F - yRot));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-xRot));
        float beamWidth = 0.15f;
        matrixStack.scale(1.0F, beamWidth, beamWidth);
        matrixStack.last().normal().setIdentity();
        IVertexBuilder ivertexbuilder = buffer.getBuffer(RenderType.entityTranslucentCull(getTextureLocation(entity)));
        float length = (float) beamVec.length();
        
        renderSide(matrixStack, new Vector3f(0, -1, 0), length, ivertexbuilder, packedLight);
        renderSide(matrixStack, new Vector3f(0, 0, -1), length, ivertexbuilder, packedLight);
        renderSide(matrixStack, new Vector3f(0, 1, 0), length, ivertexbuilder, packedLight);
        renderSide(matrixStack, new Vector3f(0, 0, 1), length, ivertexbuilder, packedLight);
        
        matrixStack.popPose();
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }
    
    
    private void renderSide(MatrixStack matrixStack, Vector3f lightNormal, float length, IVertexBuilder ivertexbuilder, int packedLight) {
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        matrixStack.pushPose();
        
        matrixStack.translate(0, 0, 0.125f);

        MatrixStack.Entry matrix = matrixStack.last();
        Matrix4f pose = matrix.pose();
        Matrix3f normal = matrix.normal();
        
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, -1, 0, 0.0F, 0.0F, lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, -1, 0, length, 0.0F, lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, 1, 0, length, 1.0f, lightNormal.x(), lightNormal.y(), lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, 1, 0, 0.0F, 1.0F, lightNormal.x(), lightNormal.y(), lightNormal.z());

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, -1, 0, 0.0F, 0.0F, -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, -1, 0, length, 0.0F, -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                length, 1, 0, length, 1.0f, -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        ClientUtil.vertex(pose, normal, ivertexbuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1, 
                0, 1, 0, 0.0F, 1.0F, -lightNormal.x(), -lightNormal.y(), -lightNormal.z());
        
        matrixStack.popPose();
    }

}
