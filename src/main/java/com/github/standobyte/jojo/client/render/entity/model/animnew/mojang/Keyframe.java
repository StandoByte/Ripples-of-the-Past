package com.github.standobyte.jojo.client.render.entity.model.animnew.mojang;

import net.minecraft.util.math.vector.Vector3f;

public class Keyframe {
    private final float timestamp;
    private final Vector3f target;
    private final Transformation.Interpolation interpolation;
    
    public Keyframe(float timestamp, Vector3f target, Transformation.Interpolation interpolation) {
        this.timestamp = timestamp;
        this.target = target;
        this.interpolation = interpolation;
    }
    
    public float timestamp() {
        return timestamp;
    }
    
    public Vector3f target() {
        return target;
    }
    
    public Transformation.Interpolation interpolation() {
        return interpolation;
    }
}
