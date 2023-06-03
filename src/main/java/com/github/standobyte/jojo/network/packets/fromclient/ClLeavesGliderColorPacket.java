package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClLeavesGliderColorPacket {
    private final int entityId;
    private final int color;

    public ClLeavesGliderColorPacket(int entityId, int color) {
        this.entityId = entityId;
        this.color = color;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClLeavesGliderColorPacket> {

        @Override
        public void encode(ClLeavesGliderColorPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeInt(msg.color);
        }

        @Override
        public ClLeavesGliderColorPacket decode(PacketBuffer buf) {
            return new ClLeavesGliderColorPacket(buf.readInt(), buf.readInt());
        }

        @Override
        public void handle(ClLeavesGliderColorPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ctx.get().getSender().level.getEntity(msg.entityId);
            if (entity instanceof LeavesGliderEntity) {
                LeavesGliderEntity glider = (LeavesGliderEntity) entity;
                if (glider.getFoliageColor() < 0) {
                    glider.setFoliageColor(msg.color & 0xFFFFFF);
                }
            }
        }

        @Override
        public Class<ClLeavesGliderColorPacket> getPacketClass() {
            return ClLeavesGliderColorPacket.class;
        }
    }
}
