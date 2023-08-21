package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.Set;
import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonTeachersSkillsPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonWindowOpenedPacket {
    
    public ClHamonWindowOpenedPacket() {}
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonWindowOpenedPacket> {

        @Override
        public void encode(ClHamonWindowOpenedPacket msg, PacketBuffer buf) {}

        @Override
        public ClHamonWindowOpenedPacket decode(PacketBuffer buf) {
            return new ClHamonWindowOpenedPacket();
        }

        @Override
        public void handle(ClHamonWindowOpenedPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    PacketManager.sendToClient(HamonExercisesPacket.allData(hamon), player);
                    Set<AbstractHamonSkill> skills = HamonUtil.nearbyTeachersSkills(player);
                    PacketManager.sendToClient(skills == null ? new HamonTeachersSkillsPacket() : 
                        new HamonTeachersSkillsPacket(skills), player);
                });
            });
        }

        @Override
        public Class<ClHamonWindowOpenedPacket> getPacketClass() {
            return ClHamonWindowOpenedPacket.class;
        }
    }

}
