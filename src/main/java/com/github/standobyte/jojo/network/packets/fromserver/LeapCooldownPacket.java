package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class LeapCooldownPacket {
    private final PowerClassification classification;
    private final int cooldown;
    
    public LeapCooldownPacket(PowerClassification classification, int cooldown) {
        this.classification = classification;
        this.cooldown = cooldown;
    }
    
    
    
    public static class Handler implements IModPacketHandler<LeapCooldownPacket> {

        @Override
        public void encode(LeapCooldownPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
            buf.writeInt(msg.cooldown);
        }

        @Override
        public LeapCooldownPacket decode(PacketBuffer buf) {
            return new LeapCooldownPacket(buf.readEnum(PowerClassification.class), buf.readInt());
        }

        @Override
        public void handle(LeapCooldownPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                power.setLeapCooldown(msg.cooldown);
            });
        }

        @Override
        public Class<LeapCooldownPacket> getPacketClass() {
            return LeapCooldownPacket.class;
        }
    }
}
