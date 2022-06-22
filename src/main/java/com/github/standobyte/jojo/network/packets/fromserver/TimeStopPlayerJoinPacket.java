package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.utils.TimeUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TimeStopPlayerJoinPacket {
	private final Phase phase;
    
    public TimeStopPlayerJoinPacket(Phase phase) {
    	this.phase = phase;
    }
    
    public static void encode(TimeStopPlayerJoinPacket msg, PacketBuffer buf) {
    	buf.writeBoolean(msg.phase == Phase.PRE);
    }
    
    public static TimeStopPlayerJoinPacket decode(PacketBuffer buf) {
        return new TimeStopPlayerJoinPacket(buf.readBoolean() ? Phase.PRE : Phase.POST);
    }

    public static void handle(TimeStopPlayerJoinPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
        	switch (msg.phase) {
        	case PRE:
        		TimeStopHandler handler = ClientUtil.getClientWorld().getCapability(WorldUtilCapProvider.CAPABILITY).map(
        				cap -> cap.getTimeStopHandler()).orElseThrow(() -> new IllegalStateException("Time stop handler capability is not present!"));
        		handler.reset();
        		break;
        	case POST:
        		TimeUtil.stopNewEntityInTime(ClientUtil.getClientPlayer(), ClientUtil.getClientWorld());
        		break;
        	}
        });
        ctx.get().setPacketHandled(true);
    }
    
    public enum Phase {
    	PRE,
    	POST
    }
}
