package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.MRDetectorModel;
import com.github.standobyte.jojo.entity.MRDetectorEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class MRDetectorRenderer extends SimpleEntityRenderer<MRDetectorEntity, MRDetectorModel> {

    public MRDetectorRenderer(EntityRendererManager renderManager) {
        super(renderManager, new MRDetectorModel(), new ResourceLocation(JojoMod.MOD_ID, "textures/entity/mr_detector.png"));
    }    
    
    @Override
    protected void doRender(MRDetectorEntity entity, MRDetectorModel model, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.translate(0.0D, MathHelper.sin((entity.tickCount + partialTick) * 0.04F) * 0.04F, 0.0D);
        renderModel(entity, model, partialTick, matrixStack, buffer.getBuffer(model.renderType(getTextureLocation(entity))), packedLight);
        model.renderFlames(matrixStack, buffer, entityRenderDispatcher.camera);
        matrixStack.popPose();
    }
}
