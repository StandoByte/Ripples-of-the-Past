package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.mrpresident.CocoJumboTurtleEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CPlayerDiggingPacket;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    public void jojoOnItemSwapKey(CPlayerDiggingPacket packet, CallbackInfo ci) {
        if (packet.getAction() == CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            for (Entity passenger : player.getPassengers()) {
                if (CocoJumboTurtleEntity.isCarriedTurtle(passenger, player)) {
                    passenger.stopRiding();
                    ci.cancel();
                }
            }
        }
    }
}
