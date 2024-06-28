package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClToggleStandManualControlPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClToggleStandManualControlPacket> {

        @Override
        public void encode(ClToggleStandManualControlPacket msg, PacketBuffer buf) {}

        @Override
        public ClToggleStandManualControlPacket decode(PacketBuffer buf) {
            return new ClToggleStandManualControlPacket();
        }
    
        @Override
        public void handle(ClToggleStandManualControlPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            if (player.isAlive()) {
                IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                    if (power.hasPower()) {
                        if (power.getType().canBeManuallyControlled()) {
                            if (power.isActive()) {
                                StandEntity standEntity = (StandEntity) power.getStandManifestation();
                                ActionConditionResult canControlEntity = standEntity.canBeManuallyControlled();
                                if (canControlEntity.isPositive()) {
                                    boolean keepPosition = player.isShiftKeyDown();
                                    StandUtil.setManualControl(player, !standEntity.isManuallyControlled(), keepPosition);
                                }
                                else if (canControlEntity.getWarning() != null) {
                                    player.displayClientMessage(canControlEntity.getWarning(), true);
                                }
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
        }

        @Override
        public Class<ClToggleStandManualControlPacket> getPacketClass() {
            return ClToggleStandManualControlPacket.class;
        }
    }

}
