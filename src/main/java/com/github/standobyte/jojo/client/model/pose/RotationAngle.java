package com.github.standobyte.jojo.client.model.pose;

import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.client.renderer.model.ModelRenderer;

public class RotationAngle {
    public final ModelRenderer modelRenderer;
    private final float angleX;
    private final float angleY;
    private final float angleZ;
    
    public RotationAngle(ModelRenderer modelRenderer, float angleX, float angleY, float angleZ) {
        this.modelRenderer = modelRenderer;
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;
    }
    
    public static RotationAngle fromDegrees(ModelRenderer modelRenderer, float angleX, float angleY, float angleZ) {
        return new RotationAngle(modelRenderer, angleX * MathUtil.DEG_TO_RAD, angleY * MathUtil.DEG_TO_RAD, angleZ * MathUtil.DEG_TO_RAD);
    }
    
    public void applyRotation(float rotationAmount) {
        modelRenderer.xRot = MathUtil.rotLerpRad(rotationAmount, modelRenderer.xRot, angleX);
        modelRenderer.yRot = MathUtil.rotLerpRad(rotationAmount, modelRenderer.yRot, angleY);
        modelRenderer.zRot = MathUtil.rotLerpRad(rotationAmount, modelRenderer.zRot, angleZ);
    }
}
