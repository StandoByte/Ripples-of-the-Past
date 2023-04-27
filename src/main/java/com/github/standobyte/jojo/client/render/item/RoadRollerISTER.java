package com.github.standobyte.jojo.client.render.item;

import com.github.standobyte.jojo.client.render.entity.renderer.RoadRollerRenderer;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RoadRollerISTER extends ItemStackTileEntityRenderer {
    private final RoadRollerItemModel roadRollerModel = new RoadRollerItemModel();

    @Override
    public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer renderTypeBuffer, int light, int overlay) {
        Item item = itemStack.getItem();
        if (item == ModItems.ROAD_ROLLER.get()) {
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            IVertexBuilder vertexBuilder = ItemRenderer.getFoilBufferDirect(
                    renderTypeBuffer, roadRollerModel.renderType(RoadRollerRenderer.TEXTURE), false, itemStack.hasFoil());
            roadRollerModel.renderToBuffer(matrixStack, vertexBuilder, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.popPose();
        }
    }
}
