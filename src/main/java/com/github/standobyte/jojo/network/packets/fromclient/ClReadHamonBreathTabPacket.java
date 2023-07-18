package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClReadHamonBreathTabPacket {
    
    public ClReadHamonBreathTabPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClReadHamonBreathTabPacket> {
        
        @Override
        public void encode(ClReadHamonBreathTabPacket msg, PacketBuffer buf) {
        }
        
        @Override
        public ClReadHamonBreathTabPacket decode(PacketBuffer buf) {
            return new ClReadHamonBreathTabPacket();
        }
        
        @Override
        public void handle(ClReadHamonBreathTabPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.setSentNotification(OneTimeNotification.HAMON_BREATH_GUIDE, true);
            });
        }
        
        @Override
        public Class<ClReadHamonBreathTabPacket> getPacketClass() {
            return ClReadHamonBreathTabPacket.class;
        }
    }
}
