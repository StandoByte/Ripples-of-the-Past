package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClToggleStandManualControlPacket {

    public static void encode(ClToggleStandManualControlPacket msg, PacketBuffer buf) {}

    public static ClToggleStandManualControlPacket decode(PacketBuffer buf) {
        return new ClToggleStandManualControlPacket();
    }

    public static void handle(ClToggleStandManualControlPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player.isAlive()) {
                IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                    if (power.hasPower()) {
                        if (power.getType().canBeManuallyControlled()) {
                            if (power.isActive()) {
                                StandUtil.setManualControl(player, !((StandEntity) power.getStandManifestation()).isManuallyControlled(), player.isShiftKeyDown());
                            }
                        }
                        else {
                            player.displayClientMessage(new TranslationTextComponent("jojo.chat.message.no_entity_stand"), true);
                        }
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
