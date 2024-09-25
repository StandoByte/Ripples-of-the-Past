package com.github.standobyte.jojo.mixin.client;

import java.util.Map;
import java.util.Queue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ClientTimeStopHandler;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;

// TODO tick instances of ItemPickupParticle
@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
    @Shadow @Final private Map<IParticleRenderType, Queue<Particle>> particles;
    @Shadow @Final private Queue<Particle> particlesToAdd;

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
    
//    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
//    public void jojoTsParticleCancelAdd(Particle particle, CallbackInfo ci) {
//        if (ClientTimeStopHandler.isTimeStoppedStatic() && (
//                false /* exclude certain particles */
//                )) {
//            ci.cancel();
//        }
//    }
    
    // public Particle createParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed)
    // public void add(Particle pEffect)
    
}
