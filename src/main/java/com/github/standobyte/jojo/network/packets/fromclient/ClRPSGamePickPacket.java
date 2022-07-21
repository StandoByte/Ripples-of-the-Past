package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRPSGamePickPacket {
    private final Pick playerPick;
    
    public ClRPSGamePickPacket(Pick playerPick) {
        this.playerPick = playerPick;
    }

    public static void encode(ClRPSGamePickPacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.playerPick);
    }

    public static ClRPSGamePickPacket decode(PacketBuffer buf) {
        return new ClRPSGamePickPacket(buf.readEnum(Pick.class));
    }

    public static void handle(ClRPSGamePickPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.getCurrentRockPaperScissorsGame().ifPresent(game -> game.makeAPick(player, msg.playerPick));
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
