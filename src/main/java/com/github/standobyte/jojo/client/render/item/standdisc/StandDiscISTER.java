package com.github.standobyte.jojo.client.render.item.standdisc;

import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.ModelRenderer.TexturedQuad;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

public class StandDiscISTER extends ItemStackTileEntityRenderer {

    @Override
    public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, 
            MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
        ItemRenderer ir = Minecraft.getInstance().getItemRenderer();
        IBakedModel pModel = ir.getModel(itemStack, null, null);
        
        RenderType rendertype = RenderTypeLookup.getRenderType(itemStack, true);
        IVertexBuilder ivertexbuilder = ItemRenderer.getFoilBufferDirect(
                buffer, rendertype, true, itemStack.hasFoil());
        ir.renderModelLists(pModel, itemStack, light, overlay, matrixStack, ivertexbuilder);
        
        
        
        StandInstance stand = StandDiscItem.getStandFromStack(itemStack, true);
        if (stand != null) {
            renderStandIcon(matrixStack, stand, itemStack, buffer, light, overlay);
        }
    }
    
    
    
    private void renderStandIcon(MatrixStack matrixStack, StandInstance stand, ItemStack discItem, 
            IRenderTypeBuffer buffer, int light, int overlay) {
        ResourceLocation icon = StandSkinsManager.getInstance().getRemappedResPath(
                manager -> manager.getStandSkin(stand), stand.getType().getIconTexture(null));
        
        IVertexBuilder vertexBuilder = ItemRenderer.getFoilBufferDirect(
                buffer, RenderType.entityCutoutNoCull(icon), 
                false, discItem.hasFoil());
        
        renderIconQuad(matrixStack.last(), QUAD_FRONT, vertexBuilder, light, overlay);
        renderIconQuad(matrixStack.last(), QUAD_BACK, vertexBuilder, light, overlay);
    }
    
    static {
        float x0 = 1;
        float y0 = 5;
        float z0 = 7.498F;
        float x1 = 7;
        float y1 = 11;
        float z1 = 8.502F;
        ModelRenderer.PositionTextureVertex vertex7 = new ModelRenderer.PositionTextureVertex(
                x0, y0, z0, 0.0F, 0.0F);
        ModelRenderer.PositionTextureVertex vertex = new ModelRenderer.PositionTextureVertex(
                x1, y0, z0, 0.0F, 8.0F);
        ModelRenderer.PositionTextureVertex vertex1 = new ModelRenderer.PositionTextureVertex(
                x1, y1, z0, 8.0F, 8.0F);
        ModelRenderer.PositionTextureVertex vertex2 = new ModelRenderer.PositionTextureVertex(
                x0, y1, z0, 8.0F, 0.0F);
        ModelRenderer.PositionTextureVertex vertex3 = new ModelRenderer.PositionTextureVertex(
                x0, y0, z1, 0.0F, 0.0F);
        ModelRenderer.PositionTextureVertex vertex4 = new ModelRenderer.PositionTextureVertex(
                x1, y0, z1, 0.0F, 8.0F);
        ModelRenderer.PositionTextureVertex vertex5 = new ModelRenderer.PositionTextureVertex(
                x1, y1, z1, 8.0F, 8.0F);
        ModelRenderer.PositionTextureVertex vertex6 = new ModelRenderer.PositionTextureVertex(
                x0, y1, z1, 8.0F, 0.0F);
        
        QUAD_FRONT = new ModelRenderer.TexturedQuad(
                new ModelRenderer.PositionTextureVertex[]{
                        vertex2, 
                        vertex1,
                        vertex, 
                        vertex7 
                }, 
                0, 0, 16, 16, 
                16, 16, false, Direction.NORTH);
        
        QUAD_BACK = new ModelRenderer.TexturedQuad(
                new ModelRenderer.PositionTextureVertex[]{ 
                        vertex5, 
                        vertex6,
                        vertex3, 
                        vertex4}, 
                0, 0, 16, 16, 
                16, 16, false, Direction.SOUTH);
    }
    
    private static final TexturedQuad QUAD_FRONT;
    private static final TexturedQuad QUAD_BACK;
    
    private void renderIconQuad(MatrixStack.Entry poseEntry, TexturedQuad quad, 
            IVertexBuilder vertexBuilder, int light, int overlay) {
        Matrix4f pose = poseEntry.pose();
        Matrix3f entry = poseEntry.normal();
        Vector3f normal = quad.normal.copy();
        normal.transform(entry);
        float x = normal.x();
        float y = normal.y();
        float z = normal.z();

        for (int i = 0; i < quad.vertices.length; ++i) {
            ModelRenderer.PositionTextureVertex vertex = quad.vertices[i];
            float vertexX = vertex.pos.x() / 16.0F;
            float vertexY = vertex.pos.y() / 16.0F;
            float vertexZ = vertex.pos.z() / 16.0F;
            Vector4f vector4f = new Vector4f(vertexX, vertexY, vertexZ, 1.0F);
            vector4f.transform(pose);
            vertexBuilder.vertex(vector4f.x(), vector4f.y(), vector4f.z(), 
                    1, 1, 1, 1, vertex.u, vertex.v, 
                    overlay, light, x, y, z);
        }
    }
}
