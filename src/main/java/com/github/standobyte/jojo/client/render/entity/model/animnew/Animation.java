package com.github.standobyte.jojo.client.render.entity.model.animnew;

import java.util.List;
import java.util.Map;

public class Animation {
    private final float lengthInSeconds;
    private final boolean looping;
    private final Map<String, List<Transformation>> boneAnimations;
    
    public Animation(float lengthInSeconds, boolean looping, Map<String, List<Transformation>> boneAnimations) {
        this.lengthInSeconds = lengthInSeconds;
        this.looping = looping;
        this.boneAnimations = boneAnimations;
    }
    
    public float lengthInSeconds() {
        return lengthInSeconds;
    }
    
    public boolean looping() {
        return looping;
    }
    
    public Map<String, List<Transformation>> boneAnimations() {
        return boneAnimations;
    }
}
