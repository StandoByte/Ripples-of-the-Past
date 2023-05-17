package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHamonAbandonButtonPacket {
    
    public ClHamonAbandonButtonPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHamonAbandonButtonPacket> {
        
        @Override
        public void encode(ClHamonAbandonButtonPacket msg, PacketBuffer buf) {
            
        }

        @Override
        public ClHamonAbandonButtonPacket decode(PacketBuffer buf) {
            return new ClHamonAbandonButtonPacket();
        }

        @Override
        public void handle(ClHamonAbandonButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            INonStandPower.getNonStandPowerOptional(player).ifPresent(power -> {
                if (power.getType() == ModPowers.HAMON.get()) {
                    power.clear();
                    ModCriteriaTriggers.ABANDON_HAMON.get().trigger(player);
                }
            });
        }

        @Override
        public Class<ClHamonAbandonButtonPacket> getPacketClass() {
            return ClHamonAbandonButtonPacket.class;
        }
    }
    

}
