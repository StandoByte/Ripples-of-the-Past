package com.github.standobyte.jojo.client.model.entity.mob;

import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;

import net.minecraft.client.renderer.entity.model.BipedModel;

public class HamonMasterModel extends BipedModel<HamonMasterEntity> {

	public HamonMasterModel() {
		super(0.0F);
	}
//
//	@Override
//	public void setupAnim(HamonMasterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
//		super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
//		setRotationAngle(leftArm, -0.5236F, 0.3927F, 0.0F);
//		setRotationAngle(rightArm, -0.5236F, -0.3927F, 0.0F);
//		setRotationAngle(leftLeg, -1.5708F, 0.5236F, 0.0F);
//		setRotationAngle(rightLeg, -1.5708F, -0.5236F, 0.0F);
//		leftLeg.setPos(2.9F, 12.0F, 1.0F);
//		rightLeg.setPos(-2.9F, 12.0F, 1.0F);
//	}
//
//	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
//		modelRenderer.xRot = x;
//		modelRenderer.yRot = y;
//		modelRenderer.zRot = z;
//	}
//
//	@Override
//    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
//		matrixStack.pushPose();
//		matrixStack.translate(0, 0.67, 0);
//		super.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//		matrixStack.popPose();
//	}

}
