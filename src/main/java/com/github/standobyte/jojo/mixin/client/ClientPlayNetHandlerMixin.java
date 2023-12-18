package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CPlayerPacket;

@Mixin(ClientPlayNetHandler.class)
public class ClientPlayNetHandlerMixin {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void jojoCancelVanillaClPacket(IPacket<?> packet, CallbackInfo ci) {
        if (NetworkUtil.blockPacketsToServer && !(
                packet instanceof CKeepAlivePacket || packet instanceof CPlayerPacket)) {
            ci.cancel();
        }
    }
    
}
