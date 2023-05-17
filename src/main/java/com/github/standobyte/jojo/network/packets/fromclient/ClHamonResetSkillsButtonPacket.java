package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonResetSkillsButtonPacket {
    private final HamonSkillsTab type;
    
    public ClHamonResetSkillsButtonPacket(HamonSkillsTab type) {
        this.type = type;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonResetSkillsButtonPacket> {

        @Override
        public void encode(ClHamonResetSkillsButtonPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.type);
        }

        @Override
        public ClHamonResetSkillsButtonPacket decode(PacketBuffer buf) {
            return new ClHamonResetSkillsButtonPacket(buf.readEnum(HamonSkillsTab.class));
        }

        @Override
        public void handle(ClHamonResetSkillsButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.resetHamonSkills(player, msg.type);
                });
            });
        }

        @Override
        public Class<ClHamonResetSkillsButtonPacket> getPacketClass() {
            return ClHamonResetSkillsButtonPacket.class;
        }
    }
    
    
    
    public static enum HamonSkillsTab {
        STRENGTH,
        CONTROL,
        TECHNIQUE
    }
}
