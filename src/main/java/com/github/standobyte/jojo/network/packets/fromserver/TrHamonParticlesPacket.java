package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
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

    public static void encode(TrHamonParticlesPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.intensity);
    }

    public static TrHamonParticlesPacket decode(PacketBuffer buf) {
        return new TrHamonParticlesPacket(buf.readInt(), buf.readFloat());
    }

    public static void handle(TrHamonParticlesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                HamonPowerType.createHamonSparkParticlesEmitter(entity, msg.intensity);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
