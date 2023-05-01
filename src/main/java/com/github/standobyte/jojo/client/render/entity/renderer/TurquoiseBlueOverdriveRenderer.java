package com.github.standobyte.jojo.client.render.entity.renderer;

import com.github.standobyte.jojo.entity.damaging.projectile.HamonTurquoiseBlueOverdriveEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class TurquoiseBlueOverdriveRenderer extends EntityRenderer<HamonTurquoiseBlueOverdriveEntity> {

    public TurquoiseBlueOverdriveRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(HamonTurquoiseBlueOverdriveEntity p_110775_1_) {
        return null;
    }

    @Override
    public void render(HamonTurquoiseBlueOverdriveEntity entity, float yRotation, float partialTick, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (shouldRender(entity)) {
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
    
    protected boolean shouldRender(HamonTurquoiseBlueOverdriveEntity entity) {
        return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
    }
    
}
