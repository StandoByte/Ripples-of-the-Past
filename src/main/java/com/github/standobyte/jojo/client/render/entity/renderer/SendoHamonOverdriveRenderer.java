package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.entity.HamonSendoOverdriveEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class SendoHamonOverdriveRenderer extends EntityRenderer<HamonSendoOverdriveEntity> {

    public SendoHamonOverdriveRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(HamonSendoOverdriveEntity p_110775_1_) {
        return null;
    }

    @Override
    public void render(HamonSendoOverdriveEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (shouldRender(entity)) {
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    protected boolean shouldRender(HamonSendoOverdriveEntity entity) {
        return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
    }
    
}
