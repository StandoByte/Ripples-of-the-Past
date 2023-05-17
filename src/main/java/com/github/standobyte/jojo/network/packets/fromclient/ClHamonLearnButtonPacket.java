package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonLearnButtonPacket {
    private final AbstractHamonSkill skill;
    
    public ClHamonLearnButtonPacket(AbstractHamonSkill skill) {
        this.skill = skill;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonLearnButtonPacket> {

        @Override
        public void encode(ClHamonLearnButtonPacket msg, PacketBuffer buf) {
            buf.writeRegistryId(msg.skill);
        }

        @Override
        public ClHamonLearnButtonPacket decode(PacketBuffer buf) {
            return new ClHamonLearnButtonPacket(buf.readRegistryIdSafe(AbstractHamonSkill.class));
        }

        @Override
        public void handle(ClHamonLearnButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    msg.skill.learnNewSkill(hamon, player);
                });
            });
        }

        @Override
        public Class<ClHamonLearnButtonPacket> getPacketClass() {
            return ClHamonLearnButtonPacket.class;
        }
    }

}
