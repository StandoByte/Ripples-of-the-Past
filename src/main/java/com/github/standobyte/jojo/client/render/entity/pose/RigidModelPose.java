package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.function.UnaryOperator;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public class RigidModelPose<T extends Entity> implements IModelPose<T> {
    private IModelPose<T> wrappedPose;

    public RigidModelPose(IModelPose<T> pose) {
        this.wrappedPose = pose;
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide side) {
        wrappedPose.poseModel(1.0F, entity, ticks, yRotOffsetRad, xRotRad, side);
    }

    @Override
    public IModelPose<T> setEasing(UnaryOperator<Float> function) {
        return this;
    }

}
