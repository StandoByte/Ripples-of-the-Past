package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncCommonConfigPacket {
    private final JojoModConfig.Common.SyncedValues values;
    
    public SyncCommonConfigPacket(JojoModConfig.Common.SyncedValues values) {
        this.values = values;
    }
    
    public static void encode(SyncCommonConfigPacket msg, PacketBuffer buf) {
        msg.values.writeToBuf(buf);
    }
    
    public static SyncCommonConfigPacket decode(PacketBuffer buf) {
        return new SyncCommonConfigPacket(new JojoModConfig.Common.SyncedValues(buf));
    }

    public static void handle(SyncCommonConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            msg.values.changeConfigValues();
        });
        ctx.get().setPacketHandled(true);
    }
}
