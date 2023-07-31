package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.HamonProjectileShieldEntity;
import com.github.standobyte.jojo.util.general.PlaneRectangle;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class HamonProjectileShieldRenderer extends EntityRenderer<HamonProjectileShieldEntity> {
    
    public HamonProjectileShieldRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }
    
    @Deprecated
    @Override
    public ResourceLocation getTextureLocation(HamonProjectileShieldEntity entity) {
        return new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectile_shield.png");
    }
    
    @Override
    public void render(HamonProjectileShieldEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        MatrixStack.Entry matrixstack$entry = matrixStack.last();
        IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.outline(getTextureLocation(entity)));
        PlaneRectangle rectangle = entity.getShieldRectangle();
        
        vertex(matrixstack$entry, vertexBuilder, 
                packedLight, rectangle.pRD.subtract(entity.position()), 1, 1);
        vertex(matrixstack$entry, vertexBuilder, 
                packedLight, rectangle.pLD.subtract(entity.position()), 0, 1);
        vertex(matrixstack$entry, vertexBuilder, 
                packedLight, rectangle.pLU.subtract(entity.position()), 0, 0);
        vertex(matrixstack$entry, vertexBuilder, 
                packedLight, rectangle.pRU.subtract(entity.position()), 1, 0);
    }
    
    private void vertex(MatrixStack.Entry matrixstack$entry, IVertexBuilder vertexBuilder,
            int packedLight, Vector3d pos, float u, float v) {
        ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F, 
                (float) pos.x, (float) pos.y, (float) pos.z, u, v);
    }
}
