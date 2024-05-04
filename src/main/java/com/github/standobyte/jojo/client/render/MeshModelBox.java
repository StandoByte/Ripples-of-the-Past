package com.github.standobyte.jojo.client.render;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.ModelRenderer.PositionTextureVertex;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;

public class MeshModelBox extends ModelRenderer.ModelBox {
    
    private MeshModelBox(Builder builder) {
        super(0, 0, 
                builder.minX, 
                builder.minY, 
                builder.minZ, 
                builder.maxX - builder.minX, 
                builder.maxY - builder.minY, 
                builder.maxZ - builder.minZ, 
                0, 0, 0, 
                false, 1, 1);
        
        ModelRenderer.TexturedQuad[] quads = builder.quads.toArray(new ModelRenderer.TexturedQuad[0]);
        ClientReflection.setPolygons(this, quads);
    }
    
    // a convenience method for chainable calls
    public void addCube(ModelRenderer modelRenderer) {
        ClientReflection.addCube(modelRenderer, this);
    }
    
    
    
    public static class Builder {
        private final boolean livingEntityRenderHacks;
        private final MeshFaceBuilder faceBuilder;
        private float minX;
        private float minY;
        private float minZ;
        private float maxX;
        private float maxY;
        private float maxZ;
        private final List<ModelRenderer.TexturedQuad> quads = new ArrayList<>();
        
        public Builder(boolean livingEntityRenderHacks, float texWidth, float texHeight) {
            this.livingEntityRenderHacks = livingEntityRenderHacks;
            faceBuilder = new MeshFaceBuilder(this, texWidth, texHeight);
        }
        
        public Builder(boolean livingEntityRenderHacks, Model model) {
            this(livingEntityRenderHacks, model.texWidth, model.texHeight);
        }
        
        public MeshFaceBuilder startFace(Direction lightingDir) {
            if (livingEntityRenderHacks) {
                lightingDir = lightingDir.getAxis() == Axis.Z ? lightingDir : lightingDir.getOpposite();
            }
            return faceBuilder.withState(lightingDir, null, false, false);
        }
        
        public MeshFaceBuilder startFace(Vector3f faceNormal) {
            return faceBuilder.withState(null, faceNormal, false, false);
        }
        
        public MeshFaceBuilder startFaceCalcNormal() {
            return faceBuilder.withState(null, null, true, false);
        }
        
        public MeshFaceBuilder startFaceCalcNormal(boolean invertVec) {
            return faceBuilder.withState(null, null, true, invertVec);
        }
        
        
        public MeshModelBox buildCube() {
            MeshModelBox cube = new MeshModelBox(this);
            return cube;
        }
        
        
        public class MeshFaceBuilder {
            private final MeshModelBox.Builder boxBuilder;
            private final float texWidth;
            private final float texHeight;
            
            private Direction direction;
            private Vector3f faceNormal;
            private boolean calcNormalFromVertices;
            private boolean invertCalcNormal;
            
            private List<ModelRenderer.PositionTextureVertex> vertices = new ArrayList<>();
            
            private MeshFaceBuilder(MeshModelBox.Builder boxBuilder, float texWidth, float texHeight) {
                this.boxBuilder = boxBuilder;
                this.texWidth = texWidth;
                this.texHeight = texHeight;
            }
            
            public MeshFaceBuilder withVertex(double x, double y, double z, double texU, double texV) {
                if (boxBuilder.livingEntityRenderHacks) {
                    x = -x;
                    y = -y;
                }
                float xF = (float) x;
                float yF = (float) y;
                float zF = (float) z;
                boxBuilder.minX = Math.min(boxBuilder.minX, xF);
                boxBuilder.minY = Math.min(boxBuilder.minY, yF);
                boxBuilder.minZ = Math.min(boxBuilder.minZ, zF);
                boxBuilder.maxX = Math.max(boxBuilder.maxX, xF);
                boxBuilder.maxY = Math.max(boxBuilder.maxY, yF);
                boxBuilder.maxZ = Math.max(boxBuilder.maxZ, zF);
                
                ModelRenderer.PositionTextureVertex vertex = new ModelRenderer.PositionTextureVertex(
                        xF, yF, zF, (float) texU / texWidth, (float) texV / texHeight);
                vertices.add(vertex);
                return this;
            }
            
            private MeshFaceBuilder withState(Direction direction, Vector3f faceNormal, 
                    boolean calcNormalFromVertices, boolean invertCalcNormal) {
                this.direction = direction;
                this.faceNormal = faceNormal;
                this.calcNormalFromVertices = calcNormalFromVertices;
                this.invertCalcNormal = invertCalcNormal;
                return this;
            }
            
            public MeshModelBox.Builder createFace() {
                if (vertices.size() > 2) {
                    if (vertices.size() > 3) {
                        ModelRenderer.PositionTextureVertex swap = vertices.get(3);
                        vertices.set(3, vertices.get(2));
                        vertices.set(2, swap);
                    }
                    ModelRenderer.PositionTextureVertex[] verticesDummy = new ModelRenderer.PositionTextureVertex[] {
                            new ModelRenderer.PositionTextureVertex(0, 0, 0, 0, 0),
                            new ModelRenderer.PositionTextureVertex(0, 0, 0, 0, 0),
                            new ModelRenderer.PositionTextureVertex(0, 0, 0, 0, 0),
                            new ModelRenderer.PositionTextureVertex(0, 0, 0, 0, 0)
                    };
                    ModelRenderer.TexturedQuad quad = new ModelRenderer.TexturedQuad(verticesDummy, 
                            0, 0, 0, 0, 1, 1, false, direction != null ? direction : Direction.UP);
                    
                    ModelRenderer.PositionTextureVertex[] verticesArr = vertices.toArray(new ModelRenderer.PositionTextureVertex[4]);
                    if (this.vertices.size() < verticesArr.length) {
                        PositionTextureVertex lastVertex = verticesArr[this.vertices.size() - 1];
                        for (int i = this.vertices.size(); i < verticesArr.length; i++) {
                            verticesArr[i] = lastVertex;
                        }
                    }
                    ClientReflection.setVertices(quad, verticesArr);
                    
                    if (calcNormalFromVertices) {
                        Vector3f pos0 = vertices.get(0).pos.copy();
                        Vector3f vec1 = vertices.get(1).pos.copy();
                        Vector3f vec2 = vertices.get(2).pos.copy();
                        vec1.sub(pos0);
                        vec2.sub(pos0);
                        vec1.cross(vec2);
                        vec1.normalize();
                        if (invertCalcNormal) {
                            vec1.mul(-1);
                        }
                        ClientReflection.setNormal(quad, vec1);
                    }
                    else if (faceNormal != null) {
                        ClientReflection.setNormal(quad, faceNormal);
                    }
                    
                    boxBuilder.quads.add(quad);
                }
                
                vertices.clear();
                return boxBuilder;
            }
        }
    }
}
