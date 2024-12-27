package com.github.standobyte.jojo.client.render.entity.model.animnew.mojang;

import java.util.Arrays;

import com.github.standobyte.jojo.client.render.entity.model.animnew.IModelRendererScale;
import com.github.standobyte.jojo.client.render.entity.model.animnew.molang.KeyframeWithQuery;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3f;

public class Transformation {
    private final Target target;
    private final KeyframeWithQuery[] keyframeWrappers;
    private final Keyframe[] keyframes;
    
    public Transformation(Target target, KeyframeWithQuery[] keyframes) {
        this.target = target;
        this.keyframeWrappers = keyframes;
        this.keyframes = Arrays.stream(keyframes).map(KeyframeWithQuery::getKeyframe).toArray(Keyframe[]::new);
    }
    
    public Target target() {
        return target;
    }
    
    public Keyframe[] keyframes() {
        for (KeyframeWithQuery query : keyframeWrappers) {
            query.evaluate();
        }
        return keyframes;
    }
    
    public static interface Target {
        public void apply(ModelRenderer var1, Vector3f var2);
    }
    
    public static class Targets {
        public static final Target TRANSLATE = Transformation::translateModelPart;
        public static final Target ROTATE = Transformation::rotateModelPart;
        public static final Target SCALE = Transformation::scaleModelPart;
        
    }
    
    public static interface Interpolation {
        public Vector3f apply(Vector3f dest, float delta, Keyframe[] keyframes, int start, int end, float scale);
    }
    
    
    public static void translateModelPart(ModelRenderer modelRenderer, Vector3f tlVec) {
        modelRenderer.x += tlVec.x();
        modelRenderer.y += tlVec.y();
        modelRenderer.z += tlVec.z();
    }
    
    public static void rotateModelPart(ModelRenderer modelRenderer, Vector3f rotVec) {
        modelRenderer.xRot += rotVec.x();
        modelRenderer.yRot += rotVec.y();
        modelRenderer.zRot += rotVec.z();
    }
    
    public static void scaleModelPart(ModelRenderer modelRenderer, Vector3f scaleVec) {
        ((IModelRendererScale) modelRenderer).setScale(scaleVec.x(), scaleVec.y(), scaleVec.z());
    }
}
