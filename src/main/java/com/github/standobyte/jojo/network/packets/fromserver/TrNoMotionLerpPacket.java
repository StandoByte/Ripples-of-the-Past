package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrNoMotionLerpPacket {
    private final int entityId;
    private final int ticks;

    public TrNoMotionLerpPacket(int entityId, int ticks) {
        this.entityId = entityId;
        this.ticks = ticks;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrNoMotionLerpPacket> {

        @Override
        public void encode(TrNoMotionLerpPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeVarInt(msg.ticks);
        }

        @Override
        public TrNoMotionLerpPacket decode(PacketBuffer buf) {
            return new TrNoMotionLerpPacket(buf.readInt(), buf.readVarInt());
        }

        @Override
        public void handle(TrNoMotionLerpPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (!TimeStopHandler.canPlayerSeeInStoppedTime(ClientUtil.getClientPlayer())) {
                Entity entity = ClientUtil.getEntityById(msg.entityId);
                if (entity instanceof LivingEntity) {
                    entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        cap.setNoLerpTicks(msg.ticks);
                    });
                }
            }
        }

        @Override
        public Class<TrNoMotionLerpPacket> getPacketClass() {
            return TrNoMotionLerpPacket.class;
        }
    }

}
