package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncStandControlStatusPacket {
    private final boolean manualControl;
    private final boolean keepPosition;
    
    public SyncStandControlStatusPacket(boolean manualControl, boolean keepPosition) {
        this.manualControl = manualControl;
        this.keepPosition = keepPosition;
    }
    
    public static void encode(SyncStandControlStatusPacket msg, PacketBuffer buf) {
        byte flags = 0;
        if (msg.manualControl) {
            flags |= 1;
        }
        if (msg.keepPosition) {
            flags |= 2;
        }
        buf.writeByte(flags);
    }
    
    public static SyncStandControlStatusPacket decode(PacketBuffer buf) {
        byte flags = buf.readByte();
        return new SyncStandControlStatusPacket((flags & 1) > 0, (flags & 2) > 0);
    }

    public static void handle(SyncStandControlStatusPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            StandUtil.setManualControl(ClientUtil.getClientPlayer(), msg.manualControl, msg.keepPosition);
        });
        ctx.get().setPacketHandled(true);
    }
}
