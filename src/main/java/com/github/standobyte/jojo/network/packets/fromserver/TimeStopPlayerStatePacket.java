package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientTimeStopHandler;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TimeStopPlayerStatePacket {
    private final boolean canSee;
    private final boolean canMove;
    
    public TimeStopPlayerStatePacket(boolean canSee, boolean canMove) {
        this.canSee = canSee;
        this.canMove = canMove;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TimeStopPlayerStatePacket> {

        @Override
        public void encode(TimeStopPlayerStatePacket msg, PacketBuffer buf) {
            byte flags = 0;
            if (msg.canSee) {
                flags |= 1;
                if (msg.canMove) {
                    flags |= 2;
                }
            }
            buf.writeByte(flags);
        }

        @Override
        public TimeStopPlayerStatePacket decode(PacketBuffer buf) {
            byte flags = buf.readByte();
            return new TimeStopPlayerStatePacket((flags & 1) > 0, (flags & 2) > 0);
        }

        @Override
        public void handle(TimeStopPlayerStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientTimeStopHandler.getInstance().setTimeStopClientState(msg.canSee, msg.canMove);
        }

        @Override
        public Class<TimeStopPlayerStatePacket> getPacketClass() {
            return TimeStopPlayerStatePacket.class;
        }
    }
}
