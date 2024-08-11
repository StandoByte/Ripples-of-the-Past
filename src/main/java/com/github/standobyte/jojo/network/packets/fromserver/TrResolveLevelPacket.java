package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrResolveLevelPacket {
    private final int entityId;
    private final int level;
    
    public TrResolveLevelPacket(int entityId, int level) {
        this.entityId = entityId;
        this.level = level;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrResolveLevelPacket> {

        @Override
        public void encode(TrResolveLevelPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeVarInt(msg.level);
        }

        @Override
        public TrResolveLevelPacket decode(PacketBuffer buf) {
            return new TrResolveLevelPacket(buf.readInt(), buf.readVarInt());
        }

        @Override
        public void handle(TrResolveLevelPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                IStandPower.getStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.setResolveLevel(msg.level);
                });
            }
        }

        @Override
        public Class<TrResolveLevelPacket> getPacketClass() {
            return TrResolveLevelPacket.class;
        }
    }
}
