package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ClientTimeStopHandler;

import net.minecraft.client.renderer.WorldRenderer;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    
    @ModifyVariable(method = "renderSnowAndRain", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float jojoTsWeatherChangePartialTick(float partialTick) {
        if (ClientTimeStopHandler.isTimeStoppedStatic()) {
            return 1.0F;
        }
        return partialTick;
    }
    
    @ModifyVariable(method = "renderClouds", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float jojoTsCloudsChangePartialTick(float partialTick) {
        if (ClientTimeStopHandler.isTimeStoppedStatic()) {
            return 1.0F;
        }
        return partialTick;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void jojoTsWorldRendererCancelTick(CallbackInfo ci) {
        if (ClientTimeStopHandler.isTimeStoppedStatic()) {
            ci.cancel();
        }
    }
    
}
