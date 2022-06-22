package com.github.standobyte.jojo.client.model.entity.projectile;

import com.github.standobyte.jojo.entity.itemprojectile.ClackersEntity;
import com.github.standobyte.jojo.util.utils.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class ClackersModel extends EntityModel<ClackersEntity> {
    private final ModelRenderer clackers;
    private final ModelRenderer string1;
    private final ModelRenderer ball1;
    private final ModelRenderer string2;
    private final ModelRenderer ball2;

    public ClackersModel() {
        texWidth = 32;
        texHeight = 32;

        clackers = new ModelRenderer(this);
        clackers.setPos(0.0F, -4.0F, 0.0F);
        clackers.texOffs(0, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, -0.3F, false);

        string1 = new ModelRenderer(this);
        string1.setPos(0.0F, 0.0F, 0.0F);
        clackers.addChild(string1);
        string1.texOffs(24, 0).addBox(-0.5F, -7.5F, -0.5F, 1.0F, 8.0F, 1.0F, -0.4F, false);

        ball1 = new ModelRenderer(this);
        ball1.setPos(0.0F, -7.5F, 0.0F);
        string1.addChild(ball1);
        ball1.texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, -2.25F, false);

        string2 = new ModelRenderer(this);
        string2.setPos(0.0F, 0.0F, 0.0F);
        clackers.addChild(string2);
        string2.texOffs(24, 9).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F, -0.4F, false);

        ball2 = new ModelRenderer(this);
        ball2.setPos(0.0F, 7.5F, 0.0F);
        string2.addChild(ball2);
        ball2.texOffs(0, 12).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, -2.25F, false);
    }

    @Override
    public void setupAnim(ClackersEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        clackers.setPos(0.0F, -4.0F, 0.0F);
        if (!entity.isInGround()) {
            xRotation = (xRotation + ticks * 18.0F * (float) entity.getDeltaMovement().length()) % 360.0F;
        }
        clackers.xRot = xRotation * MathUtil.DEG_TO_RAD;
        clackers.yRot = yRotationOffset * MathUtil.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        clackers.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}