package com.github.standobyte.jojo.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.storage.MapDecoration;

public class CustomIconMapRender {
    private static MatrixStack matrixStack;
    private static IRenderTypeBuffer buffer;
    @SuppressWarnings("unused")
    private static boolean active;
    private static int packedLight;
    
    public static void clCaptureIconRenderArgs(
            MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, 
            boolean pActive, int pPackedLight) {
        matrixStack = pMatrixStack;
        buffer = pBuffer;
        active = pActive;
        packedLight = pPackedLight;
    }
    
    public static void customIconRender(MapDecoration mapIcon, ResourceLocation iconPath, int index) {
        RenderType icon = RenderType.text(iconPath);
        
        if (matrixStack == null || buffer == null) {
            return;
        }
        
        matrixStack.pushPose();
        matrixStack.translate(((float)mapIcon.getX() / 2 + 64), ((float)mapIcon.getY() / 2 + 64), -0.02);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)(mapIcon.getRot() * 360) / 16));
        matrixStack.scale(4, 4, 3);
        matrixStack.translate(-0.125, 0.125, 0);
        Matrix4f matrix4f1 = matrixStack.last().pose();
        IVertexBuilder ivertexbuilder1 = buffer.getBuffer(icon);
        ivertexbuilder1.vertex(matrix4f1, -1,  1, (float)index * -0.001F).color(255, 255, 255, 255).uv(0, 0).uv2(packedLight).endVertex();
        ivertexbuilder1.vertex(matrix4f1,  1,  1, (float)index * -0.001F).color(255, 255, 255, 255).uv(1, 0).uv2(packedLight).endVertex();
        ivertexbuilder1.vertex(matrix4f1,  1, -1, (float)index * -0.001F).color(255, 255, 255, 255).uv(1, 1).uv2(packedLight).endVertex();
        ivertexbuilder1.vertex(matrix4f1, -1, -1, (float)index * -0.001F).color(255, 255, 255, 255).uv(0, 1).uv2(packedLight).endVertex();
        matrixStack.popPose();
    }
}
