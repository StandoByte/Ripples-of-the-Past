package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResetResolveValuePacket {
    
    public ResetResolveValuePacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ResetResolveValuePacket> {

        @Override
        public void encode(ResetResolveValuePacket msg, PacketBuffer buf) {
        }

        @Override
        public ResetResolveValuePacket decode(PacketBuffer buf) {
            return new ResetResolveValuePacket();
        }

        @Override
        public void handle(ResetResolveValuePacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getResolveCounter().resetResolveValue();
            });
        }

        @Override
        public Class<ResetResolveValuePacket> getPacketClass() {
            return ResetResolveValuePacket.class;
        }
    }
}
