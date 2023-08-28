package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public class ModelPoseSided<T extends Entity> implements IModelPose<T> {
    private final Map<HandSide, IModelPose<T>> poses = new EnumMap<>(HandSide.class);
    
    public ModelPoseSided(IModelPose<T> poseLeft, IModelPose<T> poseRight) {
        poses.put(HandSide.LEFT, poseLeft);
        poses.put(HandSide.RIGHT, poseRight);
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide side) {
        poses.get(side).poseModel(rotationAmount, entity, ticks, yRotOffsetRad, xRotRad, side);
    }
    
    @Override
    public ModelPoseSided<T> setEasing(UnaryOperator<Float> function) {
        for (IModelPose<T> pose : poses.values()) {
            pose.setEasing(function);
        }
        return this;
    }
}
