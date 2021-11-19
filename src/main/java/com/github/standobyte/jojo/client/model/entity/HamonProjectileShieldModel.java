package com.github.standobyte.jojo.client.model.entity;

import com.github.standobyte.jojo.entity.HamonProjectileShieldEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class HamonProjectileShieldModel extends EntityModel<HamonProjectileShieldEntity> {
	private final ModelRenderer shield;
	
	public HamonProjectileShieldModel() {
	    super(RenderType::entityTranslucent);
        texWidth = 64;
        texHeight = 64;

        shield = new ModelRenderer(this);
        shield.setPos(0.0F, -24.0F, 0.0F);
        shield.texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, 16.0F, false);
	}

    @Override
    public void setupAnim(HamonProjectileShieldEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		shield.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}