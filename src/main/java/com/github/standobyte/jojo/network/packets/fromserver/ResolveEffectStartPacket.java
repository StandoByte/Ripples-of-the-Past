package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResolveEffectStartPacket {
    private final int effectAmplifier;
    
    public ResolveEffectStartPacket(int effectAmplifier) {
        this.effectAmplifier = effectAmplifier;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ResolveEffectStartPacket> {

        @Override
        public void encode(ResolveEffectStartPacket msg, PacketBuffer buf) {
            buf.writeVarInt(msg.effectAmplifier);
        }

        @Override
        public ResolveEffectStartPacket decode(PacketBuffer buf) {
            return new ResolveEffectStartPacket(buf.readVarInt());
        }

        @Override
        public void handle(ResolveEffectStartPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientEventHandler.getInstance().onResolveEffectStart(msg.effectAmplifier);
        }

        @Override
        public Class<ResolveEffectStartPacket> getPacketClass() {
            return ResolveEffectStartPacket.class;
        }
    }
}
