package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncResolveLevelPacket {
    private final int resolveLevel;
    
    public SyncResolveLevelPacket(int resolveLevel) {
        this.resolveLevel = resolveLevel;
    }
    
    public static void encode(SyncResolveLevelPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.resolveLevel);
    }
    
    public static SyncResolveLevelPacket decode(PacketBuffer buf) {
        return new SyncResolveLevelPacket(buf.readInt());
    }

    public static void handle(SyncResolveLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setResolveLevel(msg.resolveLevel);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
