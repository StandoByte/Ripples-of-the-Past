package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class RPSGameStatePacket {
    private final Type packetType;
    
    private List<Pick> playerPicks;
    private List<Pick> opponentPicks;
    
    private int opponentId;
    
    private boolean playerWon;
    
    private Pick opponentPick;
    
    public static RPSGameStatePacket stateUpdated(List<Pick> playerPicks, List<Pick> opponentPicks) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.UPDATE);
        int size = Math.min(playerPicks.size(), opponentPicks.size());
        packet.playerPicks = playerPicks.subList(0, size);
        packet.opponentPicks = opponentPicks.subList(0, size);
        return packet;
    }
    
    public static RPSGameStatePacket enteredGame(int opponentId) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.ENTER);
        packet.opponentId = opponentId;
        return packet;
    }
    
    public static RPSGameStatePacket leftGame() {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.LEAVE);
        return packet;
    }
    
    public static RPSGameStatePacket gameOver(boolean playerWon) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.GAME_OVER);
        packet.playerWon = playerWon;
        return packet;
    }
    
    public static RPSGameStatePacket cheat(int opponentId, Pick opponentPick) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.CHEAT);
        packet.opponentId = opponentId;
        packet.opponentPick = opponentPick;
        return packet;
    }
    
    private RPSGameStatePacket(Type packetType) {
        this.packetType = packetType;
    }
    
    public static void encode(RPSGameStatePacket msg, PacketBuffer buf) {
        buf.writeEnum(msg.packetType);
        switch (msg.packetType) {
        case UPDATE:
            int size = msg.playerPicks.size();
            buf.writeVarInt(size);
            for (int i = 0; i < size; i++) {
                buf.writeEnum(msg.playerPicks.get(i));
            }
            for (int i = 0; i < size; i++) {
                buf.writeEnum(msg.opponentPicks.get(i));
            }
            break;
        case ENTER:
            buf.writeInt(msg.opponentId);
            break;
        case LEAVE:
            break;
        case GAME_OVER:
            buf.writeBoolean(msg.playerWon);
            break;
        case CHEAT:
            buf.writeInt(msg.opponentId);
            buf.writeEnum(msg.opponentPick);
            break;
        }
    }
    
    public static RPSGameStatePacket decode(PacketBuffer buf) {
        Type type = buf.readEnum(Type.class);
        switch (type) {
        case UPDATE:
            int size = buf.readVarInt();
            List<Pick> playerPicks = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                playerPicks.add(buf.readEnum(Pick.class));
            }
            List<Pick> opponentPicks = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                opponentPicks.add(buf.readEnum(Pick.class));
            }
            return RPSGameStatePacket.stateUpdated(playerPicks, opponentPicks);
        case ENTER:
            return RPSGameStatePacket.enteredGame(buf.readInt());
        case LEAVE:
            return RPSGameStatePacket.leftGame();
        case GAME_OVER:
            return RPSGameStatePacket.gameOver(buf.readBoolean());
        case CHEAT:
            return RPSGameStatePacket.cheat(buf.readInt(), buf.readEnum(Pick.class));
        }
        return null;
    }

    public static void handle(RPSGameStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ClientUtil.getClientPlayer();
            switch (msg.packetType) {
            case UPDATE:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> {
                        game.makeAPick(player, null);
                        game.updateState(msg.playerPicks, msg.opponentPicks);
                    });
                });
                break;
            case ENTER:
                Entity opponent = ClientUtil.getEntityById(msg.opponentId);
                RockPaperScissorsGame newGame = new RockPaperScissorsGame(player, opponent);
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setCurrentRockPaperScissorsGame(newGame));
                ClientUtil.openRockPaperScissorsScreen(newGame, player, opponent);
                break;
            case LEAVE:
                
                break;
            case GAME_OVER:
                
                break;
            case CHEAT:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> {
                        game.makeAPick(ClientUtil.getEntityById(msg.opponentId), msg.opponentPick);
                    });
                });
                break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    private enum Type {
        UPDATE,
        ENTER,
        LEAVE,
        GAME_OVER,
        CHEAT
    }
}
