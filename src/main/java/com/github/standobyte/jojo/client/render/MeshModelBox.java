package com.github.standobyte.jojo.client.render;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.ModelRenderer.PositionTextureVertex;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

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
    
    
    /*
     *            Vanilla cubes:
     * 
     *    u0   u1       u2   u3  u4   u5
     * v0       ┌────────┬────────┐
     *          │   U    │   D    │
     * v1  ┌────┼────────┼────┬───┴────┐ ⎫
     *     │    │        │    │        │ ⎪
     *     │ E  │   N    │ W  │   S    │ ⎬ size.y
     *     │    │        │    │        │ ⎪
     *     │    │        │    │        │ ⎪
     * v2  └────┴────────┴────┴────────┘ ⎭
     *     size.z               size.x
     */
    
    
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
        
        public Builder(boolean livingEntityRenderHacks, Model model) {
            this.livingEntityRenderHacks = livingEntityRenderHacks;
            faceBuilder = new MeshFaceBuilder(this, model.texWidth, model.texHeight);
        }
        
        public MeshFaceBuilder startFace(Direction lightingDir) {
            if (livingEntityRenderHacks) {
                lightingDir = lightingDir.getAxis() == Axis.Z ? lightingDir : lightingDir.getOpposite();
            }
            faceBuilder.direction = lightingDir;
            return faceBuilder;
        }
        
        
        public void addCube(ModelRenderer modelRenderer) {
            MeshModelBox cube = new MeshModelBox(this);
            ClientReflection.addCube(modelRenderer, cube);
        }
        
        
        public class MeshFaceBuilder {
            private final MeshModelBox.Builder boxBuilder;
            private final float texWidth;
            private final float texHeight;
            
            private Direction direction;
            
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
            
            public MeshModelBox.Builder createFace() {
                if (this.vertices.size() > 2) {
                    ModelRenderer.PositionTextureVertex[] verticesDummy = new ModelRenderer.PositionTextureVertex[] {
                            new ModelRenderer.PositionTextureVertex(0, 0, 0, 0, 0),
                            new ModelRenderer.PositionTextureVertex(0, 0, 0, 0, 0),
                            new ModelRenderer.PositionTextureVertex(0, 0, 0, 0, 0),
                            new ModelRenderer.PositionTextureVertex(0, 0, 0, 0, 0)
                    };
                    ModelRenderer.TexturedQuad quad = new ModelRenderer.TexturedQuad(verticesDummy, 0, 0, 0, 0, 1, 1, false, direction);
                    
                    ModelRenderer.PositionTextureVertex[] verticesArr = vertices.toArray(new ModelRenderer.PositionTextureVertex[4]);
                    if (this.vertices.size() < verticesArr.length) {
                        PositionTextureVertex lastVertex = verticesArr[this.vertices.size() - 1];
                        for (int i = this.vertices.size(); i < verticesArr.length; i++) {
                            verticesArr[i] = lastVertex;
                        }
                    }
                    ClientReflection.setVertices(quad, verticesArr);
                    
                    boxBuilder.quads.add(quad);
                }
                
                vertices.clear();
                return boxBuilder;
            }
        }
    }
}
