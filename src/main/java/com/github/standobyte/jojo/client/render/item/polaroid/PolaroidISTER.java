package com.github.standobyte.jojo.client.render.item.polaroid;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.polaroid.PolaroidHelper;
import com.github.standobyte.jojo.client.render.item.generic.CustomModelItemISTER;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;

public class PolaroidISTER extends CustomModelItemISTER<PolaroidModel> {
    
    public PolaroidISTER() {
        super(
                new ResourceLocation(JojoMod.MOD_ID, "polaroid"),
                new ResourceLocation(JojoMod.MOD_ID, "textures/item/polaroid.png"),
                ModItems.POLAROID,
                PolaroidModel::new);
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer renderTypeBuffer, int light, int overlay) {
        if (!(entity == ClientUtil.getClientPlayer() && PolaroidHelper.isTakingPhoto())) {
            super.renderByItem(itemStack, transformType, matrixStack, renderTypeBuffer, light, overlay);
        }
    }
    
    @Override
    protected void doRender(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer renderTypeBuffer, int light, int overlay) {
        boolean isHeld = entity != null && (entity.getItemInHand(Hand.MAIN_HAND) == itemStack || entity.getItemInHand(Hand.OFF_HAND) == itemStack);
//        float ticks = Minecraft.getInstance().player.tickCount + ClientUtil.getPartialTick();
        float photoProgress = 0;
        model.setAnim(isHeld, photoProgress);
        model.setRenderPhoto(false);
        super.doRender(itemStack, transformType, matrixStack, renderTypeBuffer, light, overlay);
        if (photoProgress > 0) {
            model.setRenderPhoto(true);
            IVertexBuilder vertexBuilder = ItemRenderer.getFoilBufferDirect(
                    renderTypeBuffer, model.renderType(PolaroidHelper.PHOTO_TEXTURE), false, itemStack.hasFoil());
            model.renderToBuffer(matrixStack, vertexBuilder, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

}
