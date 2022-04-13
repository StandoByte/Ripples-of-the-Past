package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncResolveLevelPacket {
    private final int level;
    
    public SyncResolveLevelPacket(int level) {
        this.level = level;
    }
    
    public static void encode(SyncResolveLevelPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.level);
    }
    
    public static SyncResolveLevelPacket decode(PacketBuffer buf) {
        return new SyncResolveLevelPacket(buf.readVarInt());
    }

    public static void handle(SyncResolveLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setResolveLevel(msg.level);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
