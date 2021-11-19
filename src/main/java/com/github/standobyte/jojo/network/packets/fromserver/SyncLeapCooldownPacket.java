package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncLeapCooldownPacket {
    private final PowerClassification classification;
    private final int cooldown;
    
    public SyncLeapCooldownPacket(PowerClassification classification, int cooldown) {
        this.classification = classification;
        this.cooldown = cooldown;
    }
    
    public static void encode(SyncLeapCooldownPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.classification);
        buf.writeInt(msg.cooldown);
    }
    
    public static SyncLeapCooldownPacket decode(PacketBuffer buf) {
        return new SyncLeapCooldownPacket(buf.readEnum(PowerClassification.class), buf.readInt());
    }

    public static void handle(SyncLeapCooldownPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                power.setLeapCooldown(msg.cooldown);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
