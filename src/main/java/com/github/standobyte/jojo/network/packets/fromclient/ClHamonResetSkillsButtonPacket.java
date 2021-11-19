package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonSkillType;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonResetSkillsButtonPacket {
    private final HamonSkillType type;
    
    public ClHamonResetSkillsButtonPacket(HamonSkillType type) {
        this.type = type;
    }
    
    public static void encode(ClHamonResetSkillsButtonPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.type);
    }
    
    public static ClHamonResetSkillsButtonPacket decode(PacketBuffer buf) {
        return new ClHamonResetSkillsButtonPacket(buf.readEnum(HamonSkillType.class));
    }
    
    public static void handle(ClHamonResetSkillsButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.resetHamonSkills(msg.type);
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
