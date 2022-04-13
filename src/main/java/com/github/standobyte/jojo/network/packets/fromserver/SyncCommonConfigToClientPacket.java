package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncCommonConfigToClientPacket {
    private final JojoModConfig.Common.SyncedValues values;
    
    public SyncCommonConfigToClientPacket(JojoModConfig.Common.SyncedValues values) {
        this.values = values;
    }
    
    public static void encode(SyncCommonConfigToClientPacket msg, PacketBuffer buf) {
        msg.values.writeToBuf(buf);
    }
    
    public static SyncCommonConfigToClientPacket decode(PacketBuffer buf) {
        return new SyncCommonConfigToClientPacket(JojoModConfig.Common.SyncedValues.readFromBuf(buf));
    }

    public static void handle(SyncCommonConfigToClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // FIXME !!!!!!!!!!!!!!!!!!!! config sync: handle packet
            msg.values.changeValues(JojoModConfig.getCommonConfigInstance());
        });
        ctx.get().setPacketHandled(true);
    }
}
