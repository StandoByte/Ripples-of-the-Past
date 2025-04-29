package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.model.LeavesGliderModel;
import com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile.CDBlockBulletRenderer;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class LeavesGliderRenderer extends SimpleEntityRenderer<LeavesGliderEntity, LeavesGliderModel> {
    private static final ResourceLocation DEFAULT_OAK_LEAVES = new ResourceLocation("textures/block/oak_leaves.png");

    public LeavesGliderRenderer(EntityRendererManager renderManager) {
        super(renderManager, new LeavesGliderModel(), null);
    }
    
    @Override
    public ResourceLocation getTextureLocation(LeavesGliderEntity entity) {
        ResourceLocation tex = entity.getLeavesTexture();
        if (tex == null) {
            tex = CDBlockBulletRenderer.getBlockTexture(entity.getLeavesBlock());
            if (tex == null) {
                tex = DEFAULT_OAK_LEAVES;
            }
            entity.setLeavesTex(tex);
        }
        return tex;
    }
    
    @Override
    protected void renderModel(LeavesGliderEntity entity, LeavesGliderModel model, float partialTick, 
            MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight) {
        matrixStack.pushPose();
        matrixStack.translate(0, -entity.getBbHeight(), 0);
        float[] rgb = ClientUtil.rgb(entity.getFoliageColor());
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 
                rgb[0], rgb[1], rgb[2], 1.0F);
        matrixStack.popPose();
    }

}
