package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRPSPickThoughtsPacket {
    @Nullable private final Pick pickThoughts;
    
    public ClRPSPickThoughtsPacket(Pick playerPick) {
        this.pickThoughts = playerPick;
    }

    public static void encode(ClRPSPickThoughtsPacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.pickThoughts != null);
        if (msg.pickThoughts != null) {
            buf.writeEnum(msg.pickThoughts);
        }
    }

    public static ClRPSPickThoughtsPacket decode(PacketBuffer buf) {
        return new ClRPSPickThoughtsPacket(buf.readBoolean() ? buf.readEnum(Pick.class) : null);
    }

    public static void handle(ClRPSPickThoughtsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.getCurrentRockPaperScissorsGame().ifPresent(game -> game.sendThoughtsToOpponent(player, msg.pickThoughts));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
