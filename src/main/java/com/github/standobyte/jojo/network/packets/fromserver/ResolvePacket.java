package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResolvePacket {
    private final float resolve;
    private final int noDecayTicks;
    
    public ResolvePacket(float resolve, int noDecayTicks) {
        this.resolve = resolve;
        this.noDecayTicks = noDecayTicks;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ResolvePacket> {

        @Override
        public void encode(ResolvePacket msg, PacketBuffer buf) {
            buf.writeFloat(msg.resolve);
            buf.writeVarInt(msg.noDecayTicks);
        }

        @Override
        public ResolvePacket decode(PacketBuffer buf) {
            return new ResolvePacket(buf.readFloat(), buf.readVarInt());
        }

        @Override
        public void handle(ResolvePacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getResolveCounter().setResolveValue(msg.resolve, msg.noDecayTicks);
            });
        }

        @Override
        public Class<ResolvePacket> getPacketClass() {
            return ResolvePacket.class;
        }
    }
}
