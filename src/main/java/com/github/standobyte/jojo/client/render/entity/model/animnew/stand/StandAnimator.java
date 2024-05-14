package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class StandAnimator {
    private final Map<String, Animation> namedAnimations = new HashMap<>();
//    private Animation idleAnim;
//    private final Animation idlePose0;
//    private List<Animation> summonAnims = new ArrayList<>();
//    private Map<StandPose, StandActionAnimation> actionAnims = new HashMap<>();
    
    public StandAnimator() {}
    
    
    public void putNamedAnim(String name, Animation anim) {
        namedAnimations.put(name, anim);
    }

    public Animation getNamedAnim(String name) {
        return namedAnimations.get(name);
    }
    
//    public StandAnimator addIdleAnim(Animation anim) {
//        this.idleAnim = anim;
//        return this;
//    }
//    
//    public StandAnimator addSummonAnim(Animation anim) {
//        // TODO
//        return this;
//    }
//    
//    public StandAnimator addSummonAnimFromPose(Animation staticPose) {
//        // TODO
//        return this;
//    }
//    
//    public StandAnimator addActionAnim(StandPose standAction, StandActionAnimation anim) {
//        // TODO
//        return this;
//    }
//    
//    
//    public boolean animate() {
//        return false;
//    }
    
    
    private static final Vector3f TEMP = new Vector3f();
    public static void animate(StandEntityModel<?> model, Animation animation, float ticks, float scale) {
        float seconds = animation.looping() ? (ticks / 20.0f) % animation.lengthInSeconds() : ticks / 20.0f;
        for (Map.Entry<String, List<Transformation>> entry : animation.boneAnimations().entrySet()) {
            ModelRenderer modelPart = model.getModelPart(entry.getKey());
            if (modelPart != null) {
                List<Transformation> transformations = entry.getValue();
                for (Transformation tf : transformations) {
                    Keyframe[] keyframes = tf.keyframes();
                    int i = Math.max(0, MathHelper.binarySearch(0, keyframes.length, index -> seconds <= keyframes[index].timestamp()) - 1);
                    int j = Math.min(keyframes.length - 1, i + 1);
                    Keyframe keyframe = keyframes[i];
                    Keyframe keyframe2 = keyframes[j];
                    float h = seconds - keyframe.timestamp();
                    float k = j != i ? MathHelper.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
                    keyframe2.interpolation().apply(TEMP, k, keyframes, i, j, scale);
                    tf.target().apply(modelPart, TEMP);
                }
            }
        }
    }
}
