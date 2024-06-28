package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClStopWallClimbPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClStopWallClimbPacket> {

        @Override
        public void encode(ClStopWallClimbPacket msg, PacketBuffer buf) {}

        @Override
        public ClStopWallClimbPacket decode(PacketBuffer buf) {
            return new ClStopWallClimbPacket();
        }
    
        @Override
        public void handle(ClStopWallClimbPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            if (player.isAlive()) {
                player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.stopWallClimbing());
            }
        }

        @Override
        public Class<ClStopWallClimbPacket> getPacketClass() {
            return ClStopWallClimbPacket.class;
        }
    }

}
