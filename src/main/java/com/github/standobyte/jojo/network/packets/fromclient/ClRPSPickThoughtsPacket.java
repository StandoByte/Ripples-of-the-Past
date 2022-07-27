package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRPSPickThoughtsPacket {
    private final Pick pickThoughts;
    
    public ClRPSPickThoughtsPacket(Pick playerPick) {
        this.pickThoughts = playerPick;
    }

    public static void encode(ClRPSPickThoughtsPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.pickThoughts);
    }

    public static ClRPSPickThoughtsPacket decode(PacketBuffer buf) {
        return new ClRPSPickThoughtsPacket(buf.readEnum(Pick.class));
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
