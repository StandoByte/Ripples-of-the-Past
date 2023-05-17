package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandControlStatusPacket {
    private final boolean manualControl;
    private final boolean keepPosition;
    
    public StandControlStatusPacket(boolean manualControl, boolean keepPosition) {
        this.manualControl = manualControl;
        this.keepPosition = keepPosition;
    }
    
    
    
    public static class Handler implements IModPacketHandler<StandControlStatusPacket> {

        @Override
        public void encode(StandControlStatusPacket msg, PacketBuffer buf) {
            byte flags = 0;
            if (msg.manualControl) {
                flags |= 1;
            }
            if (msg.keepPosition) {
                flags |= 2;
            }
            buf.writeByte(flags);
        }

        @Override
        public StandControlStatusPacket decode(PacketBuffer buf) {
            byte flags = buf.readByte();
            return new StandControlStatusPacket((flags & 1) > 0, (flags & 2) > 0);
        }

        @Override
        public void handle(StandControlStatusPacket msg, Supplier<NetworkEvent.Context> ctx) {
            // FIXME when a player dies with the stand summoned, throws "java.lang.IllegalStateException: Player's stand power capability is empty."
            StandUtil.setManualControl(ClientUtil.getClientPlayer(), msg.manualControl, msg.keepPosition);
        }

        @Override
        public Class<StandControlStatusPacket> getPacketClass() {
            return StandControlStatusPacket.class;
        }
    }
}
