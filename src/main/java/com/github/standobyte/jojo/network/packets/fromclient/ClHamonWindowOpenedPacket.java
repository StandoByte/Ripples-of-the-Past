package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.EnumSet;
import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.HamonTeachersSkillsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonWindowOpenedPacket {
    
    public ClHamonWindowOpenedPacket() {}
    
    public static void encode(ClHamonWindowOpenedPacket msg, PacketBuffer buf) {}
    
    public static ClHamonWindowOpenedPacket decode(PacketBuffer buf) {
        return new ClHamonWindowOpenedPacket();
    }
    
    public static void handle(ClHamonWindowOpenedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                    PacketManager.sendToClient(new HamonExercisesPacket(hamon), player);
                    EnumSet<HamonSkill> skills = HamonPowerType.nearbyTeachersSkills(player);
                    PacketManager.sendToClient(skills.isEmpty() ? new HamonTeachersSkillsPacket() : new HamonTeachersSkillsPacket(HamonTeachersSkillsPacket.encodeSkills(skills)), player);
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
