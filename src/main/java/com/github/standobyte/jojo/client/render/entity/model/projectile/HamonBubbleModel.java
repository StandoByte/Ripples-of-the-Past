package com.github.standobyte.jojo.client.render.entity.model.projectile;

import java.util.Random;

import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class HamonBubbleModel extends EntityModel<HamonBubbleEntity> {
    private final ModelRenderer bubble;
    private final Random random = new Random();
    private int entityId;

    public HamonBubbleModel() {
        texWidth = 16;
        texHeight = 16;
        bubble = new ModelRenderer(this);
        bubble.setPos(0.0F, 0.0F, 0.0F);
        bubble.texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
        bubble.texOffs(6, 0).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 1.0F, 1.0F, 0.2F, false);
        bubble.texOffs(0, 4).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.2F, false);
        bubble.texOffs(6, 2).addBox(-0.5F, -1.5F, -1.0F, 1.0F, 1.0F, 2.0F, 0.2F, false);
    }

    @Override
    public void setupAnim(HamonBubbleEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        bubble.yRot = yRotationOffset * ((float)Math.PI / 180F);
        bubble.xRot = xRotation * ((float)Math.PI / 180F);
        entityId = entity.getId();
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        random.setSeed(entityId);
        float size = 1.0F + (random.nextFloat() - 0.5F) * 0.4F;
        matrixStack.pushPose();
        matrixStack.scale(size, size, size);
        bubble.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        matrixStack.popPose();
    }
}