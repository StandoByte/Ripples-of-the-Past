package com.github.standobyte.jojo.client.model.pose;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public class ConditionalModelPose<T extends Entity> implements IModelPose<T> {
    private final SortedMap<Predicate<T>, IModelPose<T>> poseChecks = new TreeMap<>();
    
    // FIXME (!!!!) breaks the renderers init for whatever reason
    public ConditionalModelPose<T> addPose(@Nullable Predicate<T> condition, IModelPose<T> pose) {
        poseChecks.put(condition == null ? entity -> true : condition, pose);
        return this;
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotationOffset, float xRotation,
            HandSide side) {
        for (Map.Entry<Predicate<T>, IModelPose<T>> entry : poseChecks.entrySet()) {
            if (entry.getKey().test(entity)) {
                entry.getValue().poseModel(rotationAmount, entity, ticks, yRotationOffset, xRotation, side);
                return;
            }
        }
    }

    @Override
    public IModelPose<T> setEasing(UnaryOperator<Float> function) {
        for (IModelPose<T> model : poseChecks.values()) {
            model.setEasing(function);
        }
        return this;
    }

}
