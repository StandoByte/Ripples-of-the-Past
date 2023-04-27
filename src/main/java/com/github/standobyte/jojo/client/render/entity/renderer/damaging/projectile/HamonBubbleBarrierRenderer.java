package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.projectile.HamonBubbleBarrierModel;
import com.github.standobyte.jojo.client.render.entity.renderer.SimpleEntityRenderer;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleBarrierEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

public class HamonBubbleBarrierRenderer extends SimpleEntityRenderer<HamonBubbleBarrierEntity, HamonBubbleBarrierModel> {

    public HamonBubbleBarrierRenderer(EntityRendererManager renderManager) {
        super(renderManager, new HamonBubbleBarrierModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/projectiles/hamon_bubble_barrier.png"));
    }
    
    @Override
    protected void renderModel(HamonBubbleBarrierEntity entity, HamonBubbleBarrierModel model, 
            float partialTick, MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight) {
        matrixStack.pushPose();
        float size = entity.getSize(partialTick);
        if (size < 1) {
            matrixStack.scale(size, size, size);
        }
        model.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.popPose();
    }

}
