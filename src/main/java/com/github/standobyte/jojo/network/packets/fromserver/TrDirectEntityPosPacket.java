package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrDirectEntityPosPacket {
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    
    public TrDirectEntityPosPacket(int entityId, Vector3d pos) {
    	this(entityId, pos.x, pos.y, pos.z);
    }
    
    public TrDirectEntityPosPacket(int entityId, double x, double y, double z) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static void encode(TrDirectEntityPosPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }
    
    public static TrDirectEntityPosPacket decode(PacketBuffer buf) {
        return new TrDirectEntityPosPacket(buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(TrDirectEntityPosPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                entity.setPacketCoordinates(msg.x, msg.y, msg.z);
            	entity.moveTo(msg.x, msg.y, msg.z);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
