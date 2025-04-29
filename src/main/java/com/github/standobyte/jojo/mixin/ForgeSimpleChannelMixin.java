package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.network.NetworkUtil;

import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mixin(SimpleChannel.class)
public class ForgeSimpleChannelMixin {

    @Inject(method = "sendToServer", at = @At("HEAD"), cancellable = true, remap = false)
    private void jojoCancelForgeClPacket(Object packet, CallbackInfo ci) {
        if (NetworkUtil.blockPacketsToServer) {
            ci.cancel();
        }
    }

}
