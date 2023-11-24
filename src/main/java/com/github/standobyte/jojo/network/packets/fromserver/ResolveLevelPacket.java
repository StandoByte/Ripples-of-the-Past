package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResolveLevelPacket {
    private final int level;
    
    public ResolveLevelPacket(int level) {
        this.level = level;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ResolveLevelPacket> {

        @Override
        public void encode(ResolveLevelPacket msg, PacketBuffer buf) {
            buf.writeVarInt(msg.level);
        }

        @Override
        public ResolveLevelPacket decode(PacketBuffer buf) {
            return new ResolveLevelPacket(buf.readVarInt());
        }

        @Override
        public void handle(ResolveLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setResolveLevel(msg.level);
            });
        }

        @Override
        public Class<ResolveLevelPacket> getPacketClass() {
            return ResolveLevelPacket.class;
        }
    }
}
