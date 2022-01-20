package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClStopHeldActionPacket {
    private final PowerClassification classification;
    private final boolean shouldFire;
    
    public ClStopHeldActionPacket(PowerClassification classification, boolean shouldFire) {
        this.classification = classification;
        this.shouldFire = shouldFire;
    }

    public static void encode(ClStopHeldActionPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.classification);
        buf.writeBoolean(msg.shouldFire);
    }

    public static ClStopHeldActionPacket decode(PacketBuffer buf) {
        return new ClStopHeldActionPacket(buf.readEnum(PowerClassification.class), buf.readBoolean());
    }

    public static void handle(ClStopHeldActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                if (power.getHeldAction() != null) {
                    power.stopHeldAction(msg.shouldFire);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
