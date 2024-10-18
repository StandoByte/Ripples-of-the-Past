package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LifeformsMetMobs;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class GENativeMobsPacket {
    private LifeformsMetMobs serverData;
    private PacketBuffer receivedPacketBuf;
    
    public GENativeMobsPacket(LifeformsMetMobs serverData) {
        this.serverData = serverData;
    }
    
    private GENativeMobsPacket(PacketBuffer toDecode) {
        this.receivedPacketBuf = toDecode;
    }
    
    
    
    public static class Handler implements IModPacketHandler<GENativeMobsPacket> {

        @Override
        public void encode(GENativeMobsPacket msg, PacketBuffer buf) {
            msg.serverData.nativeMobsToBuf(buf);
        }

        @Override
        public GENativeMobsPacket decode(PacketBuffer buf) {
            return new GENativeMobsPacket(buf);
        }

        @Override
        public void handle(GENativeMobsPacket msg, Supplier<Context> ctx) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.getMetMobs().nativeMobsFromBuf(msg.receivedPacketBuf);
            });
        }

        @Override
        public Class<GENativeMobsPacket> getPacketClass() {
            return GENativeMobsPacket.class;
        }
    }
}
