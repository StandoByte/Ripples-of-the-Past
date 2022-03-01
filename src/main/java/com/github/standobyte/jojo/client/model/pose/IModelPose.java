package com.github.standobyte.jojo.client.model.pose;

import java.util.function.UnaryOperator;

import net.minecraft.entity.Entity;

public interface IModelPose<T extends Entity> {
    void poseModel(float rotationAmount, T entity, float ticks, float yRotationOffset, float xRotation);
    IModelPose<T> setEasing(UnaryOperator<Float> function);
}
