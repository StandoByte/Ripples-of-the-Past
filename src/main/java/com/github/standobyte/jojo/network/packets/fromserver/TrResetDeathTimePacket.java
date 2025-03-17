package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrResetDeathTimePacket {
    private final int entityId;
    
    public TrResetDeathTimePacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrResetDeathTimePacket> {

        @Override
        public void encode(TrResetDeathTimePacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public TrResetDeathTimePacket decode(PacketBuffer buf) {
            return new TrResetDeathTimePacket(buf.readInt());
        }

        @Override
        public void handle(TrResetDeathTimePacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).deathTime = 0;
            }
        }

        @Override
        public Class<TrResetDeathTimePacket> getPacketClass() {
            return TrResetDeathTimePacket.class;
        }
    }
}
