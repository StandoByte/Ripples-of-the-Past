package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TimeStopPlayerJoinPacket {
    private final Phase phase;
    
    public TimeStopPlayerJoinPacket(Phase phase) {
        this.phase = phase;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TimeStopPlayerJoinPacket> {

        @Override
        public void encode(TimeStopPlayerJoinPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.phase == Phase.PRE);
        }

        @Override
        public TimeStopPlayerJoinPacket decode(PacketBuffer buf) {
            return new TimeStopPlayerJoinPacket(buf.readBoolean() ? Phase.PRE : Phase.POST);
        }

        @Override
        public void handle(TimeStopPlayerJoinPacket msg, Supplier<NetworkEvent.Context> ctx) {
            switch (msg.phase) {
            case PRE:
                TimeStopHandler handler = ClientUtil.getClientWorld().getCapability(WorldUtilCapProvider.CAPABILITY).map(
                        cap -> cap.getTimeStopHandler()).orElseThrow(() -> new IllegalStateException("Time stop handler capability is not present!"));
                handler.reset();
                break;
            case POST:
                TimeStopHandler.stopNewEntityInTime(ClientUtil.getClientPlayer(), ClientUtil.getClientWorld());
                break;
            }
        }

        @Override
        public Class<TimeStopPlayerJoinPacket> getPacketClass() {
            return TimeStopPlayerJoinPacket.class;
        }
    }
    
    public enum Phase {
        PRE,
        POST
    }
}
