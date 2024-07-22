package com.github.standobyte.jojo.client.render.entity.model.animnew.mojang;

import com.github.standobyte.jojo.client.ClientUtil;

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
