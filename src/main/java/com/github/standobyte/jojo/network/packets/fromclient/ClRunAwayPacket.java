package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRunAwayPacket {
    
    
    
    public static class Handler implements IModPacketHandler<ClRunAwayPacket> {

        @Override
        public void encode(ClRunAwayPacket msg, PacketBuffer buf) {}

        @Override
        public ClRunAwayPacket decode(PacketBuffer buf) {
            return new ClRunAwayPacket();
        }

        @Override
        public void handle(ClRunAwayPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
                if (player.isSprinting()) {
                INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        if (hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())) {
                            JojoModUtil.sayVoiceLine(player, ModSounds.JOSEPH_RUN_AWAY.get());
                        }
                    });
                });
            }
        }

        @Override
        public Class<ClRunAwayPacket> getPacketClass() {
            return ClRunAwayPacket.class;
        }
    }

}
