package com.github.standobyte.jojo.client.model.pose;

import net.minecraft.entity.Entity;

public class RigidModelPose<T extends Entity> implements IModelPose<T> {
    private IModelPose<T> wrappedPose;

    public RigidModelPose(IModelPose<T> pose) {
        this.wrappedPose = pose;
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotationOffset, float xRotation) {
        wrappedPose.poseModel(1.0F, entity, ticks, yRotationOffset, xRotation);
    }

}
