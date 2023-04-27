package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.RPSCheat;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRPSGameInputPacket {
    private final PacketType packetType;
    private final Pick playerPick;
    private final PowerClassification cheatPower;
    
    public static ClRPSGameInputPacket pick(Pick playerPick) {
        return new ClRPSGameInputPacket(PacketType.PICK, playerPick, null);
    }
    
    public static ClRPSGameInputPacket cheat(PowerClassification withPower) {
        return new ClRPSGameInputPacket(PacketType.CHEAT, null, withPower);
    }
    
    public static ClRPSGameInputPacket quitGame() {
        return new ClRPSGameInputPacket(PacketType.QUIT, null, null);
    }
    
    private ClRPSGameInputPacket(PacketType packetType, Pick playerPick, PowerClassification cheatPower) {
        this.packetType = packetType;
        this.playerPick = playerPick;
        this.cheatPower = cheatPower;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClRPSGameInputPacket> {

        @Override
        public void encode(ClRPSGameInputPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.packetType);
            switch (msg.packetType) {
            case PICK:
                buf.writeEnum(msg.playerPick);
                break;
            case CHEAT:
                buf.writeEnum(msg.cheatPower);
                break;
            case QUIT:
                break;
            }
        }

        @Override
        public ClRPSGameInputPacket decode(PacketBuffer buf) {
            PacketType packetType = buf.readEnum(PacketType.class);
            switch (packetType) {
            case PICK:
                return pick(buf.readEnum(Pick.class));
            case CHEAT:
                return cheat(buf.readEnum(PowerClassification.class));
            case QUIT:
                return quitGame();
            }
            throw new IllegalStateException();
        }

        @Override
        public void handle(ClRPSGameInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ctx.get().getSender();
            switch (msg.packetType) {
            case PICK:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> game.makeAPick(player, msg.playerPick, false));
                });
                break;
            case CHEAT:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> {
                        RPSCheat cheat = game.getCheat(player, msg.cheatPower);
                        if (cheat != null) {
                            cheat.cheat(game, game.getPlayer(player), player.level);
                            game.getPlayer(player).setCheated();
                        }
                    });
                });
                break;
            case QUIT:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> {
                        game.leaveGame(player);
                    });
                });
                break;
            }
        }

        @Override
        public Class<ClRPSGameInputPacket> getPacketClass() {
            return ClRPSGameInputPacket.class;
        }
    }

    private enum PacketType {
        PICK,
        CHEAT,
        QUIT
    }
}
