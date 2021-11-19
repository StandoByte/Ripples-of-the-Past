package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncManaRegenPointsPacket {
    private final PowerClassification classification;
    private final float points;
    
    public SyncManaRegenPointsPacket(PowerClassification classification, float points) {
        this.classification = classification;
        this.points = points;
    }
    
    public static void encode(SyncManaRegenPointsPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.classification);
        buf.writeFloat(msg.points);
    }
    
    public static SyncManaRegenPointsPacket decode(PacketBuffer buf) {
        return new SyncManaRegenPointsPacket(buf.readEnum(PowerClassification.class), buf.readFloat());
    }

    public static void handle(SyncManaRegenPointsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                power.setManaRegenPoints(msg.points);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
