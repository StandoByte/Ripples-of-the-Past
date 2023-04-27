package com.github.standobyte.jojo.util.general;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

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
    
    
    
    
    public static Quaternion quaternionZYX(float x, float y, float z, boolean degrees) {
        if (degrees) {
            x *= DEG_TO_RAD;
            y *= DEG_TO_RAD;
            z *= DEG_TO_RAD;
        }

        float s1 = (float) Math.sin(0.5F * x);
        float c1 = (float) Math.cos(0.5F * x);
        float s2 = (float) Math.sin(0.5F * y);
        float c2 = (float) Math.cos(0.5F * y);
        float s3 = (float) Math.sin(0.5F * z);
        float c3 = (float) Math.cos(0.5F * z);

        float i = s1 * c2 * c3 - c1 * s2 * s3;
        float j = c1 * s2 * c3 + s1 * c2 * s3;
        float k = c1 * c2 * s3 - s1 * s2 * c3;
        float r = c1 * c2 * c3 + s1 * s2 * s3;
        
        return new Quaternion(i, j, k, r);
    }

    @SuppressWarnings("unused")
    public static class Matrix4ZYX {
        private float m00;
        private float m01;
        private float m02;
        private float m03;
        private float m10;
        private float m11;
        private float m12;
        private float m13;
        private float m20;
        private float m21;
        private float m22;
        private float m23;
        private float m30;
        private float m31;
        private float m32;
        private float m33;
        
        public Matrix4ZYX(Quaternion q) {
            float f = q.i();
            float f1 = q.j();
            float f2 = q.k();
            float f3 = q.r();
            float f4 = 2.0F * f * f;
            float f5 = 2.0F * f1 * f1;
            float f6 = 2.0F * f2 * f2;
            this.m00 = 1.0F - f5 - f6;
            this.m11 = 1.0F - f6 - f4;
            this.m22 = 1.0F - f4 - f5;
            this.m33 = 1.0F;
            float f7 = f * f1;
            float f8 = f1 * f2;
            float f9 = f2 * f;
            float f10 = f * f3;
            float f11 = f1 * f3;
            float f12 = f2 * f3;
            this.m10 = 2.0F * (f7 + f12);
            this.m01 = 2.0F * (f7 - f12);
            this.m20 = 2.0F * (f9 - f11);
            this.m02 = 2.0F * (f9 + f11);
            this.m21 = 2.0F * (f8 + f10);
            this.m12 = 2.0F * (f8 - f10);
        }
        
        public Vector3f rotationVec() {
            double xRot;
            double yRot;
            double zRot;

            yRot = Math.asin(-MathHelper.clamp(m20, -1, 1 ));

            if (Math.abs(m20) < 0.999999) {
                xRot = MathHelper.atan2(m21, m22);
                zRot = MathHelper.atan2(m10, m00);
            } else {
                xRot = 0;
                zRot = MathHelper.atan2(-m01, m11);
            }
            
            return new Vector3f((float) xRot, (float) yRot, (float) zRot);
        }
    }
}
