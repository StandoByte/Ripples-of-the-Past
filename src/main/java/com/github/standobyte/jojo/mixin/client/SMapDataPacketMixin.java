package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.util.mc.CustomTargetIconMap;

import net.minecraft.network.play.server.SMapDataPacket;
import net.minecraft.world.storage.MapDecoration;

@Mixin(SMapDataPacket.class)
public class SMapDataPacketMixin {
    @Shadow private MapDecoration[] decorations;

    @Inject(method = "applyToMap", at = @At("HEAD"))
    public void jojoReplaceMapDecorations(CallbackInfo ci) {
        CustomTargetIconMap.CustomIconMapDecoration.replaceWithCustomIcons(decorations);
    }
}
