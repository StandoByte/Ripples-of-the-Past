package com.github.standobyte.jojo.client.render.entity.model.animnew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.molang.AnimContext;
import com.github.standobyte.jojo.client.render.entity.model.animnew.molang.KeyframeWithQuery;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.GeckoStandAnimator;

import net.minecraft.util.math.vector.Vector3f;

// kinda like RigidModelPose from the previous pose animating system
public class AnimTimestamp {
    private final Map<String, List<Transformation>> boneAnimations;
    
    protected AnimTimestamp(Map<String, List<Transformation>> boneAnimations) {
        this.boneAnimations = boneAnimations;
    }
    
    @Deprecated
    public static AnimTimestamp timestamp(Animation anim, float timeInSeconds) {
        if (timeInSeconds > anim.lengthInSeconds()) {
            if (anim.looping()) {
                timeInSeconds = timeInSeconds % anim.lengthInSeconds();
            }
            else {
                timeInSeconds = anim.lengthInSeconds();
            }
        }
        
        Map<String, List<Transformation>> boneAnimations = new HashMap<>();
        AnimContext.clearContext();
        for (Map.Entry<String, List<Transformation>> entry : anim.boneAnimations().entrySet()) {
            List<Transformation> timestampTransforms = new ArrayList<>();
            for (Transformation tf : entry.getValue()) {
                Keyframe[] keyframes = tf.keyframes();
                Vector3f vec = GeckoStandAnimator.lerpKeyframes(keyframes, timeInSeconds, 1);
                
                KeyframeWithQuery timestampKeyframe = KeyframeWithQuery.constant(vec).withKeyframe(0, Interpolations.LINEAR);
                Transformation timestampTf = new Transformation(tf.target(), new KeyframeWithQuery[] { timestampKeyframe });
                timestampTransforms.add(timestampTf);
            }
            boneAnimations.put(entry.getKey(), timestampTransforms);
        }
        
        return new AnimTimestamp(boneAnimations);
    }
    
    // TODO
    public static void pasteKeyframe(Animation anim, AnimTimestamp timestamp, float targetTime) {
        for (Map.Entry<String, List<Transformation>> timestampBone : timestamp.boneAnimations.entrySet()) {
            
        }
    }
    
}
