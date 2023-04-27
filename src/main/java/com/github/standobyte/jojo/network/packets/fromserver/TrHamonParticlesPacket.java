package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonPowerType;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonParticlesPacket {
    private final int entityId;
    private final float intensity;

    public TrHamonParticlesPacket(int entityId, float intensity) {
        this.entityId = entityId;
        this.intensity = intensity;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonParticlesPacket> {

        @Override
        public void encode(TrHamonParticlesPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeFloat(msg.intensity);
        }

        @Override
        public TrHamonParticlesPacket decode(PacketBuffer buf) {
            return new TrHamonParticlesPacket(buf.readInt(), buf.readFloat());
        }

        @Override
        public void handle(TrHamonParticlesPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                HamonPowerType.createHamonSparkParticlesEmitter(entity, msg.intensity);
            }
        }

        @Override
        public Class<TrHamonParticlesPacket> getPacketClass() {
            return TrHamonParticlesPacket.class;
        }
    }
}
