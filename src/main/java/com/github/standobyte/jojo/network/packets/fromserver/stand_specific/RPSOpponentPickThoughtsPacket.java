package com.github.standobyte.jojo.network.packets.fromserver.stand_specific;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class RPSOpponentPickThoughtsPacket {
    @Nullable private final Pick pickThoughts;
    
    public RPSOpponentPickThoughtsPacket(Pick playerPick) {
        this.pickThoughts = playerPick;
    }

    public static void encode(RPSOpponentPickThoughtsPacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.pickThoughts != null);
        if (msg.pickThoughts != null) {
            buf.writeEnum(msg.pickThoughts);
        }
    }

    public static RPSOpponentPickThoughtsPacket decode(PacketBuffer buf) {
        return new RPSOpponentPickThoughtsPacket(buf.readBoolean() ? buf.readEnum(Pick.class) : null);
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
