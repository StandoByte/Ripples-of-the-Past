package com.github.standobyte.jojo.client.renderer.entity;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.model.entity.LeavesGliderModel;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class LeavesGliderRenderer extends SimpleEntityRenderer<LeavesGliderEntity, LeavesGliderModel> {

    public LeavesGliderRenderer(EntityRendererManager renderManager) {
        super(renderManager, new LeavesGliderModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/leaves_glider.png"));
    }
    
    @Override
    protected void renderModel(LeavesGliderEntity entity, LeavesGliderModel model, float partialTick, 
            MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight) {
        matrixStack.pushPose();
        matrixStack.translate(0, -entity.getBbHeight(), 0);
        int color = entity.getFoliageColor();
        int red = (color & 0xFF0000) >> 16;
        int green = (color & 0x00FF00) >> 8;
        int blue = color & 0x0000FF;
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 
                (float) red / 255F, (float) green / 255F, (float) blue / 255F, 1.0F);
        matrixStack.popPose();
    }

}
