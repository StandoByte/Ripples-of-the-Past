package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRPSPickThoughtsPacket {
    @Nullable private final Pick pickThoughts;
    
    public ClRPSPickThoughtsPacket(Pick playerPick) {
        this.pickThoughts = playerPick;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClRPSPickThoughtsPacket> {

        @Override
        public void encode(ClRPSPickThoughtsPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.pickThoughts != null);
            if (msg.pickThoughts != null) {
                buf.writeEnum(msg.pickThoughts);
            }
        }

        @Override
        public ClRPSPickThoughtsPacket decode(PacketBuffer buf) {
            return new ClRPSPickThoughtsPacket(buf.readBoolean() ? buf.readEnum(Pick.class) : null);
        }

        @Override
        public void handle(ClRPSPickThoughtsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.getCurrentRockPaperScissorsGame().ifPresent(game -> game.sendThoughtsToOpponent(player, msg.pickThoughts));
            });
        }

        @Override
        public Class<ClRPSPickThoughtsPacket> getPacketClass() {
            return ClRPSPickThoughtsPacket.class;
        }
    }
}
