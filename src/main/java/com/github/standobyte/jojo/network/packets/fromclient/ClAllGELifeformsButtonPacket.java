package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClAllGELifeformsButtonPacket {
    
    public ClAllGELifeformsButtonPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClAllGELifeformsButtonPacket> {
    
        @Override
        public void encode(ClAllGELifeformsButtonPacket msg, PacketBuffer buf) {
        }

        @Override
        public ClAllGELifeformsButtonPacket decode(PacketBuffer buf) {
            return new ClAllGELifeformsButtonPacket();
        }

        @Override
        public void handle(ClAllGELifeformsButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player.abilities.instabuild) {
                GoldExperienceChooseLifeform.unlockAllEntityTypes(player);
            }
        }

        @Override
        public Class<ClAllGELifeformsButtonPacket> getPacketClass() {
            return ClAllGELifeformsButtonPacket.class;
        }
    }
}
