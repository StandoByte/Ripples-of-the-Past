package com.github.standobyte.jojo.entity.stand;

import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

public enum TargetHitPart {
    HEAD,
    TORSO_ARMS,
    LEGS;
    
    public static TargetHitPart getHitTarget(Entity target, Entity aiming) {
        double distanceToTarget = JojoModUtil.getDistance(aiming, target.getBoundingBox());
        double aimY = aiming.getEyePosition(1.0F).add(aiming.getLookAngle().scale(distanceToTarget)).y;
        return getHitTarget(target, aimY - target.getY());
    }
    
    public static TargetHitPart getHitTarget(Entity target, double targetY) {
        double height = target.getBbHeight();
        if (targetY < height * 0.75) {
            if (targetY < height * 0.375) {
                return LEGS;
            }
            else {
                return TORSO_ARMS;
            }
        }
        else {
            return HEAD;
        }
    }

    public Vector3d getPartCenter(LivingEntity target) {
        switch (this) {
        case HEAD:
            return new Vector3d(target.getX(), target.getY(1.0), target.getZ());
        case TORSO_ARMS:
            return new Vector3d(target.getX(), target.getY(0.7), target.getZ())
                    .add(new Vector3d(target.getBbWidth() * 0.375F, 0, 0).yRot((180 - target.yRot) * MathUtil.DEG_TO_RAD));
        case LEGS:
            return new Vector3d(target.getX(), target.getY(0.0), target.getZ());
        default:
            return null;
        }
    }
}
