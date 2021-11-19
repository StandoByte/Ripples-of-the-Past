package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonStartMeditationPacket {
    
    public ClHamonStartMeditationPacket() {}
    
    public static void encode(ClHamonStartMeditationPacket msg, PacketBuffer buf) {}
    
    public static ClHamonStartMeditationPacket decode(PacketBuffer buf) {
        return new ClHamonStartMeditationPacket();
    }
    
    public static void handle(ClHamonStartMeditationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                    player.addEffect(new EffectInstance(ModEffects.MEDITATION.get()));
                    hamon.startMeditating(player.position(), player.yHeadRot, player.xRot);
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
