package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClToggleStandSummonPacket {

    public static void encode(ClToggleStandSummonPacket msg, PacketBuffer buf) {}

    public static ClToggleStandSummonPacket decode(PacketBuffer buf) {
        return new ClToggleStandSummonPacket();
    }

    public static void handle(ClToggleStandSummonPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player.isAlive()) {
                IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                    if (power.hasPower()) {
                        power.toggleSummon();
                    }
                    else {
                        player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.no_stand"), true);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
