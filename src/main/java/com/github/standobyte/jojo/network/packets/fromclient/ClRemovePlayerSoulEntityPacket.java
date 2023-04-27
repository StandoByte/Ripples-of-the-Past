package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.SoulEntity;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRemovePlayerSoulEntityPacket {
    private final int soulEntityId;
    
    public ClRemovePlayerSoulEntityPacket(int soulEntityId) {
        this.soulEntityId = soulEntityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClRemovePlayerSoulEntityPacket> {

        @Override
        public void encode(ClRemovePlayerSoulEntityPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.soulEntityId);
        }

        @Override
        public ClRemovePlayerSoulEntityPacket decode(PacketBuffer buf) {
            return new ClRemovePlayerSoulEntityPacket(buf.readInt());
        }

        @Override
        public void handle(ClRemovePlayerSoulEntityPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ctx.get().getSender().level.getEntity(msg.soulEntityId);
            if (entity instanceof SoulEntity) {
                ((SoulEntity) entity).skipAscension();
            }
        }

        @Override
        public Class<ClRemovePlayerSoulEntityPacket> getPacketClass() {
            return ClRemovePlayerSoulEntityPacket.class;
        }
    }
}
