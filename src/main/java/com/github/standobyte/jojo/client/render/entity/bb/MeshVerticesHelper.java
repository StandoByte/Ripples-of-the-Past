package com.github.standobyte.jojo.client.render.entity.bb;

import net.minecraft.util.math.vector.Vector3f;

public class MeshVerticesHelper {
    private static Vector3f base1 = new Vector3f();
    private static Vector3f base2 = new Vector3f();
    private static Vector3f top = new Vector3f();
    private static Vector3f check = new Vector3f();
    private static Vector3f _startP = new Vector3f();
    private static Vector3f _startEnd = new Vector3f();
    private static Vector3f normal = new Vector3f();
    
    static boolean magicFunction(float[] _base1, float[] _base2, float[] _top, float[] _check) {
        // Construct a plane with coplanar points "base1" and "base2" with a normal towards "top"
        base1.set(_base1);
        base2.set(_base2);
        top.set(_top);
        check.set(_check);
        
        subVectors(_startP, _top, _base1);
        subVectors(_startEnd, _base2, _base1);
        float startEnd2 = _startEnd.dot(_startEnd);
        float startEnd_startP = _startEnd.dot(_startP);
        float t = startEnd_startP / startEnd2;
        subVectors(normal, _base2, _base1);
        normal.mul(t);
        normal.add(base1);
        normal.sub(top);
        
        Vector3f planeNormal = normal.copy();
        float planeConstant = -base2.dot(planeNormal);
        float distance = planeNormal.dot(check) + planeConstant;
        return distance > 0;
    }

    private static Vector3f tmp = new Vector3f();
    private static void subVectors(Vector3f target, float[] a, float[] b) {
        target.set(a);
        tmp.set(b);
        target.sub(tmp);
    }
    
}
