package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import java.util.Random;

import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public abstract class ArmBarrageSwing<T extends Entity, M extends EntityModel<T>> extends AdditionalBarrageSwing<T, M> {
    private static final Random RANDOM = new Random();
    private final HandSide side;
    private final Vector3d offset;
    private final float zRot;
    
    public ArmBarrageSwing(IBarrageAnimation<T, M> barrageAnim, float ticks, float ticksMax, HandSide side, double maxOffset) {
        super(barrageAnim, ticks, ticksMax);
        this.side = side;
        double upOffset = (RANDOM.nextDouble() - 0.5) * maxOffset;
        double leftOffset = RANDOM.nextDouble() * maxOffset / 2;
        double frontOffset = RANDOM.nextDouble() * 0.5;
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
    public void poseAndRender(T entity, M model, MatrixStack matrixStack, IVertexBuilder buffer, 
            float yRotOffsetRad, float xRotRad, 
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        setArmOnlyModelVisibility(entity, model, side);
        double zAdditional = (0.5F - Math.abs(0.5F - ticks / ticksMax));
        Vector3d offsetRot = new Vector3d(offset.x, -offset.y, offset.z + zAdditional).xRot(xRotRad);
        matrixStack.pushPose();
        matrixStack.translate(offsetRot.x, offsetRot.y, -offsetRot.z);
        barrageAnim.animateSwing(entity, model, ticks / ticksMax, side, yRotOffsetRad, xRotRad, zRot);
        barrageAnim.beforeSwingAfterimageRender(matrixStack, model, ticks / ticksMax, side);
        model.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha * 0.75F);
        matrixStack.popPose();
    }
    
    protected abstract void setArmOnlyModelVisibility(T entity, M model, HandSide side);
}
