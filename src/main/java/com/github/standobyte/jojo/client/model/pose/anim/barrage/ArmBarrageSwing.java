package com.github.standobyte.jojo.client.model.pose.anim.barrage;

import java.util.Random;

import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel.VisibilityMode;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.utils.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ArmBarrageSwing<T extends StandEntity> extends AdditionalBarrageSwing<T> {
    private final HandSide side;
    private final Vector3d offset;
    private final float zRot;
    
    public ArmBarrageSwing(IBarrageAnimation<T> barrageAnim, float ticks, float ticksMax, HandSide side, StandEntity stand, double maxOffset) {
        super(barrageAnim, ticks, ticksMax);
        this.side = side;
        Random random = stand.getRandom();
        double upOffset = (random.nextDouble() - 0.5) * maxOffset;
        double leftOffset = random.nextDouble() * maxOffset / 2;
        double frontOffset = random.nextDouble() * 0.5;
        if (side == HandSide.RIGHT) {
            leftOffset *= -1;
        }
        zRot = MathUtil.wrapRadians((float) (Math.PI / 2 - MathHelper.atan2(upOffset, leftOffset)));
        offset = new Vector3d(leftOffset, upOffset, frontOffset);
    }
    
    public HandSide getSide() {
        return side;
    }
    
    @Override
    public void poseAndRender(T entity, StandEntityModel<T> model, MatrixStack matrixStack, IVertexBuilder buffer, 
            float yRotationOffset, float xRotation, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        model.setVisibility(entity, side == HandSide.LEFT ? VisibilityMode.LEFT_ARM_ONLY : VisibilityMode.RIGHT_ARM_ONLY, false);
        double zAdditional = (0.5F - Math.abs(0.5F - ticks / ticksMax));
        Vector3d offsetRot = new Vector3d(offset.x, -offset.y, offset.z + zAdditional).xRot(xRotation * MathUtil.DEG_TO_RAD);
        matrixStack.pushPose();
        matrixStack.translate(offsetRot.x, offsetRot.y, -offsetRot.z);
        barrageAnim.animateSwing(entity, ticks / ticksMax, side, yRotationOffset, xRotation, zRot);
        model.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha * 0.75F);
        matrixStack.popPose();
    }
}
