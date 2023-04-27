package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.HGEmeraldEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class HGEmeraldModel extends EntityModel<HGEmeraldEntity> {
    private final ModelRenderer emerald;

    public HGEmeraldModel() {
        texWidth = 32;
        texHeight = 32;
        emerald = new ModelRenderer(this);
        emerald.setPos(0.0F, 0.0F, 0.0F);
        emerald.texOffs(0, 0).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, false);
        emerald.texOffs(0, 9).addBox(-2.0F, -3.5F, -3.5F, 4.0F, 3.0F, 7.0F, 0.0F, false);
        emerald.texOffs(0, 19).addBox(-1.5F, -3.0F, -4.5F, 3.0F, 2.0F, 9.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(HGEmeraldEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        emerald.yRot = yRotationOffset * ((float)Math.PI / 180F);
        emerald.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        emerald.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}