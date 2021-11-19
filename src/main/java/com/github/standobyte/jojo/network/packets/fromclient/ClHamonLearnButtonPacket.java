package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonLearnButtonPacket {
    private final HamonSkill skill;
    
    public ClHamonLearnButtonPacket(HamonSkill skill) {
        this.skill = skill;
    }
    
    public static void encode(ClHamonLearnButtonPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.skill);
    }
    
    public static ClHamonLearnButtonPacket decode(PacketBuffer buf) {
        return new ClHamonLearnButtonPacket(buf.readEnum(HamonSkill.class));
    }
    
    public static void handle(ClHamonLearnButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.learnHamonSkill(msg.skill, true);
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
