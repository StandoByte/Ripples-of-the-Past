package com.github.standobyte.jojo.util.general;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public class PlaneRectangle {
    private static final Random RANDOM = new Random();
    public final Vector3d pLD;
    public final Vector3d pLU;
    public final Vector3d pRU;
    public final Vector3d pRD;
    public final Vector3d center;
    public final float width;
    public final float height;
    public final Vector3d normalVec;
    
    public static PlaneRectangle create(Vector3d center, float xRot, float yRot, float width, float height) {
        Vector3d offset = new Vector3d(width / 2, height / 2, 0)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        Vector3d offset2 = new Vector3d(width / 2, -height / 2, 0)
                .xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        
        Vector3d pointLeftDown = center.add(offset);
        Vector3d pointLeftUp = center.add(offset2);
        Vector3d pointRightUp = center.add(offset.reverse());
        Vector3d pointRightDown = pointRightUp.add(pointLeftDown.subtract(pointLeftUp));
        
        return new PlaneRectangle(pointLeftDown, pointLeftUp, pointRightUp, pointRightDown, center, width, height);
    }
    
    private PlaneRectangle(Vector3d pointLeftDown, Vector3d pointLeftUp, Vector3d pointRightUp, Vector3d pointRightDown,
            Vector3d center, float width, float height) {
        this.pLD = pointLeftDown;
        this.pLU = pointLeftUp;
        this.pRU = pointRightUp;
        this.pRD = pointRightDown;
//        this.center = pointLeftDown.add(pointLeftUp.subtract(pointLeftDown).scale(0.5)).add(pointRightDown.subtract(pointLeftDown).scale(0.5));
        this.center = center;
        this.width = width;
        this.height = height;
        this.normalVec = pointRightUp.subtract(pointLeftDown).cross(pointLeftUp.subtract(pointRightDown)).normalize();
    }
    
//    public PlaneRectangle scale(double scale) {
//        return scale(scale, scale);
//    }
//    
//    public PlaneRectangle scale(double scaleX, double scaleY) {
//        Vector3d right = pRD.subtract(pLD).scale(scaleX * 0.5);
//        Vector3d up = pLU.subtract(pLD).scale(scaleY * 0.5);
//        return clockwisePoints(
//                center.add(right.reverse()).add(up.reverse()),
//                center.add(right.reverse()).add(up),
//                center.add(right).add(up));
//    }
    
    public Vector3d getUniformRandomPos() {
        return pLD
                .add(pRD.subtract(pLD).scale(RANDOM.nextDouble()))
                .add(pLU.subtract(pLD).scale(RANDOM.nextDouble()));
    }
    
    
    /**
     * @return The intersection point of the projectile's movement trajectory with this plane
     */
    @Nullable
    public Vector3d projectileIsPassing(Entity projectile) {
        Vector3d deltaMov = projectile.getDeltaMovement();
        Vector3d posCur = projectile.position();
        Vector3d posNext = posCur.add(deltaMov);
        double normalProjCur = posCur.subtract(center).dot(normalVec);
        double normalProjNext = posNext.subtract(center).dot(normalVec);
        if (normalProjCur > 0 && // current position is in front of the shield
                normalProjNext <= 0 /* next position would be behind the shield */ ) {
            double penetrationRatio = normalProjCur / (normalProjCur - normalProjNext);
            Vector3d intersectionPoint = posCur.add(deltaMov.scale(penetrationRatio));
            
            Vector3d centerToIntersectionVec = intersectionPoint.subtract(center);
            Vector3d horizontalShieldVec = pRD.subtract(pLD);
            Vector3d verticalShieldVec = pLU.subtract(pLD);
            boolean intersectsInShieldBounds = 
                    Math.abs(centerToIntersectionVec.dot(horizontalShieldVec)) <= width * width / 2 && // comparing the projections length with extra steps
                    Math.abs(centerToIntersectionVec.dot(verticalShieldVec)) <= height * height / 2;
            
            if (intersectsInShieldBounds) {
                return intersectionPoint;
            }
        }
        
        return null;
    }
    
}
