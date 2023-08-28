package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.function.UnaryOperator;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public interface IModelPose<T extends Entity> {
    void poseModel(float rotationAmount, T entity, float ticks, 
            float yRotOffsetRad, float xRotRad, HandSide side);
    IModelPose<T> setEasing(UnaryOperator<Float> function);
}
