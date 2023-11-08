package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ClientTimeStopHandler;

import net.minecraft.client.particle.ParticleManager;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void jojoTimeStopCancelTick(CallbackInfo ci) {
        if (ClientTimeStopHandler.getInstance().isTimeStopped()) {
            ci.cancel();
        }
    }
}
