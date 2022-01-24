package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncResolveLimitPacket {
    private final float resolveLimit;
    private final int noDecayTicks;
    
    public SyncResolveLimitPacket(float resolveLimit, int noDecayTicks) {
        this.resolveLimit = resolveLimit;
        this.noDecayTicks = noDecayTicks;
    }
    
    public static void encode(SyncResolveLimitPacket msg, PacketBuffer buf) {
        buf.writeFloat(msg.resolveLimit);
        buf.writeShort(msg.noDecayTicks);
    }
    
    public static SyncResolveLimitPacket decode(PacketBuffer buf) {
        return new SyncResolveLimitPacket(buf.readFloat(), buf.readShort());
    }

    public static void handle(SyncResolveLimitPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setResolveLimit(msg.resolveLimit, msg.noDecayTicks);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
