package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mc.damage.NoKnockbackOnBlocking;

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    public void jojoNoHurtBob(CallbackInfo ci) {
        if (NoKnockbackOnBlocking.clCancelHurtBob) {
            ci.cancel();
        }
    }
    
}
