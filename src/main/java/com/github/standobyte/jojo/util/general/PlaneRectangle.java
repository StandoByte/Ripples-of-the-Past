package com.github.standobyte.jojo.util.general;

import java.util.Random;

import net.minecraft.util.math.vector.Vector3d;

public class PlaneRectangle {
    private static final Random RANDOM = new Random();
    public final Vector3d pLD;
    public final Vector3d pLU;
    public final Vector3d pRU;
    public final Vector3d pRD;
    public final Vector3d center;
    
    public static PlaneRectangle vertical(Vector3d pLD, Vector3d pRU) {
        return new PlaneRectangle(pLD, new Vector3d(pLD.x, pRU.y, pLD.z), pRU, new Vector3d(pRU.x, pLD.y, pRU.z));
    }
    
    public static PlaneRectangle clockwisePoints(Vector3d pLD, Vector3d pLU, Vector3d pRU) {
        return new PlaneRectangle(pLD, pLU, pRU, pRU.add(pLD.subtract(pLU)));
    }
    
    private PlaneRectangle(Vector3d pLD, Vector3d pLU, Vector3d pRU, Vector3d pRD) {
        this.pLD = pLD;
        this.pLU = pLU;
        this.pRU = pRU;
        this.pRD = pRD;
        this.center = pLD.add(pLU.subtract(pLD).scale(0.5)).add(pRD.subtract(pLD).scale(0.5));
    }
    
    public PlaneRectangle scale(double scale) {
        return scale(scale, scale);
    }
    
    public PlaneRectangle scale(double scaleX, double scaleY) {
        Vector3d right = pRD.subtract(pLD).scale(scaleX * 0.5);
        Vector3d up = pLU.subtract(pLD).scale(scaleY * 0.5);
        return clockwisePoints(
                center.add(right.reverse()).add(up.reverse()),
                center.add(right.reverse()).add(up),
                center.add(right).add(up));
    }
    
    public Vector3d getUniformRandomPos() {
        return pLD
                .add(pRD.subtract(pLD).scale(RANDOM.nextDouble()))
                .add(pLU.subtract(pLD).scale(RANDOM.nextDouble()));
    }
}
