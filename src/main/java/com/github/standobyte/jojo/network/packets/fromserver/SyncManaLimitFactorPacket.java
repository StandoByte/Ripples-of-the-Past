package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncManaLimitFactorPacket {
    private final PowerClassification classification;
    private final float factor;

    public SyncManaLimitFactorPacket(PowerClassification classification, float factor) {
        this.classification = classification;
        this.factor = factor;
    }

    public static void encode(SyncManaLimitFactorPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.classification);
        buf.writeFloat(msg.factor);
    }

    public static SyncManaLimitFactorPacket decode(PacketBuffer buf) {
        return new SyncManaLimitFactorPacket(buf.readEnum(PowerClassification.class), buf.readFloat());
    }

    public static void handle(SyncManaLimitFactorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                power.setManaLimitFactor(msg.factor);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
