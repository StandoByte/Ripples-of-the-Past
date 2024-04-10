package com.github.standobyte.jojo.client.render.entity.model.animnew;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3f;

public class Transformation {
    private final Target target;
    private final Keyframe[] keyframes;
    
    public Transformation(Target target, Keyframe[] keyframes) {
        this.target = target;
        this.keyframes = keyframes;
    }
    
    public Target target() {
        return target;
    }
    
    public Keyframe[] keyframes() {
        return keyframes;
    }
    
    public static interface Target {
        public void apply(ModelRenderer var1, Vector3f var2);
    }
    
    public static class Interpolations {
        public static final Interpolation LINEAR = (dest, delta, keyframes, start, end, scale) -> {
            Vector3f startVec = keyframes[start].target();
            Vector3f endVec = keyframes[end].target();
            Vector3f lerped = startVec.copy();
            lerped.lerp(endVec, delta);
            dest.set(lerped.x(), lerped.y(), lerped.z());
            return dest;
        };
        public static final Interpolation CUBIC = (dest, delta, keyframes, start, end, scale) -> {
            Vector3f p0Vec = keyframes[Math.max(0, start - 1)].target();
            Vector3f startVec = keyframes[start].target();
            Vector3f endVec = keyframes[end].target();
            Vector3f p3Vec = keyframes[Math.min(keyframes.length - 1, end + 1)].target();
            dest.set(
                    MathUtil.catmullRom(delta, p0Vec.x(), startVec.x(), endVec.x(), p3Vec.x()) * scale, 
                    MathUtil.catmullRom(delta, p0Vec.y(), startVec.y(), endVec.y(), p3Vec.y()) * scale, 
                    MathUtil.catmullRom(delta, p0Vec.z(), startVec.z(), endVec.z(), p3Vec.z()) * scale);
            return dest;
        };
    }
    
    public static class Targets {
        public static final Target TRANSLATE = ClientUtil::translateModelPart;
        public static final Target ROTATE = ClientUtil::rotateModelPart;
        /**
         * Placeholder - 1.16's ModelRenderers do not have scale fields
         */
        public static final Target SCALE = ClientUtil::scaleModelPart;
    }
    
    public static interface Interpolation {
        public Vector3f apply(Vector3f dest, float delta, Keyframe[] keyframes, int start, int end, float scale);
    }
}
