package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class CommonConfigPacket {
    private final JojoModConfig.Common.SyncedValues values;
    
    public CommonConfigPacket(JojoModConfig.Common.SyncedValues values) {
        this.values = values;
    }
    
    public static void encode(CommonConfigPacket msg, PacketBuffer buf) {
        msg.values.writeToBuf(buf);
    }
    
    public static CommonConfigPacket decode(PacketBuffer buf) {
        return new CommonConfigPacket(new JojoModConfig.Common.SyncedValues(buf));
    }

    public static void handle(CommonConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            msg.values.changeConfigValues();
        });
        ctx.get().setPacketHandled(true);
    }
}
