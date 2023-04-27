package com.github.standobyte.jojo.client.render.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

// Made with Blockbench 3.9.2


public class CameraModel extends EntityModel<Entity> {
    private final ModelRenderer camera;
    private final ModelRenderer leg1;
    private final ModelRenderer leg2;
    private final ModelRenderer leg3;
    private final ModelRenderer leg4;

    public CameraModel() {
        texWidth = 64;
        texHeight = 32;

        camera = new ModelRenderer(this);
        camera.setPos(0.0F, 12.5F, 0.0F);
        camera.texOffs(0, 0).addBox(-4.0F, -20.5F, -5.0F, 8.0F, 8.0F, 10.0F, 0.0F, false);

        leg1 = new ModelRenderer(this);
        leg1.setPos(0.0F, -12.5F, -0.5F);
        camera.addChild(leg1);
        setRotationAngle(leg1, -0.1745F, 0.0F, 0.0F);
        leg1.texOffs(36, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 25.0F, 1.0F, 0.0F, false);

        leg2 = new ModelRenderer(this);
        leg2.setPos(0.0F, -12.5F, 0.5F);
        camera.addChild(leg2);
        setRotationAngle(leg2, 0.1745F, 0.0F, 0.0F);
        leg2.texOffs(36, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 25.0F, 1.0F, 0.0F, false);

        leg3 = new ModelRenderer(this);
        leg3.setPos(0.5F, -12.5F, 0.0F);
        camera.addChild(leg3);
        setRotationAngle(leg3, 0.0F, 0.0F, -0.1745F);
        leg3.texOffs(36, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 25.0F, 1.0F, 0.0F, false);

        leg4 = new ModelRenderer(this);
        leg4.setPos(-0.5F, -12.5F, 0.0F);
        camera.addChild(leg4);
        setRotationAngle(leg4, 0.0F, 0.0F, 0.1745F);
        leg4.texOffs(36, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 25.0F, 1.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(Entity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {}

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        camera.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}