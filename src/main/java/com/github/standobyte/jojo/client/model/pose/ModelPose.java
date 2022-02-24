package com.github.standobyte.jojo.client.model.pose;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelPose<T extends Entity> implements IModelPose<T> {
    private final Map<ModelRenderer, RotationAngle> rotations = new HashMap<>();
    private UnaryOperator<Float> transitionFunc = x -> x;
    private ModelAnim<T> additionalAnim = null;

    public ModelPose(RotationAnglesArray rotations) {
        for (RotationAngle rotation : rotations) {
            putRotation(rotation);
        }
    }
    
    public ModelPose<T> putRotation(RotationAngle rotation) {
        if (rotation.modelRenderer != null) {
            rotations.put(rotation.modelRenderer, rotation);
        }
        return this;
    }
    
    public ModelPose<T> setTransitionFunction(UnaryOperator<Float> function) {
        this.transitionFunc = function;
        return this;
    }
    
    public ModelPose<T> setAdditionalAnim(ModelAnim<T> anim) {
        this.additionalAnim = anim;
        return this;
    }
    
    public RigidModelPose<T> createRigid() {
        return new RigidModelPose<T>(this);
    }
    
    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotationOffset, float xRotation) {
        applyRotations(rotationAmount);
        additionalAnim(rotationAmount, entity, ticks, yRotationOffset, xRotation);
    }
    
    protected void applyRotations(float rotationAmount) {
        rotationAmount = transitionFunc.apply(rotationAmount);
        for (Map.Entry<ModelRenderer, RotationAngle> rotation : rotations.entrySet()) {
            rotation.getValue().applyRotation(rotationAmount);
        }
    }
    
    protected void additionalAnim(float rotationAmount, T entity, float ticks, float yRotationOffset, float xRotation) {
        if (additionalAnim != null) {
            additionalAnim.setupAnim(rotationAmount, entity, ticks, yRotationOffset, xRotation);
        }
    }
    
    
    
    public static interface ModelAnim<T extends Entity> {
        void setupAnim(float rotationAmount, T entity, float ticks, float yRotationOffset, float xRotation);
    }
}
