package com.github.standobyte.jojo.client.render;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;

@Deprecated
public class SlopeModelBox extends ModelRenderer.ModelBox {

    public SlopeModelBox(int pTexCoordU, int pTexCoordV, 
            float pOriginX, float pOriginY1, float pOriginY2, float pOriginZ, 
            float pDimensionX, float pDimensionY, float pDimensionY2, float pDimensionZ, 
            float pGrowX, float pGrowY, float pGrowZ, 
            boolean pMirror, float pTexWidthScaled, float pTexHeightScaled) {
        super(pTexCoordU, pTexCoordV, 
                pOriginX, pOriginY1, pOriginZ, 
                pDimensionX, (pOriginY2 - pOriginY1) + pDimensionY, pDimensionZ, 
                pGrowX, pGrowY, pGrowZ, 
                pMirror, pTexWidthScaled, pTexHeightScaled);
        
        ModelRenderer.TexturedQuad[] polygons = new ModelRenderer.TexturedQuad[6];
        float x1 = pOriginX + pDimensionX;
        float y11 = pOriginY1 + pDimensionY;
        float y12 = pOriginY2 + pDimensionY2;
        float z1 = pOriginZ + pDimensionZ;
        pOriginX = pOriginX - pGrowX;
        pOriginY1 = pOriginY1 - pGrowY;
        pOriginY2 = pOriginY2 - pGrowY;
        pOriginZ = pOriginZ - pGrowZ;
        x1 += pGrowX;
        y11 += pGrowY;
        y12 += pGrowY;
        z1 = z1 + pGrowZ;
        if (pMirror) {
            float f3 = x1;
            x1 = pOriginX;
            pOriginX = f3;
        }
        
        ModelRenderer.PositionTextureVertex x0y0z0 = new ModelRenderer.PositionTextureVertex(pOriginX, pOriginY1, pOriginZ, 0.0F, 0.0F);
        ModelRenderer.PositionTextureVertex x1y0z0 = new ModelRenderer.PositionTextureVertex(x1, pOriginY1, pOriginZ, 0.0F, 8.0F);
        ModelRenderer.PositionTextureVertex x1y1z0 = new ModelRenderer.PositionTextureVertex(x1, y11, pOriginZ, 8.0F, 8.0F);
        ModelRenderer.PositionTextureVertex x0y1z0 = new ModelRenderer.PositionTextureVertex(pOriginX, y11, pOriginZ, 8.0F, 0.0F);
        ModelRenderer.PositionTextureVertex x0y0z1 = new ModelRenderer.PositionTextureVertex(pOriginX, pOriginY2, z1, 0.0F, 0.0F);
        ModelRenderer.PositionTextureVertex x1y0z1 = new ModelRenderer.PositionTextureVertex(x1, pOriginY2, z1, 0.0F, 8.0F);
        ModelRenderer.PositionTextureVertex x1y1z1 = new ModelRenderer.PositionTextureVertex(x1, y12, z1, 8.0F, 8.0F);
        ModelRenderer.PositionTextureVertex x0y1z1 = new ModelRenderer.PositionTextureVertex(pOriginX, y12, z1, 8.0F, 0.0F);
        float f4 = (float)pTexCoordU;
        float f5 = (float)pTexCoordU + pDimensionZ;
        float f6 = (float)pTexCoordU + pDimensionZ + pDimensionX;
        float f7 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionX;
        float f8 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionZ;
        float f9 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionZ + pDimensionX;
        float f10 = (float)pTexCoordV;
        float f11 = (float)pTexCoordV + pDimensionZ;
        float f12 = (float)pTexCoordV + pDimensionZ + pDimensionY;
        polygons[2] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x1y0z1, 
                x0y0z1, 
                x0y0z0, 
                x1y0z0}, 
                f5, f10, f6, f11, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.DOWN);
        polygons[3] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x1y1z0, 
                x0y1z0, 
                x0y1z1, 
                x1y1z1}, 
                f6, f11, f7, f10, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.UP);
        polygons[1] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x0y0z0, 
                x0y0z1, 
                x0y1z1, 
                x0y1z0}, 
                f4, f11, f5, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.WEST);
        polygons[4] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x1y0z0,
                x0y0z0, 
                x0y1z0, 
                x1y1z0}, 
                f5, f11, f6, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.NORTH);
        polygons[0] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x1y0z1, 
                x1y0z0, 
                x1y1z0, 
                x1y1z1}, 
                f6, f11, f8, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.EAST);
        polygons[5] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{
                x0y0z1, 
                x1y0z1, 
                x1y1z1, 
                x0y1z1}, 
                f8, f11, f9, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.SOUTH);

        ClientReflection.setPolygons(this, polygons);
    }
    
    public static void addSlopeBox(ModelRenderer modelRenderer, 
            int pTexCoordU, int pTexCoordV, 
            float pOriginX, float pOriginY1, float pOriginY2, float pOriginZ, 
            float pDimensionX, float pDimensionY, float pDimensionY2, float pDimensionZ,
            float pGrow, boolean pMirror, float xTexSize, float yTexSize) {
        addSlopeBox(modelRenderer, 
                pTexCoordU, pTexCoordV, 
                pOriginX, pOriginY1, pOriginY2, pOriginZ, 
                pDimensionX, pDimensionY, pDimensionY2, pDimensionZ,
                pGrow, pGrow, pGrow, 
                pMirror, xTexSize, yTexSize);
    }
    
    public static void addSlopeBox(ModelRenderer modelRenderer, 
            int pTexCoordU, int pTexCoordV, 
            float pOriginX, float pOriginY1, float pOriginY2, float pOriginZ, 
            float pDimensionX, float pDimensionY, float pDimensionY2, float pDimensionZ,
            float pGrowX, float pGrowY, float pGrowZ, 
            boolean pMirror, float xTexSize, float yTexSize) {
        ClientReflection.addCube(modelRenderer, new SlopeModelBox(
                pTexCoordU, pTexCoordV, 
                pOriginX, pOriginY1, pOriginY2, pOriginZ, 
                pDimensionX, pDimensionY, pDimensionY2, pDimensionZ,
                pGrowX, pGrowY, pGrowZ, 
                pMirror, xTexSize, yTexSize));
    }
}
