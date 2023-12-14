package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;

@Mixin(ClientPlayNetHandler.class)
public class ClientPlayNetHandlerMixin {
    public static boolean blockPacketsToServer = false;

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void jojoCancelVanillaClPacket(IPacket<?> packet, CallbackInfo ci) {
        if (blockPacketsToServer && !(packet instanceof CKeepAlivePacket)) {
            ci.cancel();
        }
    }
    
}
