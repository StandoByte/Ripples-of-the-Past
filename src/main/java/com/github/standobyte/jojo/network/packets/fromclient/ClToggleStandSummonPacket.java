package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClToggleStandSummonPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClToggleStandSummonPacket> {

        @Override
        public void encode(ClToggleStandSummonPacket msg, PacketBuffer buf) {}

        @Override
        public ClToggleStandSummonPacket decode(PacketBuffer buf) {
            return new ClToggleStandSummonPacket();
        }
    
        @Override
        public void handle(ClToggleStandSummonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            if (player.isAlive()) {
                IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                    power.toggleSummon();
                });
            }
        }

        @Override
        public Class<ClToggleStandSummonPacket> getPacketClass() {
            return ClToggleStandSummonPacket.class;
        }
    }

}
