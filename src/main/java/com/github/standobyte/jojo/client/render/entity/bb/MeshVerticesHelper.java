package com.github.standobyte.jojo.client.render.entity.bb;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.client.render.entity.bb.ParseGenericModel.ModelParsed.Vertex;

import net.minecraft.util.math.vector.Vector3f;

/**
 * The function that fixes the vertex order for mesh faces with 4 vertices in Blockbench format models.
 * Taken from <a href="https://github.com/JannisX11/blockbench/blob/368efc7c8275d11fac355efa90720ebcd850f3b8/js/outliner/mesh.js#L186">Blockbench's source code</a> (GPL v3.0),
 * the math is from <a href="https://github.com/mrdoob/three.js/blob/8540d9f9a6818db6879d8a92abe162ea7efa3475/src/math/Line3.js#L84">three.js library</a> (MIT).
 */
public class MeshVerticesHelper {
    
    static void sortVertices(Vertex[] vertices) {
        if (vertices.length < 4) return;

        if (MeshVerticesHelper.magicFunction(vertices[1].pos, vertices[2].pos, vertices[0].pos, vertices[3].pos)) {
            ArrayUtils.swap(vertices, 0, 1);
            ArrayUtils.swap(vertices, 0, 2);
        } else if (MeshVerticesHelper.magicFunction(vertices[0].pos, vertices[1].pos, vertices[2].pos, vertices[3].pos)) {
            ArrayUtils.swap(vertices, 1, 2);
        }
    }
    
    private static Vector3f base1 = new Vector3f();
    private static Vector3f base2 = new Vector3f();
    private static Vector3f top = new Vector3f();
    private static Vector3f check = new Vector3f();
    private static Vector3f _startP = new Vector3f();
    private static Vector3f _startEnd = new Vector3f();
    private static Vector3f normal = new Vector3f();
    
    private static boolean magicFunction(float[] _base1, float[] _base2, float[] _top, float[] _check) {
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
        
        float planeConstant = -base2.dot(normal);
        float distance = normal.dot(check) + planeConstant;
        return distance > 0;
    }

    private static Vector3f tmp = new Vector3f();
    private static void subVectors(Vector3f target, float[] a, float[] b) {
        target.set(a);
        tmp.set(b);
        target.sub(tmp);
    }
    
}
