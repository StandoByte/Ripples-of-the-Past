package com.github.standobyte.jojo.client.model.pose;

import java.util.function.UnaryOperator;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public class RigidModelPose<T extends Entity> implements IModelPose<T> {
    private IModelPose<T> wrappedPose;

    public RigidModelPose(IModelPose<T> pose) {
        this.wrappedPose = pose;
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotationOffset, float xRotation, HandSide side) {
        wrappedPose.poseModel(1.0F, entity, ticks, yRotationOffset, xRotation, side);
    }

    @Override
    public IModelPose<T> setEasing(UnaryOperator<Float> function) {
        return this;
    }

}
