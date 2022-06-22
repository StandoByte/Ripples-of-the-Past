package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientEventHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResolveEffectStartPacket {
    private final int effectAmplifier;
    
    public ResolveEffectStartPacket(int effectAmplifier) {
        this.effectAmplifier = effectAmplifier;
    }

    public static void encode(ResolveEffectStartPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.effectAmplifier);
    }

    public static ResolveEffectStartPacket decode(PacketBuffer buf) {
        return new ResolveEffectStartPacket(buf.readVarInt());
    }

    public static void handle(ResolveEffectStartPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientEventHandler.getInstance().onResolveEffectStart(msg.effectAmplifier);
        });
        ctx.get().setPacketHandled(true);
    }
}
