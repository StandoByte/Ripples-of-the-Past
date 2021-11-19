package com.github.standobyte.jojo.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class EntityDistanceRayTraceResult extends EntityRayTraceResult {
    private final double distance;

    public EntityDistanceRayTraceResult(Entity entity, Vector3d pos, double distance) {
        super(entity, pos);
        this.distance = distance;
    }
    
    public double getTargetAABBDistance() {
        return distance;
    }

}
