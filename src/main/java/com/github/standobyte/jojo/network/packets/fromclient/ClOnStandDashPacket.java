package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClOnStandDashPacket {
    
    public ClOnStandDashPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClOnStandDashPacket> {

        @Override
        public void encode(ClOnStandDashPacket msg, PacketBuffer buf) {
        }

        @Override
        public ClOnStandDashPacket decode(PacketBuffer buf) {
            return new ClOnStandDashPacket();
        }
        
        @Override
        public void handle(ClOnStandDashPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                if (power.canLeap()) {
                    power.onDash();
                    player.hasImpulse = true;
                }
            });
        }

        @Override
        public Class<ClOnStandDashPacket> getPacketClass() {
            return ClOnStandDashPacket.class;
        }
    }
}
