package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class TrDyingBodyTimerPacket {
    private final int entityId;
    private final int timer;
    
    public TrDyingBodyTimerPacket(int entityId, int timer) {
        this.entityId = entityId;
        this.timer = timer;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrDyingBodyTimerPacket> {

        @Override
        public void encode(TrDyingBodyTimerPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeInt(msg.timer);
        }

        @Override
        public TrDyingBodyTimerPacket decode(PacketBuffer buf) {
            return new TrDyingBodyTimerPacket(buf.readInt(), buf.readInt());
        }

        @Override
        public void handle(TrDyingBodyTimerPacket msg, Supplier<Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(
                        cap -> cap.setDyingBodyTimer(msg.timer));
            }
        }

        @Override
        public Class<TrDyingBodyTimerPacket> getPacketClass() {
            return TrDyingBodyTimerPacket.class;
        }
    }
}
