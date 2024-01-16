package com.github.standobyte.jojo.client.render.entity.pose;

import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;

public class ModelPoseTransitionMultiple<T extends Entity> implements IModelPose<T> {
    private final NavigableMap<Float, ModelPoseTransition<T>> transitions = new TreeMap<>();
    private UnaryOperator<Float> animPointFunc = x -> x;
    
    private ModelPoseTransitionMultiple(Builder<T> builder, IModelPose<T> finalPose) {
        Map.Entry<Float, IModelPose<T>> lastEntry = null;
        for (Map.Entry<Float, IModelPose<T>> builderEntry : builder.poses.entrySet()) {
            addTransition(lastEntry, builderEntry.getValue());
            lastEntry = builderEntry;
        }
        addTransition(lastEntry, finalPose);
    }
    
    private void addTransition(@Nullable Map.Entry<Float, IModelPose<T>> prevEntry, IModelPose<T> nextPose) {
        if (prevEntry != null) {
            transitions.put(prevEntry.getKey(), new ModelPoseTransition<T>(prevEntry.getValue(), nextPose));
        }
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide side) {
        rotationAmount = animPointFunc.apply(rotationAmount);
        Map.Entry<Float, ModelPoseTransition<T>> part = transitions.floorEntry(rotationAmount);
        if (part != null) {
            Map.Entry<Float, ModelPoseTransition<T>> nextPart = transitions.ceilingEntry(rotationAmount);
            float nextPartPoint = nextPart != null ? nextPart.getKey() : 1.0F;
            float rotation = part.getKey() == nextPartPoint ? nextPartPoint : MathUtil.inverseLerp(rotationAmount, part.getKey(), nextPartPoint);
            part.getValue().poseModel(rotation, entity, ticks, yRotOffsetRad, xRotRad, side);
        }
    }

    @Override
    public ModelPoseTransitionMultiple<T> setEasing(UnaryOperator<Float> function) {
        this.animPointFunc = function;
        return this;
    }

    public static class Builder<T extends Entity> {
        private final SortedMap<Float, IModelPose<T>> poses = new TreeMap<>();
        
        public Builder(IModelPose<T> startingPose) {
            addPose(0F, startingPose);
        }
        
        public Builder<T> addPose(float point, IModelPose<T> pose) {
            poses.put(point, pose);
            return this;
        }
        
        public ModelPoseTransitionMultiple<T> build(IModelPose<T> finalPose) {
            return new ModelPoseTransitionMultiple<>(this, finalPose);
        }
    }
}
