package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRunAwayPacket {

    public static void encode(ClRunAwayPacket msg, PacketBuffer buf) {}

    public static ClRunAwayPacket decode(PacketBuffer buf) {
        return new ClRunAwayPacket();
    }

    public static void handle(ClRunAwayPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
                if (player.isSprinting()) {
                INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                    power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                        if (hamon.getTechnique() == Technique.JOSEPH) {
                            JojoModUtil.sayVoiceLine(player, ModSounds.JOSEPH_RUN_AWAY.get());
                        }
                    });
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
