package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResetSyncedCommonConfigPacket {
    
    public ResetSyncedCommonConfigPacket() {
    }
    
    public static void encode(ResetSyncedCommonConfigPacket msg, PacketBuffer buf) {
    }
    
    public static ResetSyncedCommonConfigPacket decode(PacketBuffer buf) {
        return new ResetSyncedCommonConfigPacket();
    }

    public static void handle(ResetSyncedCommonConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            JojoModConfig.Common.SyncedValues.resetConfig();
        });
        ctx.get().setPacketHandled(true);
    }
}
