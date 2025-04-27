package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.action.stand.GoldExperienceMarkItem;
import com.github.standobyte.jojo.client.particle.custom.FirstPersonHamonAura;
import com.github.standobyte.jojo.client.render.item.InventoryItemHighlight;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void jojoOnItemRender(ItemStack pItemStack, ItemCameraTransforms.TransformType pTransformType, boolean pLeftHand, 
            MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay, IBakedModel pModel, CallbackInfo ci) {
        switch (pTransformType) {
        case FIRST_PERSON_LEFT_HAND:
            render1stPersonHamonAura(pMatrixStack, pBuffer, pItemStack, HandSide.LEFT);
            break;
        case FIRST_PERSON_RIGHT_HAND:
            render1stPersonHamonAura(pMatrixStack, pBuffer, pItemStack, HandSide.RIGHT);
            break;
        default:
            break;
        }
    }
    
    @ModifyVariable(method = "render", at = @At(value = "STORE"))
    public IVertexBuilder changeVertexBuilder(IVertexBuilder vertexBuilder, 
            ItemStack pItemStack, ItemCameraTransforms.TransformType pTransformType, boolean pLeftHand, 
            MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay, IBakedModel pModel) {
        boolean flag1;
        if (pTransformType != ItemCameraTransforms.TransformType.GUI && !pTransformType.firstPerson() && pItemStack.getItem() instanceof BlockItem) {
           Block block = ((BlockItem)pItemStack.getItem()).getBlock();
           flag1 = !(block instanceof BreakableBlock) && !(block instanceof StainedGlassPaneBlock);
        } else {
           flag1 = true;
        }
        RenderType rendertype = RenderTypeLookup.getRenderType(pItemStack, flag1);
        MatrixStack.Entry matrixstack$entry = pMatrixStack.last();
        
        return GoldExperienceMarkItem.ClientStuff.qwe(vertexBuilder, pItemStack, flag1, pBuffer, rendertype, matrixstack$entry);
    }
    
    private static void render1stPersonHamonAura(MatrixStack matrixStack, IRenderTypeBuffer buffer, ItemStack itemStack, HandSide handSide) {
        if (!MCUtil.itemHandFree(itemStack)) {
            matrixStack.pushPose();
            FirstPersonHamonAura.itemMatrixTransform(matrixStack, handSide, itemStack);
            FirstPersonHamonAura.getInstance().renderParticles(matrixStack, buffer, handSide);
            matrixStack.popPose();
        }
    }
    
    @ModifyVariable(method = "render", remap = false, at = @At("HEAD"), argsOnly = true, ordinal = 1)
    public int jojoItemHighlight(int pCombinedOverlay, ItemStack pItemStack, ItemCameraTransforms.TransformType pTransformType, boolean pLeftHand, 
            MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlayArg, IBakedModel pModel) {
        if (!pItemStack.isEmpty()) {
            float partialTick = Minecraft.getInstance().getDeltaFrameTime();
            float overlayAmount = InventoryItemHighlight.getHighlightAmount(pItemStack.getItem(), partialTick);
            if (overlayAmount >= 0) {
                int highlight = OverlayTexture.pack(OverlayTexture.u(overlayAmount), OverlayTexture.v(false));
                return highlight;
            }
        }
        
        return pCombinedOverlay;
    }
    
}
