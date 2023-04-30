package com.github.standobyte.jojo.client.render.armor.model;

import java.util.Collections;

import com.github.standobyte.jojo.client.ClientUtil;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

// Made with Blockbench 3.9.2


public class SatiporojaScarfArmorModel extends BipedModel<LivingEntity> {

    public SatiporojaScarfArmorModel(float size) {
        super(size);
        texWidth = 32;
        texHeight = 32;

        head.setTexSize(texWidth, texHeight);
        ClientUtil.clearCubes(head);
        head.setPos(0.0F, 0.5F, 0.0F);
        setRotationAngle(head, 0.0873F, 0.0F, 0.0F);
        head.texOffs(0, 7).addBox(-4.5F, -1.2F, -2.5F, 9.0F, 1.0F, 5.0F, 0.0F, false);
        head.texOffs(0, 0).addBox(-4.5F, 0.0F, -2.6F, 9.0F, 2.0F, 5.0F, 0.2F, false);
        head.texOffs(0, 13).addBox(-4.1F, -0.5F, -3.5F, 3.0F, 11.0F, 1.0F, -0.3F, false);
    }

    @Override
    protected Iterable<ModelRenderer> headParts() {
        return Collections.emptyList();
    }
    
    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(head);
    }
    
    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        setRotationAngle(head, body.xRot + 0.0873F, body.yRot, body.zRot);
        super.renderToBuffer(matrixStack, vertexBuilder, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}