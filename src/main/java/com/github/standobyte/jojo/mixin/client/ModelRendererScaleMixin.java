package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.render.entity.model.animnew.IModelRendererScale;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.model.ModelRenderer;

@Mixin(ModelRenderer.class)
public class ModelRendererScaleMixin implements IModelRendererScale {
    private float jojoAnimXScale = 1;
    private float jojoAnimYScale = 1;
    private float jojoAnimZScale = 1;
    
    @Override
    public void resetScale() {
        jojoAnimXScale = 1;
        jojoAnimYScale = 1;
        jojoAnimZScale = 1;        
    }

    @Override
    public void setScale(float xScale, float yScale, float zScale) {
        this.jojoAnimXScale = xScale;
        this.jojoAnimYScale = yScale;
        this.jojoAnimZScale = zScale;
    }
    
    @Inject(method = "translateAndRotate", at = @At("TAIL"))
    public void jojoScaleModelPart(MatrixStack matrixStack, CallbackInfo ci) {
        if (jojoAnimXScale != 1.0F || jojoAnimYScale != 1.0F || jojoAnimZScale != 1.0F) {
            matrixStack.scale(jojoAnimXScale, jojoAnimYScale, jojoAnimZScale);
        }
    }

}
