package com.github.standobyte.jojo.client.render.entity.model.animnew.mojang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

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
    
    
    public static class Builder {
        private final float lengthInSeconds;
        private final Map<String, List<Transformation>> transformations = new HashMap<>();
        private boolean looping;

        public static Builder create(float lengthInSeconds) {
            return new Builder(lengthInSeconds);
        }

        private Builder(float lengthInSeconds) {
            this.lengthInSeconds = lengthInSeconds;
        }

        public Builder looping() {
            this.looping = true;
            return this;
        }

        public Builder addBoneAnimation(String name2, Transformation transformation) {
            this.transformations.computeIfAbsent(name2, name -> Lists.newArrayList()).add(transformation);
            return this;
        }

        public Animation build() {
            return new Animation(this.lengthInSeconds, this.looping, this.transformations);
        }
    }
}
