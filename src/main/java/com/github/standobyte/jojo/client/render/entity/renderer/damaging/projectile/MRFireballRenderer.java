package com.github.standobyte.jojo.client.render.entity.renderer.damaging.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.MRFireballEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SpriteRenderer;

public class MRFireballRenderer extends SpriteRenderer<MRFireballEntity> {
    
    public MRFireballRenderer(EntityRendererManager renderManager) {
        super(renderManager, Minecraft.getInstance().getItemRenderer(), 1F, true);
    }

    @Override
    public void render(MRFireballEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        if (!entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player)) {
            super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
        }
    }
}
