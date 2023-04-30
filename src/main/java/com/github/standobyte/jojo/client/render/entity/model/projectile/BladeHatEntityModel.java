package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.itemprojectile.BladeHatEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class BladeHatEntityModel extends EntityModel<BladeHatEntity> {
    private final ModelRenderer hat;
    private final ModelRenderer pink_left;
    private final ModelRenderer pink_right;

    public BladeHatEntityModel() {
        texWidth = 32;
        texHeight = 32;

        hat = new ModelRenderer(this);
        hat.setPos(0.0F, 0.0F, 0.0F);
        hat.texOffs(0, 0).addBox(-4.0F, -3.75F, -4.0F, 8.0F, 3.0F, 8.0F, 0.75F, false);
        hat.texOffs(0, 25).addBox(-3.0F, -5.25F, -3.0F, 6.0F, 1.0F, 6.0F, 0.75F, false);
        hat.texOffs(0, 12).addBox(-6.0F, -0.0F, -6.0F, 12.0F, 0.0F, 12.0F, 0.0F, false);

        pink_left = new ModelRenderer(this);
        pink_left.setPos(-5.25F, -2.75F, 0.0F);
        hat.addChild(pink_left);
        setRotationAngle(pink_left, 0.0F, 0.0F, -0.3491F);
        pink_left.texOffs(0, 25).addBox(0.0F, -1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, false);

        pink_right = new ModelRenderer(this);
        pink_right.setPos(5.25F, -2.75F, 0.0F);
        hat.addChild(pink_right);
        setRotationAngle(pink_right, 0.0F, 0.0F, 0.3491F);
        pink_right.texOffs(0, 25).addBox(0.0F, -1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 0.0F, false);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    @Override
    public void setupAnim(BladeHatEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        if (!entity.isInGround() && entity.canUpdate()) {
            yRotationOffset = (yRotationOffset + ticks * 36.0F) % 360.0F;
        }
        hat.yRot = yRotationOffset * MathUtil.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        hat.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    
}
