package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public class ConditionalModelPose<T extends Entity> implements IModelPose<T> {
    private final List<PoseCondition<T>> poseConditions = new ArrayList<>();
    
    public ConditionalModelPose<T> addPose(@Nullable Predicate<T> condition, IModelPose<T> pose) {
        poseConditions.add(new PoseCondition<T>(condition == null ? entity -> true : condition, pose));
        return this;
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad,
            HandSide side) {
        for (PoseCondition<T> poseCondition : poseConditions) {
            if (poseCondition.condition.test(entity)) {
                poseCondition.pose.poseModel(rotationAmount, entity, ticks, yRotOffsetRad, xRotRad, side);
                return;
            }
        }
    }

    @Override
    public IModelPose<T> setEasing(UnaryOperator<Float> function) {
        poseConditions.stream()
        .map(poseCondition -> poseCondition.pose)
        .forEach(pose -> pose.setEasing(function));
        return this;
    }

    private static class PoseCondition<T extends Entity> {
        private final Predicate<T> condition;
        private final IModelPose<T> pose;
        
        private PoseCondition(Predicate<T> condition, IModelPose<T> pose) {
            this.condition = condition;
            this.pose = pose;
        }
    }
}
