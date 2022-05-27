package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientEventHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TimeStopPlayerStatePacket {
    private final boolean canSee;
    private final boolean canMove;
    
    public TimeStopPlayerStatePacket(boolean canSee, boolean canMove) {
        this.canSee = canSee;
        this.canMove = canMove;
    }
    
    public static void encode(TimeStopPlayerStatePacket msg, PacketBuffer buf) {
        byte flags = 0;
        if (msg.canSee) {
            flags |= 1;
            if (msg.canMove) {
                flags |= 2;
            }
        }
        buf.writeByte(flags);
    }
    
    public static TimeStopPlayerStatePacket decode(PacketBuffer buf) {
        byte flags = buf.readByte();
        return new TimeStopPlayerStatePacket((flags & 1) > 0, (flags & 2) > 0);
    }

    public static void handle(TimeStopPlayerStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientEventHandler.getInstance().setTimeStopClientState(msg.canSee, msg.canMove);
        });
        ctx.get().setPacketHandled(true);
    }
}
