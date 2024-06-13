package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;

public class StandTwoHandedBarrageAnimation<T extends StandEntity> extends TwoHandedBarrageAnimation<T, StandEntityModel<T>> {

    public StandTwoHandedBarrageAnimation(StandEntityModel<T> model, IModelPose<T> loop, 
            IModelPose<T> recovery) {
        super(model, loop, recovery);
    }

    @Override
    public BarrageSwingsHolder<T, StandEntityModel<T>> getBarrageSwingsHolder(T entity) {
        return (BarrageSwingsHolder<T, StandEntityModel<T>>) entity.getBarrageSwingsHolder();
    }

    @Override
    protected float swingsToAdd(T entity, float loop, float lastLoop) {
        return standEntitySwings(entity, loop, lastLoop, getLoopLen());
    }

    @Override
    protected double maxSwingOffset(T entity) {
        return maxStandSwingOffset(entity);
    }

    @Override
    protected void addSwing(T entity, BarrageSwingsHolder<T, StandEntityModel<T>> swings, HandSide side, float f,
            double maxOffset) {
        swings.addSwing(new StandArmBarrageSwing<>(this, f, getLoopLen(), side, maxOffset));
    }
    
    @Override
    public void animateSwing(T entity, StandEntityModel<T> model, float loopCompletion, 
            HandSide side, float yRotOffsetRad, float xRotRad, float zRotOffsetRad) {
        super.animateSwing(entity, model, loopCompletion, side, yRotOffsetRad, xRotRad, zRotOffsetRad);
        ModelRenderer arm = model.getArm(side);
        arm.zRot = arm.zRot + HumanoidStandModel.barrageHitEasing(loopCompletion) * zRotOffsetRad;
    }
    
    @Override
    public void beforeSwingAfterimageRender(MatrixStack matrixStack, StandEntityModel<T> model, float loopCompletion, HandSide side) {
        model.applyXRotation();
    }
}
