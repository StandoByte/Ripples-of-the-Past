package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.CDBloodCutterEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 4.1.3


public class CDBloodCutterModel extends EntityModel<CDBloodCutterEntity> {
    private final ModelRenderer cutter;

    public CDBloodCutterModel() {
        texWidth = 16;
        texHeight = 16;

        cutter = new ModelRenderer(this);
        cutter.setPos(0.0F, -3.1F, 0.0F);
        cutter.texOffs(0, 0).addBox(-0.5F, -3.1F, -0.4F, 1.0F, 6.0F, 4.0F, -0.4F, false);
    }

    @Override
    public void setupAnim(CDBloodCutterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        cutter.yRot = yRotationOffset * ((float)Math.PI / 180F);
        cutter.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        cutter.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}