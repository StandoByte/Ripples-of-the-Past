package com.github.standobyte.jojo.client.render.entity.pose;

import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class RotationAngle {
    public final ModelRenderer modelRenderer;
    public final float angleX;
    public final float angleY;
    public final float angleZ;
    public boolean wrapDegrees = true;
    
    public RotationAngle(ModelRenderer modelRenderer, float angleX, float angleY, float angleZ) {
        this.modelRenderer = modelRenderer;
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;
    }
    
    public static RotationAngle fromDegrees(ModelRenderer modelRenderer, float angleX, float angleY, float angleZ) {
        return new RotationAngle(modelRenderer, angleX * MathUtil.DEG_TO_RAD, angleY * MathUtil.DEG_TO_RAD, angleZ * MathUtil.DEG_TO_RAD);
    }
    
    // in case you apply the rotations to the model on the Edit tab
    // if you do it on the Animate tab, use RotationAngle.fromDegrees
    public static RotationAngle blockbenchRotationAngle(ModelRenderer modelRenderer, float angleX, float angleY, float angleZ) {
        return fromDegrees(modelRenderer, -angleX, -angleY, angleZ);
    }
    
    
    
    public RotationAngle(ModelRenderer modelRenderer, double angleX, double angleY, double angleZ) {
        this(modelRenderer, (float) angleX, (float) angleY, (float) angleZ);
    }
    
    public static RotationAngle fromDegrees(ModelRenderer modelRenderer, double angleX, double angleY, double angleZ) {
        return fromDegrees(modelRenderer, (float) angleX, (float) angleY, (float) angleZ);
    }
    public static RotationAngle blockbenchRotationAngle(ModelRenderer modelRenderer, double angleX, double angleY, double angleZ) {
        return blockbenchRotationAngle(modelRenderer, (float) angleX, (float) angleY, (float) angleZ);
    }
    
    
    
    public RotationAngle noDegreesWrapping() {
        wrapDegrees = false;
        return this;
    }
    
    public void applyRotation(float rotationAmount) {
        TernaryOperator<Float> lerp = getLerp();
        modelRenderer.xRot = lerp.apply(rotationAmount, modelRenderer.xRot, angleX);
        modelRenderer.yRot = lerp.apply(rotationAmount, modelRenderer.yRot, angleY);
        modelRenderer.zRot = lerp.apply(rotationAmount, modelRenderer.zRot, angleZ);
    }
    
    private TernaryOperator<Float> getLerp() {
        return wrapDegrees ? MathUtil::rotLerpRad : MathHelper::lerp;
    }
    
    private static interface TernaryOperator<T> {
        T apply(T a, T b, T c);
    }
}
