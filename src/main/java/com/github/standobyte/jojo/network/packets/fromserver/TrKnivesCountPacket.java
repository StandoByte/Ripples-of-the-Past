package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrKnivesCountPacket {
    private final int entityId;
    private final int knives;

    public TrKnivesCountPacket(int entityId, int knives) {
        this.entityId = entityId;
        this.knives = knives;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrKnivesCountPacket> {

        @Override
        public void encode(TrKnivesCountPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeVarInt(msg.knives);
        }

        @Override
        public TrKnivesCountPacket decode(PacketBuffer buf) {
            return new TrKnivesCountPacket(buf.readInt(), buf.readVarInt());
        }

        @Override
        public void handle(TrKnivesCountPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setKnives(msg.knives));
            }
        }

        @Override
        public Class<TrKnivesCountPacket> getPacketClass() {
            return TrKnivesCountPacket.class;
        }
    }

}
