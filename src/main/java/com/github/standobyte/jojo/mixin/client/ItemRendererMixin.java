package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.particle.custom.FirstPersonHamonAura;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void onItemRender(ItemStack pItemStack, ItemCameraTransforms.TransformType pTransformType, boolean pLeftHand, 
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
    
    private static void render1stPersonHamonAura(MatrixStack matrixStack, IRenderTypeBuffer buffer, ItemStack itemStack, HandSide handSide) {
        if (FirstPersonHamonAura.auraRendersAtItem(itemStack, handSide)) {
            matrixStack.pushPose();
            FirstPersonHamonAura.itemMatrixTransform(matrixStack, handSide, itemStack);
            FirstPersonHamonAura.getInstance().renderParticles(matrixStack, buffer, handSide);
            matrixStack.popPose();
        }
    }
}
