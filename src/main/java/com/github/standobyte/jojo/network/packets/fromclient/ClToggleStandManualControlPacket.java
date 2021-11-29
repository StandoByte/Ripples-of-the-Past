package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
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
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                if (power.hasPower()) {
                    if (power.getType() instanceof EntityStandType) {
                        if (power.isActive()) {
                            StandUtil.setManualControl(player, !((StandEntity) power.getStandManifestation()).isManuallyControlled(), player.isShiftKeyDown());
                        }
                    }
                    else {
                        player.sendMessage(new TranslationTextComponent("jojo.chat.message.no_entity_stand"), Util.NIL_UUID);
                    }
                }
                else {
                    player.sendMessage(new TranslationTextComponent("jojo.chat.message.no_stand"), Util.NIL_UUID);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
