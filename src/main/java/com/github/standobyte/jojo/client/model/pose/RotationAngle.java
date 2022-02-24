package com.github.standobyte.jojo.client.model.pose;

import net.minecraft.client.renderer.model.ModelRenderer;

public class RotationAngle {
    private static final float PI = (float) Math.PI;
    private static final float DOUBLE_PI = PI * 2F;
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
    
    public void applyRotation(float rotationAmount) {
        modelRenderer.xRot = modelRenderer.xRot + smallestDiff(angleX - modelRenderer.xRot) * rotationAmount;
        modelRenderer.yRot = modelRenderer.yRot + smallestDiff(angleY - modelRenderer.yRot) * rotationAmount;
        modelRenderer.zRot = modelRenderer.zRot + smallestDiff(angleZ - modelRenderer.zRot) * rotationAmount;
    }
    
    private float smallestDiff(float diff) {
        diff %= DOUBLE_PI;
        if (diff > PI) {
            diff -= DOUBLE_PI;
        }
        else if (diff < -PI) {
            diff += DOUBLE_PI;
        }
        return diff;
    }
}
