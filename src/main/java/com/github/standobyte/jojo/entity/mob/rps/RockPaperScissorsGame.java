package com.github.standobyte.jojo.entity.mob.rps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.RPSGameStatePacket;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class RockPaperScissorsGame {
	public final RockPaperScissorsPlayerData player1;
	public final RockPaperScissorsPlayerData player2;
	private int round = 1;
    private RockPaperScissorsPlayerData lastRoundWinner = null;
	private RockPaperScissorsPlayerData gameWinner = null;
	
	public RockPaperScissorsGame(Entity player1, Entity player2) {
		this(new RockPaperScissorsPlayerData(player1.getUUID()), new RockPaperScissorsPlayerData(player2.getUUID()));
	}
	
	private RockPaperScissorsGame(RockPaperScissorsPlayerData player1, RockPaperScissorsPlayerData player2) {
		this.player1 = player1;
		this.player2 = player2;
	}
	
	public void setIsPlaying(Entity player, boolean isPlaying) {
		Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> players = playersPair(player);
		if (players == null) return;
		players.getLeft().isPlaying = isPlaying;
	}
	
	public void makeAPick(Entity entity, @Nullable Pick pick) {
		Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> players = playersPair(entity);
		if (players == null) return;
		RockPaperScissorsPlayerData player = players.getLeft();
		RockPaperScissorsPlayerData opponent = players.getRight();
		player.pick = pick;
		if (player.pick != null && opponent.pick != null) {
			if (player.pick != opponent.pick) {
				comparePicks(player, opponent);
				comparePicks(opponent, player);
			}
			player.pick = null;
            opponent.pick = null;
            onRoundEnd(entity.level);
            if (gameWinner != null) {
                onGameOver(entity.level, gameWinner, getOpponent(gameWinner));
            }
            round++;
            lastRoundWinner = null;
		}
	}
    
	private void onRoundEnd(World world) {
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            // FIXME (!!) show ties in the ui
            syncGameState(serverWorld);
            if (lastRoundWinner != null) {
                roundWon(serverWorld, lastRoundWinner, getOpponent(lastRoundWinner));
            }
            else {
                roundTie();
            }
        }
	}
	
	private void roundTie() {}
	
	private void roundWon(ServerWorld serverWorld, RockPaperScissorsPlayerData roundWinner, RockPaperScissorsPlayerData roundLoser) {
        LivingEntity winnerEntity = roundWinner.getGamePlayerEntity(serverWorld);
        LivingEntity loserEntity = roundLoser.getGamePlayerEntity(serverWorld);
        boy2Man(winnerEntity, loserEntity, roundWinner.getScore());
	}
	
	private void onGameOver(World world, RockPaperScissorsPlayerData winner, RockPaperScissorsPlayerData loser) {
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            LivingEntity winnerEntity = winner.getGamePlayerEntity(serverWorld);
            LivingEntity loserEntity = loser.getGamePlayerEntity(serverWorld);
            sendGameOverTo(winnerEntity, winnerEntity);
            sendGameOverTo(loserEntity, winnerEntity);
        }
	}
	
	private void boy2Man(LivingEntity roundWinner, LivingEntity roundLoser, int round) {
	    IStandPower winnerStand = IStandPower.getStandPowerOptional(roundWinner).orElse(null);
        IStandPower loserStand = IStandPower.getStandPowerOptional(roundLoser).orElse(null);
        if (winnerStand != null && loserStand != null && winnerStand.getType() == ModStandTypes.BOY_II_MAN.get()) {
            // FIXME !!!!
            if (round == 3 && loserStand.hasPower()) {
                // FIXME !!!!
                loserStand.putOutStand();
            }
        }
	}
    
    private void comparePicks(RockPaperScissorsPlayerData player1, RockPaperScissorsPlayerData player2) {
        if (player1.pick.beats(player2.pick)) {
            lastRoundWinner = player1;
            if (++player1.score == 3) {
                gameWinner = player1;
            }
        }
        if (!player1.pick.ties(player2.pick)) {
            player1.previousPicks.add(player1.pick);
        }
        player1.pick = null;
    }
    
    public void updateState(List<Pick> playerPicks, List<Pick> opponentPicks) {
        player1.previousPicks = playerPicks;
        player2.previousPicks = opponentPicks;
        player1.score = 0;
        player2.score = 0;
        int size = playerPicks.size();
        for (int i = 0; i < size; i++) {
            Pick playerPick = playerPicks.get(i);
            Pick opponentPick = opponentPicks.get(i);
            if (playerPick.beats(opponentPick)) {
                player1.score++;
            }
            if (opponentPick.beats(playerPick)) {
                player2.score++;
            }
        }
    }
	
	private void syncGameState(ServerWorld world) {
	    sendStateUpdateTo(player1, player2, world.getEntity(player1.uuid));
        sendStateUpdateTo(player2, player1, world.getEntity(player2.uuid));
	}
	
	private void sendStateUpdateTo(RockPaperScissorsPlayerData player, RockPaperScissorsPlayerData opponent, Entity entity) {
	    if (entity instanceof ServerPlayerEntity) {
	        PacketManager.sendToClient(RPSGameStatePacket.stateUpdated(player.previousPicks, opponent.previousPicks), (ServerPlayerEntity) entity);
	    }
	}
    
    private void sendGameOverTo(Entity entity, Entity winner) {
        if (entity instanceof ServerPlayerEntity) {
            PacketManager.sendToClient(RPSGameStatePacket.gameOver(entity.is(winner)), (ServerPlayerEntity) entity);
        }
    }
	
	@Nullable
	private Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> playersPair(Entity player1Entity) {
		if (player1.uuid == player1Entity.getUUID()) {
			return Pair.of(player1, player2);
		}
		else if (player2.uuid == player1Entity.getUUID()) {
			return Pair.of(player2, player1);
		}
		else {
			return null;
		}
	}
	
	private RockPaperScissorsPlayerData getOpponent(RockPaperScissorsPlayerData player) {
	    return player == player1 ? player2 : player == player2 ? player1 : null;
	}
	
	public int getRound() {
	    return round;
	}
	
	@Nullable
	public UUID getWinner() {
		return gameWinner != null ? gameWinner.uuid : null;
	}
	
	public boolean isGameOver() {
	    return gameWinner != null;
	}
    
    public void entityLeftGame(Entity entity) {
        
    }

    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("Round", round);
        nbt.put("Player1", player1.writeNBT());
        nbt.put("Player2", player2.writeNBT());
        return nbt;
    }

    public static RockPaperScissorsGame fromNBT(CompoundNBT nbt) {
    	// FIXME (!!) what if a player is missing from nbt?
        RockPaperScissorsGame game = new RockPaperScissorsGame(
    			RockPaperScissorsPlayerData.fromNBT(nbt.getCompound("Player1")), 
				RockPaperScissorsPlayerData.fromNBT(nbt.getCompound("Player2")));
        game.round = nbt.getInt("Round");
        return game;
    }
	
	public static class RockPaperScissorsPlayerData {
		private final UUID uuid;
		private LivingEntity entity = null;
		private boolean isPlaying = false;
		private int score = 0;
		private List<Pick> previousPicks = new ArrayList<>();
		@Nullable
		private Pick pick = null;
		
		private RockPaperScissorsPlayerData(@Nonnull UUID uuid) {
			this.uuid = uuid;
		}
		
		private LivingEntity getGamePlayerEntity(ServerWorld serverWorld) {
		    if (this.entity == null) {
		        Entity entity = serverWorld.getEntity(uuid);
		        if (entity instanceof LivingEntity) {
		            this.entity = (LivingEntity) entity;
		        }
		    }
		    return this.entity;
		}
		
		private void setIsPlaying(boolean isPlaying) {
			this.isPlaying = isPlaying;
		}
		
		public int getScore() {
		    return score;
		}
		
		public List<Pick> getPreviousPicks() {
		    return previousPicks;
		}
		
		@Nullable
		public Pick getCurrentPick() {
		    return pick;
		}

	    private CompoundNBT writeNBT() {
	        CompoundNBT nbt = new CompoundNBT();
	        nbt.putUUID("Player", uuid);
	        nbt.putByte("Score", (byte) score);
	        if (pick != null) {
	        	nbt.putString("Pick", pick.name());
	        }
	        return nbt;
	    }

	    private static RockPaperScissorsPlayerData fromNBT(CompoundNBT nbt) {
	    	UUID uuid = nbt.getUUID("Player");
	    	// FIXME (!!) what if the uuid is null?
	    	RockPaperScissorsPlayerData player = new RockPaperScissorsPlayerData(uuid);
	    	player.score = nbt.getByte("Score");
	    	player.pick = Pick.valueOf(nbt.getString("Pick"));
	    	return player;
	    }
	}
	
	public static class PlayersPair {
		private final UUID player1;
		private final UUID player2;
		
		public PlayersPair(@Nonnull UUID player1, @Nonnull UUID player2) {
			this.player1 = player1;
			this.player2 = player2;
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof PlayersPair) {
				PlayersPair pair = (PlayersPair) object;
				return this.player1.equals(pair.player1) && this.player2.equals(pair.player2)
						|| this.player1.equals(pair.player2) && this.player2.equals(pair.player1);
			}
			return false;
		}
	}
	
	public enum Pick {
		ROCK,
		PAPER,
		SCISSORS;
		
		public boolean beats(Pick opponentPick) {
			switch (this) {
			case ROCK:
				return opponentPick == SCISSORS;
			case PAPER:
				return opponentPick == ROCK;
			case SCISSORS:
				return opponentPick == PAPER;
			default:
				throw new IllegalStateException("Rock-paper-scissors game only supports three shapes");
			}
		}
		
		public boolean ties(Pick opponentPick) {
			return this == opponentPick;
		}
	}
}
