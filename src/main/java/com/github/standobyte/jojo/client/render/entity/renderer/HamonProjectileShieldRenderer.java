package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.rendertype.CustomRenderType;
import com.github.standobyte.jojo.entity.HamonProjectileShieldEntity;
import com.github.standobyte.jojo.util.general.PlaneRectangle;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class HamonProjectileShieldRenderer extends EntityRenderer<HamonProjectileShieldEntity> {
    private static final ResourceLocation GLINT_TEXTURE = new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectile_shield.png");
    
    public HamonProjectileShieldRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }
    
    @Override
    public ResourceLocation getTextureLocation(HamonProjectileShieldEntity entity) {
        return GLINT_TEXTURE;
    }
    
    @Override
    public void render(HamonProjectileShieldEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        MatrixStack.Entry matrixstack$entry = matrixStack.last();
        IVertexBuilder vertexBuilder = buffer.getBuffer(CustomRenderType.hamonProjectileShield(getTextureLocation(entity)));
        PlaneRectangle rectangle = entity.getShieldRectangle();
        packedLight = ClientUtil.MAX_MODEL_LIGHT;
        
        // back side
        vertex(matrixstack$entry, vertexBuilder, packedLight, rectangle.pRD.subtract(entity.position()), 1.0F, 1, 1);
        vertex(matrixstack$entry, vertexBuilder, packedLight, rectangle.pLD.subtract(entity.position()), 1.0F, 0, 1);
        vertex(matrixstack$entry, vertexBuilder, packedLight, rectangle.pLU.subtract(entity.position()), 1.0F, 0, 0);
        vertex(matrixstack$entry, vertexBuilder, packedLight, rectangle.pRU.subtract(entity.position()), 1.0F, 1, 0);
        
        // front side
        vertex(matrixstack$entry, vertexBuilder, packedLight, rectangle.pLD.subtract(entity.position()), 1.0F, 1, 1);
        vertex(matrixstack$entry, vertexBuilder, packedLight, rectangle.pRD.subtract(entity.position()), 1.0F, 0, 1);
        vertex(matrixstack$entry, vertexBuilder, packedLight, rectangle.pRU.subtract(entity.position()), 1.0F, 0, 0);
        vertex(matrixstack$entry, vertexBuilder, packedLight, rectangle.pLU.subtract(entity.position()), 1.0F, 1, 0);
    }
    
    private void vertex(MatrixStack.Entry matrixstack$entry, IVertexBuilder vertexBuilder,
            int packedLight, Vector3d pos, float strength, float u, float v) {
        float alpha = 0.5F * strength;
        ClientUtil.vertex(matrixstack$entry, vertexBuilder, 
                packedLight, OverlayTexture.NO_OVERLAY, alpha, alpha, alpha, 1.0F, 
                (float) pos.x, (float) pos.y, (float) pos.z, u, v);
    }
}
