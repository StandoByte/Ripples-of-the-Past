package com.github.standobyte.jojo.client.render.entity.model;

import com.github.standobyte.jojo.entity.CrimsonBubbleEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class CrimsonBubbleModel extends EntityModel<CrimsonBubbleEntity> {
    private final ModelRenderer bubble;
    
    public CrimsonBubbleModel() {
        super(RenderType::entityTranslucent);
        texWidth = 64;
        texHeight = 64;

        bubble = new ModelRenderer(this);
        bubble.setPos(0.0F, -5.0F, 0.0F);
        bubble.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        bubble.texOffs(32, 0).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 10.0F, 0.0F, false);
        bubble.texOffs(0, 16).addBox(-2.0F, -2.0F, -5.5F, 4.0F, 4.0F, 11.0F, 0.0F, false);
        bubble.texOffs(30, 16).addBox(-5.0F, -3.0F, -3.0F, 10.0F, 6.0F, 6.0F, 0.0F, false);
        bubble.texOffs(30, 28).addBox(-5.5F, -2.0F, -2.0F, 11.0F, 4.0F, 4.0F, 0.0F, false);
        bubble.texOffs(0, 31).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 10.0F, 6.0F, 0.0F, false);
        bubble.texOffs(24, 36).addBox(-2.0F, -5.5F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(CrimsonBubbleEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        bubble.yRot = yRotationOffset * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bubble.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}