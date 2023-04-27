package com.github.standobyte.jojo.entity.mob.rps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

public class RPSPvpGamesMap {
    private final Map<PlayersPair, RockPaperScissorsGame> pvpGames = new HashMap<>();
    
    public RockPaperScissorsGame addGame(RockPaperScissorsGame game) {
        pvpGames.put(new PlayersPair(game.player1.getEntityUuid(), game.player2.getEntityUuid()), game);
        return game;
    }
    
    @Nullable
    public RockPaperScissorsGame getGameBetween(PlayerEntity player1, PlayerEntity player2) {
        return pvpGames.get(new PlayersPair(player1.getUUID(), player2.getUUID()));
    }
    
    public RockPaperScissorsGame getOrCreateGame(PlayerEntity player1, PlayerEntity player2) {
        RockPaperScissorsGame unfinishedGame = getGameBetween(player1, player2);
        return unfinishedGame != null && !unfinishedGame.isGameOver() ? unfinishedGame : addGame(new RockPaperScissorsGame(player1, player2));
    }
    
    public CompoundNBT save() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT gamesNBT = new ListNBT();
        pvpGames.forEach((players, game) -> gamesNBT.add(game.writeNBT()));
        nbt.put("Games", gamesNBT);
        return nbt;
    }
    
    public void load(CompoundNBT nbt) {
        if (nbt.contains("Games", MCUtil.getNbtId(ListNBT.class))) {
            nbt.getList("Games", MCUtil.getNbtId(CompoundNBT.class)).forEach(gameNBT -> {
                RockPaperScissorsGame game = RockPaperScissorsGame.fromNBT((CompoundNBT) gameNBT);
                if (game != null) {
                    addGame(game);
                }
            });
        }
    }
    
    
    
    private static class PlayersPair {
        private final UUID player1ID;
        private final UUID player2ID;
        
        private PlayersPair(UUID player1ID, UUID player2ID) {
            this.player1ID = player1ID;
            this.player2ID = player2ID;
        }
        
        @Override
        public boolean equals(Object object) {
            if (super.equals(object)) return true;
            if (object instanceof PlayersPair) {
                PlayersPair other = (PlayersPair) object;
                return this.player1ID.equals(other.player1ID) && this.player2ID.equals(other.player2ID)
                        || this.player1ID.equals(other.player2ID) && this.player2ID.equals(other.player1ID);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return player1ID.hashCode() / 2 + player2ID.hashCode() / 2;
        }
    }

}
