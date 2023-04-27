package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonStartMeditationPacket {
    
    public ClHamonStartMeditationPacket() {}
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonStartMeditationPacket> {

        @Override
        public void encode(ClHamonStartMeditationPacket msg, PacketBuffer buf) {}

        @Override
        public ClHamonStartMeditationPacket decode(PacketBuffer buf) {
            return new ClHamonStartMeditationPacket();
        }

        @Override
        public void handle(ClHamonStartMeditationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    player.addEffect(new EffectInstance(ModEffects.MEDITATION.get()));
                    hamon.startMeditating(player.position(), player.yHeadRot, player.xRot);
                });
            });
        }

        @Override
        public Class<ClHamonStartMeditationPacket> getPacketClass() {
            return ClHamonStartMeditationPacket.class;
        }
    }

}
