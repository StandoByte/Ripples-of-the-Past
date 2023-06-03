package com.github.standobyte.jojo.client.render.entity.model;

import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.3


public class LeavesGliderModel extends EntityModel<LeavesGliderEntity> {
    private final ModelRenderer glider;
    private final ModelRenderer frontLeft;
    private final ModelRenderer frontRight;

    public LeavesGliderModel() {
        texWidth = 16;
        texHeight = 16;

        glider = new ModelRenderer(this);
        glider.setPos(0.0F, 0.0F, 0.0F);
        glider.texOffs(0, 0).addBox(-20.0F, -0.5F, -4.11F, 40.0F, 1.0F, 24.0F, -0.375F, false);
        
        frontLeft = new ModelRenderer(this);
        frontLeft.setPos(0.0F, 0.0F, -19.625F);
        glider.addChild(frontLeft);
        setRotationAngle(frontLeft, 0.0F, -0.6806F, 0.0F);
        frontLeft.texOffs(0, 25).addBox(-0.375F, -0.5F, -0.375F, 26.0F, 1.0F, 16.0F, -0.375F, false);

        frontRight = new ModelRenderer(this);
        frontRight.setPos(0.0F, 0.0F, -19.625F);
        glider.addChild(frontRight);
        setRotationAngle(frontRight, 0.0F, 0.6806F, 0.0F);
        frontRight.texOffs(0, 42).addBox(-25.625F, -0.5F, -0.375F, 26.0F, 1.0F, 16.0F, -0.375F, false);
    }

    @Override
    public void setupAnim(LeavesGliderEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        glider.yRot = yRotationOffset * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        glider.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}