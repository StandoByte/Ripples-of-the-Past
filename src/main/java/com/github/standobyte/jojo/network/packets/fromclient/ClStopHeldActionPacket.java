package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
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
    
    
    
    public static class Handler implements IModPacketHandler<ClStopHeldActionPacket> {

        @Override
        public void encode(ClStopHeldActionPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
            buf.writeBoolean(msg.shouldFire);
        }

        @Override
        public ClStopHeldActionPacket decode(PacketBuffer buf) {
            return new ClStopHeldActionPacket(buf.readEnum(PowerClassification.class), buf.readBoolean());
        }

        @Override
        public void handle(ClStopHeldActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                if (power.getHeldAction() != null) {
                    power.stopHeldAction(msg.shouldFire);
                }
            });
        }

        @Override
        public Class<ClStopHeldActionPacket> getPacketClass() {
            return ClStopHeldActionPacket.class;
        }
    }

}
