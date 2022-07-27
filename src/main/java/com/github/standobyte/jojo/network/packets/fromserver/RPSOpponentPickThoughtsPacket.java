package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class RPSOpponentPickThoughtsPacket {
    private final Pick pickThoughts;
    
    public RPSOpponentPickThoughtsPacket(Pick playerPick) {
        this.pickThoughts = playerPick;
    }

    public static void encode(RPSOpponentPickThoughtsPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.pickThoughts);
    }

    public static RPSOpponentPickThoughtsPacket decode(PacketBuffer buf) {
        return new RPSOpponentPickThoughtsPacket(buf.readEnum(Pick.class));
    }

    public static void handle(RPSOpponentPickThoughtsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ClientUtil.getClientPlayer();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.getCurrentRockPaperScissorsGame().ifPresent(game -> game.setOpponentThoughts(player, msg.pickThoughts));
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
