package com.github.standobyte.jojo.client.model.pose;

import java.util.function.UnaryOperator;

import net.minecraft.entity.Entity;

public class ModelPoseTransition<T extends Entity> implements IModelPose<T> {
    private final IModelPose<T> pose1;
    private final ModelPose<T> pose2;
    
    public ModelPoseTransition(IModelPose<T> pose1, ModelPose<T> pose2) {
        this.pose1 = pose1;
        this.pose2 = pose2;
    }

    @Override
    public void poseModel(float transition, T entity, float ticks, float yRotationOffset, float xRotation) {
        pose1.poseModel(1.0F, entity, ticks, yRotationOffset, xRotation);
        pose2.poseModel(transition, entity, ticks, yRotationOffset, xRotation);
    }
    
    public ModelPoseTransition<T> setTransitionFunction(UnaryOperator<Float> function) {
        pose2.setTransitionFunction(function);
        return this;
    }

}
