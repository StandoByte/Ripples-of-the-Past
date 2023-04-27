package com.github.standobyte.jojo.client.render.entity.model.projectile;

import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class SCRapierModel extends EntityModel<SCRapierEntity> {
    private final ModelRenderer rapier;

    public SCRapierModel() {
        texWidth = 128;
        texHeight = 128;
        rapier = new ModelRenderer(this);
        rapier.setPos(-0.5F, 0.0F, 0.0F);
        rapier.texOffs(32, 72).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 15.0F, -0.3F, false);
    }

    @Override
    public void setupAnim(SCRapierEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        rapier.yRot = yRotationOffset * ((float)Math.PI / 180F);
        rapier.xRot = xRotation * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        rapier.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}