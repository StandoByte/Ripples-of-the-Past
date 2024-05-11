package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ClientTimeStopHandler;

import net.minecraft.client.particle.ParticleManager;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void jojoTsParticleCancelTick(CallbackInfo ci) {
        if (ClientTimeStopHandler.isTimeStoppedStatic()) {
            ci.cancel();
        }
    }
    
    @ModifyVariable(method = "renderParticles", remap = false, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float jojoTsParticleChangePartialTick(float partialTick) {
        if (ClientTimeStopHandler.isTimeStoppedStatic()) {
            return 1.0F;
        }
        return partialTick;
    }
    
    // public Particle createParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed)
    // public void add(Particle pEffect)
    
}
