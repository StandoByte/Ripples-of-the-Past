package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResolvePacket {
    private final float resolve;
    private final int noDecayTicks;
    
    public ResolvePacket(float resolve, int noDecayTicks) {
        this.resolve = resolve;
        this.noDecayTicks = noDecayTicks;
    }
    
    public static void encode(ResolvePacket msg, PacketBuffer buf) {
        buf.writeFloat(msg.resolve);
        buf.writeVarInt(msg.noDecayTicks);
    }
    
    public static ResolvePacket decode(PacketBuffer buf) {
        return new ResolvePacket(buf.readFloat(), buf.readVarInt());
    }

    public static void handle(ResolvePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getResolveCounter().setResolveValue(msg.resolve, msg.noDecayTicks);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
