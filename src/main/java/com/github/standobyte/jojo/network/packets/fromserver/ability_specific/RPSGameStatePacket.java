package com.github.standobyte.jojo.network.packets.fromserver.ability_specific;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class RPSGameStatePacket {
    private final Type packetType;
    
    private List<Pick> playerPicks;
    private List<Pick> opponentPicks;
    
    private int opponentId;
    
    private boolean playerWon;
    
    private Pick pick;
    private boolean opponentPick;
    
    public static RPSGameStatePacket stateUpdated(List<Pick> playerPicks, List<Pick> opponentPicks) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.UPDATE);
        int size = Math.min(playerPicks.size(), opponentPicks.size());
        packet.playerPicks = playerPicks.subList(0, size);
        packet.opponentPicks = opponentPicks.subList(0, size);
        return packet;
    }
    
    public static RPSGameStatePacket enteredGame(int opponentId, List<Pick> playerPicks, List<Pick> opponentPicks) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.ENTER);
        packet.opponentId = opponentId;
        int size = Math.min(playerPicks.size(), opponentPicks.size());
        packet.playerPicks = playerPicks.subList(0, size);
        packet.opponentPicks = opponentPicks.subList(0, size);
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
    
    public static RPSGameStatePacket setOpponentPick(Pick pick, int opponentId) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.SET_PICK);
        packet.opponentPick = true;
        packet.pick = pick;
        packet.opponentId = opponentId;
        return packet;
    }
    
    public static RPSGameStatePacket setOwnPick(Pick pick) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.SET_PICK);
        packet.opponentPick = false;
        packet.pick = pick;
        packet.opponentId = -1;
        return packet;
    }
    
    public static RPSGameStatePacket mindRead(int opponentId) {
        RPSGameStatePacket packet = new RPSGameStatePacket(Type.MIND_READ);
        packet.opponentId = opponentId;
        return packet;
    }
    
    private RPSGameStatePacket(Type packetType) {
        this.packetType = packetType;
    }
    
    
    
    public static class Handler implements IModPacketHandler<RPSGameStatePacket> {
        
        @Override
        public void encode(RPSGameStatePacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.packetType);
            switch (msg.packetType) {
            case UPDATE:
                writePickLists(msg, buf);
                break;
            case ENTER:
                writePickLists(msg, buf);
                buf.writeInt(msg.opponentId);
                break;
            case LEAVE:
                break;
            case GAME_OVER:
                buf.writeBoolean(msg.playerWon);
                break;
            case SET_PICK:
                buf.writeBoolean(msg.opponentPick);
                buf.writeBoolean(msg.pick != null);
                if (msg.pick != null) {
                    buf.writeEnum(msg.pick);
                }
                if (msg.opponentPick) {
                    buf.writeInt(msg.opponentId);
                }
                break;
            case MIND_READ:
                buf.writeInt(msg.opponentId);
                break;
            }
        }
        
        private void writePickLists(RPSGameStatePacket msg, PacketBuffer buf) {
            int size = msg.playerPicks.size();
            buf.writeVarInt(size);
            for (int i = 0; i < size; i++) {
                buf.writeEnum(msg.playerPicks.get(i));
            }
            for (int i = 0; i < size; i++) {
                buf.writeEnum(msg.opponentPicks.get(i));
            }
        }
        
        @Override
        public RPSGameStatePacket decode(PacketBuffer buf) {
            Type type = buf.readEnum(Type.class);
            List<Pick> playerPicks = new ArrayList<>();
            List<Pick> opponentPicks = new ArrayList<>();
            switch (type) {
            case UPDATE:
                readPickLists(playerPicks, opponentPicks, buf);
                return RPSGameStatePacket.stateUpdated(playerPicks, opponentPicks);
            case ENTER:
                readPickLists(playerPicks, opponentPicks, buf);
                return RPSGameStatePacket.enteredGame(buf.readInt(), playerPicks, opponentPicks);
            case LEAVE:
                return RPSGameStatePacket.leftGame();
            case GAME_OVER:
                return RPSGameStatePacket.gameOver(buf.readBoolean());
            case SET_PICK:
                boolean opponentPick = buf.readBoolean();
                Pick pick = buf.readBoolean() ? buf.readEnum(Pick.class) : null;
                return opponentPick ? RPSGameStatePacket.setOpponentPick(pick, buf.readInt()) : RPSGameStatePacket.setOwnPick(pick);
            case MIND_READ:
                return RPSGameStatePacket.mindRead(buf.readInt());
            }
            throw new IllegalStateException();
        }
        
        private void readPickLists(List<Pick> playerPicks, List<Pick> opponentPicks, PacketBuffer buf) {
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                playerPicks.add(buf.readEnum(Pick.class));
            }
            for (int i = 0; i < size; i++) {
                opponentPicks.add(buf.readEnum(Pick.class));
            }
        }
        
        @Override
        public void handle(RPSGameStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
            PlayerEntity player = ClientUtil.getClientPlayer();
            switch (msg.packetType) {
            case UPDATE:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> {
                        game.makeAPick(player, null, true);
                        game.updateState(msg.playerPicks, msg.opponentPicks);
                    });
                });
                break;
            case ENTER:
                Entity opponent = ClientUtil.getEntityById(msg.opponentId);
                if (opponent instanceof LivingEntity) {
                    RockPaperScissorsGame newGame = new RockPaperScissorsGame(player, (LivingEntity) opponent);
                    newGame.updateState(msg.playerPicks, msg.opponentPicks);
                    player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setCurrentRockPaperScissorsGame(newGame));
                    ClientUtil.openRockPaperScissorsScreen(newGame);
                }
                break;
            case GAME_OVER:
            case LEAVE:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> {
                        game.leaveGame(player); 
                        ClientUtil.closeRockPaperScissorsScreen(game);
                    });
                });
                break;
            case SET_PICK:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> {
                        game.makeAPick(msg.opponentPick ? ClientUtil.getEntityById(msg.opponentId) : player, msg.pick, true);
                    });
                });
                break;
            case MIND_READ:
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.getCurrentRockPaperScissorsGame().ifPresent(game -> {
                        game.getPlayer(player).canOpponentReadThoughts = true;
                    });
                });
                break;
            }
        }
        
        @Override
        public Class<RPSGameStatePacket> getPacketClass() {
            return RPSGameStatePacket.class;
        }
    }
    
    private enum Type {
        UPDATE,
        ENTER,
        LEAVE,
        GAME_OVER,
        SET_PICK,
        MIND_READ
    }
}
