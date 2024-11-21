package com.github.standobyte.jojo.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.render.entity.layerrenderer.IFirstPersonHandLayer;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends LivingRendererMixin<T, M> {
    private final List<IFirstPersonHandLayer> jojoFirstPersonHandLayers = new ArrayList<>();
    
    @Override
    public void jojoOnAddLayer(LayerRenderer<T, M> layer, CallbackInfoReturnable<Boolean> ci) {
        if (layer instanceof IFirstPersonHandLayer) {
            jojoFirstPersonHandLayers.add((IFirstPersonHandLayer) layer);
        }
    }

    @Inject(method = "renderRightHand", at = @At("TAIL"))
    public void jojoOnRenderHand(MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, 
            AbstractClientPlayerEntity player, CallbackInfo ci) {
        for (IFirstPersonHandLayer layer : jojoFirstPersonHandLayers) {
            layer.renderHandFirstPerson(HandSide.RIGHT, matrixStack, buffer, light, player, (PlayerRenderer) (Object) this);
        }
    }

    @Inject(method = "renderLeftHand", at = @At("TAIL"))
    public void jojoOnRenderLeftHand(MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, 
            AbstractClientPlayerEntity player, CallbackInfo ci) {
        for (IFirstPersonHandLayer layer : jojoFirstPersonHandLayers) {
            layer.renderHandFirstPerson(HandSide.LEFT, matrixStack, buffer, light, player, (PlayerRenderer) (Object) this);
        }
    }
    
    
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void jojoHoldItemArmPose(AbstractClientPlayerEntity player, Hand hand, CallbackInfoReturnable<BipedModel.ArmPose> ci) {
        ItemStack item = player.getItemInHand(hand);
        if (!player.swinging && !item.isEmpty() && item.getItem() == ModItems.TOMMY_GUN.get()) {
            ci.setReturnValue(BipedModel.ArmPose.CROSSBOW_HOLD);
        }
    }
    
}
