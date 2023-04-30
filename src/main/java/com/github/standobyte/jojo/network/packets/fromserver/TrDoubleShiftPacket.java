package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrDoubleShiftPacket {
    private final int entityId;
    
    public TrDoubleShiftPacket(int entityId) {
        this.entityId = entityId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrDoubleShiftPacket> {

        @Override
        public void encode(TrDoubleShiftPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
        }

        @Override
        public TrDoubleShiftPacket decode(PacketBuffer buf) {
            return new TrDoubleShiftPacket(buf.readInt());
        }

        @Override
        public void handle(TrDoubleShiftPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof PlayerEntity) {
                entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setDoubleShiftPress());
            }
        }

        @Override
        public Class<TrDoubleShiftPacket> getPacketClass() {
            return TrDoubleShiftPacket.class;
        }
    }
}
