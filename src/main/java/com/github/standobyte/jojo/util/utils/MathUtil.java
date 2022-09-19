package com.github.standobyte.jojo.util.utils;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

public class MathUtil {
    public static final float DEG_TO_RAD = (float) (Math.PI / 180D);
    public static final float RAD_TO_DEG = (float) (180D / Math.PI);
    private static final float PI = (float) Math.PI;
    private static final float DOUBLE_PI = PI * 2F;
    
    public static float yRotDegFromVec(Vector3d vec) {
        return (float) -MathHelper.atan2(vec.x, vec.z) * RAD_TO_DEG;
    }
    
    public static float xRotDegFromVec(Vector3d vec) {
        return (float) -MathHelper.atan2(vec.y, MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z)) * RAD_TO_DEG;
    }
    
    public static Vector3d relativeCoordsToAbsolute(double left, double up, double forward, float yAxisRot) {
        double yRotRad = yAxisRot * DEG_TO_RAD;
        return new Vector3d(
                left * Math.cos(yRotRad) - forward * Math.sin(yRotRad), 
                up, 
                left * Math.sin(yRotRad) + forward * Math.cos(yRotRad));
    }
    
    public static Vector3d relativeVecToAbsolute(Vector3d relativeVec, float yAxisRot) {
        return relativeCoordsToAbsolute(relativeVec.x, relativeVec.y, relativeVec.z, yAxisRot);
    }
    
    public static Vector2f xRotYRotOffsets(double angleXYRad, double z) {
        double xSq = -Math.cos(angleXYRad);
        double ySq = Math.sin(angleXYRad);
        xSq *= xSq;
        ySq *= ySq;
        double zSq = z * z;
        double angleXZ = Math.acos(Math.sqrt((xSq + zSq) / (xSq + ySq + zSq)));
        double angleYZ = Math.acos(Math.sqrt((ySq + zSq) / (xSq + ySq + zSq)));
        if (angleXYRad > Math.PI) {
            angleXZ *= -1;
        }
        if (angleXYRad > Math.PI / 2 && angleXYRad < Math.PI * 3 / 2) {
            angleYZ *= -1;
        }
        return new Vector2f((float) Math.toDegrees(angleYZ), (float) Math.toDegrees(angleXZ));
    }
    
    public static float rotLerpRad(float lerp, float angleA, float angleB) {
        return angleA + lerp * wrapRadians(angleB - angleA);
    }
    
    public static float wrapRadians(float angle) {
        angle %= DOUBLE_PI;
        if (angle >= PI) {
            angle -= DOUBLE_PI;
        }
        if (angle < -PI) {
            angle += DOUBLE_PI;
        }
        return angle;
     }
    
    public static float inverseArmorProtectionDamage(float damageAfterAbsorb, float armor, float toughness) {
        float f = armor / 25 - 1;
        float f2 = 25 * (1 + toughness / 8);
        return MathHelper.clamp(
                f2 * (f + (float) Math.sqrt(f * f + 2 * damageAfterAbsorb / f2)), 
                damageAfterAbsorb / (1 - armor / 125), 
                5 * damageAfterAbsorb);
    }
    
    public static float inverseLerp(float x, float a, float b) {
        return (x - a) / (b - a);
    }
    
    public static int fractionRandomInc(double num) {
        int numInt = MathHelper.floor(num);
        if (Math.random() < num - (double) numInt) {
            numInt++;
        }
        return numInt;
    }
}
