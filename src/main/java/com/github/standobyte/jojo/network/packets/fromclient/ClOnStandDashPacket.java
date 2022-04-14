package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClOnStandDashPacket {
    
    public ClOnStandDashPacket() {
    }
    
    public static void encode(ClOnStandDashPacket msg, PacketBuffer buf) {
    }
    
    public static ClOnStandDashPacket decode(PacketBuffer buf) {
        return new ClOnStandDashPacket();
    }
    
    public static void handle(ClOnStandDashPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                if (power.canLeap()) {
                    power.onDash();
                    player.hasImpulse = true;
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
