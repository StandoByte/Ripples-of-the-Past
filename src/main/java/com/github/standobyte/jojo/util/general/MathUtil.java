package com.github.standobyte.jojo.util.general;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.StreamSupport;

import com.github.standobyte.jojo.util.mc.reflection.ReflectionUtil;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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
    
    public static int round(double value) {
        int i = (int) value;
        double frac = value > i ? value - i : i - value;
        if (frac < 0.5) {
            return i;
        }
        else {
            return value > i ? i + 1 : i - 1;
        }
    }
    
    public static <T> Optional<T> getRandomWeightedInt(Iterable<T> items, ToIntFunction<T> getWeight, Random random) {
        ToIntFunction<T> getWeightSafe = element -> Math.max(getWeight.applyAsInt(element), 0);
        int weightSum = StreamSupport.stream(items.spliterator(), false)
                .mapToInt(getWeightSafe).sum();
        if (weightSum <= 0) return Optional.empty();
        
        int randomNum = random.nextInt(weightSum);

        for (T element: items) {
            randomNum -= getWeightSafe.applyAsInt(element);
            if (randomNum < 0) {
                return Optional.of(element);
            }
        }
        
        return Optional.empty();
    }
    
    public static <T> Optional<T> getRandomWeightedDouble(Iterable<T> items, ToDoubleFunction<T> getWeight, Random random) {
        ToDoubleFunction<T> getWeightSafe = element -> Math.max(getWeight.applyAsDouble(element), 0);
        double weightSum = StreamSupport.stream(items.spliterator(), false)
                .mapToDouble(getWeightSafe).sum();
        if (weightSum <= 0) return Optional.empty();
        
        double randomNum = random.nextDouble() * weightSum;

        for (T element: items) {
            randomNum -= getWeightSafe.applyAsDouble(element);
            if (randomNum < 0) {
                return Optional.of(element);
            }
        }
        
        return Optional.empty();
    }
    
    public static float fadeOut(float time, float maxTime, float fractionUntilFadeOut) {
        if (fractionUntilFadeOut >= 1) return 1;
        float f = 1 / (1 - fractionUntilFadeOut);
        return MathHelper.clamp(time * -f / maxTime + f, 0, 1);
    }
    
    
    /**
     * Interpolates a point on a Catmull-Rom Spline. This spline has a property that if there are two
     * splines with arguments {@code p0, p1, p2, p3} and {@code p1, p2, p3, p4}, the resulting curve
     * will have a continuous first derivative at {@code p2}, where the two input curves connect. For
     * higher-dimensional curves, the interpolation on the curve is done component-wise: for
     * inputs {@code delta, (p0x, p0y), (p1x, p1y), (p2x, p2y), (p3x, p3y)}, the output is
     * {@code (catmullRom(delta, p0x, p1x, p2x, p3x), catmullRom(delta, p0y, p1y, p2y, p3y))}.
     * 
     * @see <a href="https://en.wikipedia.org/wiki/Cubic_Hermite_spline#Catmull%E2%80%93Rom_spline">Cubic Hermite spline (Catmull\u2013Rom spline)</a>
     * 
     * @param delta the progress along the interpolation
     * @param p0 the previous data point to assist in curve-smoothing
     * @param p1 the output if {@code delta} is 0
     * @param p2 the output if {@code delta} is 1
     * @param p3 the next data point to assist in curve-smoothing
     */
    public static float catmullRom(float delta, float p0, float p1, float p2, float p3) {
        return 0.5f * (2.0f * p1 + (p2 - p0) * delta + (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3) * delta * delta + (3.0f * p1 - p0 - 3.0f * p2 + p3) * delta * delta * delta);
    }
    
    
    public static Vector3f multiplyPoint(Matrix4f matrix, Vector3d point) {
        Vector3f pointF = new Vector3f((float) point.x, (float) point.y, (float) point.z);
        Vector3f res = new Vector3f();
        float w;
        res.setX(getM(matrix, 0, 0) * pointF.x() + getM(matrix, 0, 1) * pointF.y() + getM(matrix, 0, 2) * pointF.z() + getM(matrix, 0, 3));
        res.setY(getM(matrix, 1, 0) * pointF.x() + getM(matrix, 1, 1) * pointF.y() + getM(matrix, 1, 2) * pointF.z() + getM(matrix, 1, 3));
        res.setZ(getM(matrix, 2, 0) * pointF.x() + getM(matrix, 2, 1) * pointF.y() + getM(matrix, 2, 2) * pointF.z() + getM(matrix, 2, 3));
        w = getM(matrix, 3, 0) * pointF.x() + getM(matrix, 3, 1) * pointF.y() + getM(matrix, 3, 2) * pointF.z() + getM(matrix, 3, 3);
        
        w = 1F / w;
        res.mul(w);
        return res;
    }
    
    private static float getM(Matrix4f matrix, int i, int j) {
        return ReflectionUtil.getFloatFieldValue(M_FIELDS[i][j], matrix);
    }
    
    private static final Field M00 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226575_a_");
    private static final Field M01 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226576_b_");
    private static final Field M02 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226577_c_");
    private static final Field M03 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226578_d_");
    private static final Field M10 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226579_e_");
    private static final Field M11 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226580_f_");
    private static final Field M12 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226581_g_");
    private static final Field M13 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226582_h_");
    private static final Field M20 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226583_i_");
    private static final Field M21 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226584_j_");
    private static final Field M22 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226585_k_");
    private static final Field M23 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226586_l_");
    private static final Field M30 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226587_m_");
    private static final Field M31 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226588_n_");
    private static final Field M32 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226589_o_");
    private static final Field M33 = ObfuscationReflectionHelper.findField(Matrix4f.class, "field_226590_p_");
    private static final Field[][] M_FIELDS = new Field[][] {
        {M00, M01, M02, M03},
        {M10, M11, M12, M13},
        {M20, M21, M22, M23},
        {M30, M31, M32, M33}
    };
    
    
    
    
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
    
    
    
    public static int min(int num1, int num2, int... nums) {
        int min = (num1 <= num2) ? num1 : num2;
        for (int num : nums) {
            if (num < min) {
                min = num;
            }
        }
        return min;
    }
    
    public static float min(float num1, float num2, float... nums) {
        float min = (num1 <= num2) ? num1 : num2;
        for (float num : nums) {
            if (num < min) {
                min = num;
            }
        }
        return min;
    }
    
    public static double min(double num1, double num2, double... nums) {
        double min = (num1 <= num2) ? num1 : num2;
        for (double num : nums) {
            if (num < min) {
                min = num;
            }
        }
        return min;
    }
    
    public static int max(int num1, int num2, int... nums) {
        int max = (num1 >= num2) ? num1 : num2;
        for (int num : nums) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }
    
    public static float max(float num1, float num2, float... nums) {
        float max = (num1 >= num2) ? num1 : num2;
        for (float num : nums) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }
    
    public static double max(double num1, double num2, double... nums) {
        double max = (num1 >= num2) ? num1 : num2;
        for (double num : nums) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }
}
