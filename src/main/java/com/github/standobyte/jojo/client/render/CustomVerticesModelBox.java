package com.github.standobyte.jojo.client.render;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

@Deprecated
public class CustomVerticesModelBox extends ModelRenderer.ModelBox {
    
    private CustomVerticesModelBox(Builder builder, float texWidth, float texHeight, boolean mirror) {
        super(0, 0, 
                builder.minX, 
                builder.minY, 
                builder.minZ, 
                builder.maxX - builder.minX, 
                builder.maxY - builder.minY, 
                builder.maxZ - builder.minZ, 
                0, 0, 0, 
                mirror, texWidth, texHeight);
        
        ModelRenderer.PositionTextureVertex x0y0z0 = mirror ? builder.vertices[4] : builder.vertices[0];
        ModelRenderer.PositionTextureVertex x0y0z1 = mirror ? builder.vertices[5] : builder.vertices[1];
        ModelRenderer.PositionTextureVertex x0y1z0 = mirror ? builder.vertices[6] : builder.vertices[2];
        ModelRenderer.PositionTextureVertex x0y1z1 = mirror ? builder.vertices[7] : builder.vertices[3];
        ModelRenderer.PositionTextureVertex x1y0z0 = mirror ? builder.vertices[0] : builder.vertices[4];
        ModelRenderer.PositionTextureVertex x1y0z1 = mirror ? builder.vertices[1] : builder.vertices[5];
        ModelRenderer.PositionTextureVertex x1y1z0 = mirror ? builder.vertices[2] : builder.vertices[6];
        ModelRenderer.PositionTextureVertex x1y1z1 = mirror ? builder.vertices[3] : builder.vertices[7];
        
        ModelRenderer.TexturedQuad[] polygons = new ModelRenderer.TexturedQuad[6];
        Builder.UvInfo uv;
        
        uv = builder.uv.get(Direction.DOWN);
        polygons[2] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x1y0z1, 
                x0y0z1,
                x0y0z0, 
                x1y0z0}, 
                uv.u0, uv.v0, uv.u1, uv.v1, texWidth, texHeight, mirror, Direction.DOWN);
        uv = builder.uv.get(Direction.UP);
        polygons[3] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x1y1z0,
                x0y1z0,
                x0y1z1,
                x1y1z1}, 
                uv.u0, uv.v1, uv.u1, uv.v0, texWidth, texHeight, mirror, Direction.UP);
        uv = builder.uv.get(Direction.WEST);
        polygons[1] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x0y0z0, 
                x0y0z1, 
                x0y1z1, 
                x0y1z0}, 
                uv.u0, uv.v0, uv.u1, uv.v1, texWidth, texHeight, mirror, Direction.WEST);
        uv = builder.uv.get(Direction.NORTH);
        polygons[4] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x1y0z0,
                x0y0z0, 
                x0y1z0, 
                x1y1z0}, 
                uv.u0, uv.v0, uv.u1, uv.v1, texWidth, texHeight, mirror, Direction.NORTH);
        uv = builder.uv.get(Direction.EAST);
        polygons[0] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x1y0z1, 
                x1y0z0, 
                x1y1z0, 
                x1y1z1}, 
                uv.u0, uv.v0, uv.u1, uv.v1, texWidth, texHeight, mirror, Direction.EAST);
        uv = builder.uv.get(Direction.SOUTH);
        polygons[5] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x0y0z1, 
                x1y0z1, 
                x1y1z1, 
                x0y1z1}, 
                uv.u0, uv.v0, uv.u1, uv.v1, texWidth, texHeight, mirror, Direction.SOUTH);

        ClientReflection.setPolygons(this, polygons);
    }
    
    
    
    @Deprecated
    public static class Builder {
        private final boolean livingEntityRenderHacks;
        private final ModelRenderer.PositionTextureVertex[] vertices = new ModelRenderer.PositionTextureVertex[8];
        private final Map<Direction, UvInfo> uv = new EnumMap<>(Direction.class);
        private byte init = 0;
        private float minX;
        private float minY;
        private float minZ;
        private float maxX;
        private float maxY;
        private float maxZ;
        
        public Builder(boolean livingEntityRenderHacks) {
            this.livingEntityRenderHacks = livingEntityRenderHacks;
        }
        
        public Builder withVertex(
                boolean xPositive, 
                boolean yPositive, 
                boolean zPositive, 
                float x, float y, float z) {
            if (livingEntityRenderHacks) {
                x = -x;
                y = -y;
                xPositive = !xPositive;
                yPositive = !yPositive;
            }
            int index = (zPositive ? 1 : 0) | (yPositive ? 2 : 0) | (xPositive ? 4 : 0);
            if (init == 0) {
                minX = x;
                minY = y;
                minZ = z;
                maxX = x;
                maxY = y;
                maxZ = z;
            }
            else {
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                minZ = Math.min(minZ, z);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                maxZ = Math.max(maxZ, z);
            }
            init |= (1 << index);
            
            ModelRenderer.PositionTextureVertex vertex = new ModelRenderer.PositionTextureVertex(
                    x, y, z, yPositive ? 8 : 0, xPositive ? 8 : 0);
            vertices[index] = vertex;
            
            return this;
        }
        
        public Builder withUvFace(Direction direction, float u0, float v0, float uWidth, float vHeight) {
            if (livingEntityRenderHacks) {
                direction = direction.getAxis() == Axis.Z ? direction : direction.getOpposite();
            }
            uv.put(direction, new UvInfo(u0, v0, u0 + uWidth, v0 + vHeight));
            return this;
        }
        
        static class UvInfo {
            public final float u0;
            public final float v0;
            public final float u1;
            public final float v1;
            
            private UvInfo(float u0, float v0, float u1, float v1) {
                this.u0 = u0;
                this.v0 = v0;
                this.u1 = u1;
                this.v1 = v1;
            }
        }
        
        public void addCube(ModelRenderer modelRenderer, float texWidth, float texHeight, boolean mirror) {
            if (init != -1) {
                JojoMod.getLogger().error("Not all vertices have been added yet");
                return;
            }
            if (uv.size() < 6) {
                JojoMod.getLogger().error("Not all UV faces have been added yet");
                return;
            }
            CustomVerticesModelBox cube = new CustomVerticesModelBox(this, texWidth, texHeight, mirror);
            ClientReflection.addCube(modelRenderer, cube);
        }
    }
}
