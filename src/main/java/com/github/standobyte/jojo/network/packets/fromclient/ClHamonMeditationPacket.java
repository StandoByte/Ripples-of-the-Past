package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonMeditationPacket {
    private final boolean value;
    
    public ClHamonMeditationPacket(boolean value) {
        this.value = value;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonMeditationPacket> {
    
        @Override
        public void encode(ClHamonMeditationPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.value);
        }

        @Override
        public ClHamonMeditationPacket decode(PacketBuffer buf) {
            return new ClHamonMeditationPacket(buf.readBoolean());
        }

        @Override
        public void handle(ClHamonMeditationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            if (player.isOnGround() || !msg.value) {
                INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.setIsMeditating(player, msg.value);
                    });
                });
            }
        }

        @Override
        public Class<ClHamonMeditationPacket> getPacketClass() {
            return ClHamonMeditationPacket.class;
        }
    }

}
