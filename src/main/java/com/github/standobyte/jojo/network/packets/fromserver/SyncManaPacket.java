package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncManaPacket {
    private final PowerClassification classification;
    private final float mana;
    
    public SyncManaPacket(PowerClassification classification, float mana) {
        this.classification = classification;
        this.mana = mana;
    }
    
    public static void encode(SyncManaPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.classification);
        buf.writeFloat(msg.mana);
    }
    
    public static SyncManaPacket decode(PacketBuffer buf) {
        return new SyncManaPacket(buf.readEnum(PowerClassification.class), buf.readFloat());
    }

    public static void handle(SyncManaPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                power.setMana(msg.mana);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
