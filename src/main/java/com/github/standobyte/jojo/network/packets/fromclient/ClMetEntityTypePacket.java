package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClMetEntityTypePacket {
    private final int entityId;
    
    public ClMetEntityTypePacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClMetEntityTypePacket> {

        @Override
        public void encode(ClMetEntityTypePacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public ClMetEntityTypePacket decode(PacketBuffer buf) {
            return new ClMetEntityTypePacket(buf.readInt());
        }
        
        @Override
        public void handle(ClMetEntityTypePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                Entity entity = player.level.getEntity(msg.entityId);
                if (entity != null && entity.distanceToSqr(player) <= 144) {
                    cap.addMetEntityType(entity.getType());
                }
            });
        }

        @Override
        public Class<ClMetEntityTypePacket> getPacketClass() {
            return ClMetEntityTypePacket.class;
        }
    }
}
