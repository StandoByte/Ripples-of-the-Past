package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonPickTechniquePacket {
    private final CharacterHamonTechnique technique;
    
    public ClHamonPickTechniquePacket(CharacterHamonTechnique technique) {
        this.technique = technique;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonPickTechniquePacket> {
    
        @Override
        public void encode(ClHamonPickTechniquePacket msg, PacketBuffer buf) {
            buf.writeRegistryId(msg.technique);
        }

        @Override
        public ClHamonPickTechniquePacket decode(PacketBuffer buf) {
            return new ClHamonPickTechniquePacket(buf.readRegistryIdSafe(CharacterHamonTechnique.class));
        }

        @Override
        public void handle(ClHamonPickTechniquePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.pickHamonTechnique(player, msg.technique);
                });
            });
        }

        @Override
        public Class<ClHamonPickTechniquePacket> getPacketClass() {
            return ClHamonPickTechniquePacket.class;
        }
    }

}
