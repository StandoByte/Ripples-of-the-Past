package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.HamonCutterEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class HamonCutterModel extends EntityModel<HamonCutterEntity> {
    private final ModelRenderer cutter;

    public HamonCutterModel() {
        texWidth = 32;
        texHeight = 32;
        cutter = new ModelRenderer(this);
        cutter.setPos(0.0F, 0.0F, 0.0F);
        cutter.texOffs(0, 0).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 1.0F, 6.0F, -0.4F, false);
        cutter.texOffs(0, 7).addBox(-3.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F, -0.395F, false);
    }

    @Override
    public void setupAnim(HamonCutterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        yRotationOffset = (yRotationOffset + ticks * 60F) % 360F;
        cutter.yRot = yRotationOffset * MathUtil.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        cutter.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}