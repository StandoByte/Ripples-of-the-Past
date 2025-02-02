package com.github.standobyte.jojo.client.render.entity.bb;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3f;

/**
 * The function that fixes the vertex order for mesh faces with 4 vertices in Blockbench format models.
 * Taken from <a href="https://github.com/JannisX11/blockbench/blob/368efc7c8275d11fac355efa90720ebcd850f3b8/js/outliner/mesh.js#L186">Blockbench's source code</a> (GPL v3.0),
 * the math is from <a href="https://github.com/mrdoob/three.js/blob/8540d9f9a6818db6879d8a92abe162ea7efa3475/src/math/Line3.js#L84">three.js library</a> (MIT).
 */
public class MeshVerticesHelper {
    
    public static void sortVertices(ModelRenderer.PositionTextureVertex[] vertices) {
        if (vertices.length < 4) return;

        if (MeshVerticesHelper.magicFunction(vertices[1].pos, vertices[2].pos, vertices[0].pos, vertices[3].pos)) {
            ArrayUtils.swap(vertices, 0, 1);
            ArrayUtils.swap(vertices, 0, 2);
        } else if (MeshVerticesHelper.magicFunction(vertices[0].pos, vertices[1].pos, vertices[2].pos, vertices[3].pos)) {
            ArrayUtils.swap(vertices, 1, 2);
        }
    }
    
    private static Vector3f _startP = new Vector3f();
    private static Vector3f _startEnd = new Vector3f();
    private static Vector3f normal = new Vector3f();
    
    private static boolean magicFunction(Vector3f base1, Vector3f base2, Vector3f top, Vector3f check) {
        // Construct a plane with coplanar points "base1" and "base2" with a normal towards "top"
        subVectors(_startP, top, base1);
        subVectors(_startEnd, base2, base1);
        float startEnd2 = _startEnd.dot(_startEnd);
        float startEnd_startP = _startEnd.dot(_startP);
        float t = startEnd_startP / startEnd2;
        subVectors(normal, base2, base1);
        normal.mul(t);
        normal.add(base1);
        normal.sub(top);
        
        float planeConstant = -base2.dot(normal);
        float distance = normal.dot(check) + planeConstant;
        return distance > 0;
    }

    private static void subVectors(Vector3f target, Vector3f a, Vector3f b) {
        target.set(a.x(), a.y(), a.z());
        target.sub(b);
    }
    
}
