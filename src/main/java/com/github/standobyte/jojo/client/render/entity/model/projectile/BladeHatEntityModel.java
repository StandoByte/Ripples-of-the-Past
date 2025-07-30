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
    private final ModelRenderer cube_r1;
    private final ModelRenderer cube_r2;
    private final ModelRenderer cube_r3;
    private final ModelRenderer cube_r4;
    private final ModelRenderer cube_r5;
    private final ModelRenderer cube_r6;

    public BladeHatEntityModel() {
        texWidth = 64;
        texHeight = 64;

        hat = new ModelRenderer(this);
        hat.setPos(0.0F, 0.0F, 0.0F);
        hat.texOffs(0, 23).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, 0.0F, false);
        hat.texOffs(32, 23).addBox(-4.0F, -6.1F, -4.0F, 8.0F, 4.0F, 8.0F, 0.25F, false);
        hat.texOffs(0, 12).addBox(-4.5F, -2.85F, -4.5F, 9.0F, 2.0F, 9.0F, -0.125F, false);
        hat.texOffs(36, 12).addBox(-3.0F, -2.85F, -4.6F, 6.0F, 2.0F, 1.0F, -0.1F, false);
        hat.texOffs(0, 37).addBox(-1.5F, -2.85F, -4.75F, 3.0F, 2.0F, 1.0F, 0.1F, false);
        hat.texOffs(0, 0).addBox(-4.0F, 0.0F, -6.0F, 8.0F, 0.0F, 12.0F, 0.0F, false);

        cube_r1 = new ModelRenderer(this);
        cube_r1.setPos(8.6198F, -1.9135F, 0.0F);
        hat.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 0.0F, -0.3927F);
        cube_r1.texOffs(32, 35).addBox(-5.0F, 0.0F, -5.0F, 2.0F, 0.0F, 10.0F, 0.0F, true);

        cube_r2 = new ModelRenderer(this);
        cube_r2.setPos(-8.6198F, -1.9135F, 0.0F);
        hat.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 0.0F, 0.3927F);
        cube_r2.texOffs(32, 35).addBox(3.0F, 0.0F, -5.0F, 2.0F, 0.0F, 10.0F, 0.0F, false);

        cube_r3 = new ModelRenderer(this);
        cube_r3.setPos(-4.5746F, -3.5345F, 0.9056F);
        hat.addChild(cube_r3);
        setRotationAngle(cube_r3, -0.3927F, 0.0F, -0.2182F);
        cube_r3.texOffs(8, 37).addBox(0.0F, -2.5F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, true);

        cube_r4 = new ModelRenderer(this);
        cube_r4.setPos(-4.5F, -3.6F, 0.0F);
        hat.addChild(cube_r4);
        setRotationAngle(cube_r4, -0.1745F, 0.0F, -0.2182F);
        cube_r4.texOffs(36, 15).addBox(0.0F, -2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, true);

        cube_r5 = new ModelRenderer(this);
        cube_r5.setPos(4.5746F, -3.5345F, 0.9056F);
        hat.addChild(cube_r5);
        setRotationAngle(cube_r5, -0.3927F, 0.0F, 0.2182F);
        cube_r5.texOffs(8, 37).addBox(0.0F, -2.5F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, false);

        cube_r6 = new ModelRenderer(this);
        cube_r6.setPos(4.5F, -3.6F, 0.0F);
        hat.addChild(cube_r6);
        setRotationAngle(cube_r6, -0.1745F, 0.0F, 0.2182F);
        cube_r6.texOffs(36, 15).addBox(0.0F, -2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 0.0F, false);
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
