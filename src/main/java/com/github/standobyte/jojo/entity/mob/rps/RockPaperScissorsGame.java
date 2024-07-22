package com.github.standobyte.jojo.entity.mob.rps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.stand.effect.BoyIIManStandPartTakenEffect;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModCustomStats;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.RPSGameStatePacket;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.RPSOpponentPickThoughtsPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandEffectsTracker;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class RockPaperScissorsGame {
    private static final int ROUNDS_TO_WIN = 3;
    private static boolean cheatInitialized = false;
    private static Map<PowerClassification, Map<IPowerType<?, ?>, RPSCheat>> CHEATS;

    public final RockPaperScissorsPlayerData player1;
    public final RockPaperScissorsPlayerData player2;
    private int round = 1;
    private RockPaperScissorsPlayerData lastRoundWinner = null;
    private RockPaperScissorsPlayerData gameWinner = null;
    private boolean newRound = false;
    private boolean playerLeft = false;

    public RockPaperScissorsGame(LivingEntity player1, LivingEntity player2) {
        this(new RockPaperScissorsPlayerData(player1.getUUID()), new RockPaperScissorsPlayerData(player2.getUUID()));
        this.player1.entity = player1;
        this.player2.entity = player2;
        initCheat();
    }

    private RockPaperScissorsGame(RockPaperScissorsPlayerData player1, RockPaperScissorsPlayerData player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public void gameStarted(ServerWorld serverWorld) {
        playerLeft = false;
        sendToBothPlayers(serverWorld, (player, opponent) -> {
            Entity opponentEntity = opponent.getGamePlayerEntity(serverWorld);
            return opponentEntity != null ? RPSGameStatePacket.enteredGame(opponentEntity.getId(), player1.previousPicks, player2.previousPicks) : null;
        });
    }

    public void setIsPlaying(Entity player, boolean isPlaying) {
        Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> players = playersPair(player);
        if (players == null) return;
        players.getLeft().isReady = isPlaying;
    }

    @Nullable
    public RockPaperScissorsPlayerData getPlayer(Entity playerEntity) {
        Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> players = playersPair(playerEntity);
        return players != null ? players.getLeft() : null;
    }

    public void makeAPick(Entity entity, @Nullable Pick pick, boolean canChange) {
        Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> players = playersPair(entity);
        World world = entity.level;
        if (players == null) return;
        RockPaperScissorsPlayerData player = players.getLeft();
        RockPaperScissorsPlayerData opponent = players.getRight();
        if (!canChange && player.pick != null) return;
        player.pick = pick;
        if (player.pick != null && opponent.pick != null) {
            comparePicks(player, opponent);
            comparePicks(opponent, player);
            
            if (!world.isClientSide()) {
                ((ServerWorld) world).sendParticles(player1.pick.getParticle(), player1.entity.getX(), player1.entity.getY() + player1.entity.getBbHeight(), player1.entity.getZ(), 
                        0, 0, 0, 0, 0);
                ((ServerWorld) world).sendParticles(player2.pick.getParticle(), player2.entity.getX(), player2.entity.getY() + player2.entity.getBbHeight(), player2.entity.getZ(), 
                        0, 0, 0, 0, 0);
            }
            
            player1.pick = null;
            player1.canOpponentReadThoughts = false;
            player1.pickThoughts = null;
            
            player2.pick = null;
            player2.canOpponentReadThoughts = false;
            player2.pickThoughts = null;
            
            onRoundEnd(world);
            if (gameWinner != null) {
                onGameOver(world, gameWinner);
            }
            round++;
            newRound = true;
            lastRoundWinner = null;
        }
        if (player.canOpponentReadThoughts && !world.isClientSide()) {
            this.sendPick((ServerWorld) world, player, true);
        }
    }
    
    public boolean checkNewRound() {
        boolean isItNewRoundYet = newRound;
        this.newRound = false;
        return isItNewRoundYet;
    }

    private void onRoundEnd(World world) {
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            // FIXME (BIIM) show ties in the ui
            this.sendToBothPlayers(serverWorld, (player, opponent) -> RPSGameStatePacket.stateUpdated(player.previousPicks, opponent.previousPicks));
            if (lastRoundWinner != null) {
                roundWon(serverWorld, lastRoundWinner, getOpponent(lastRoundWinner));
            }
            else {
                roundTie();
            }
            
            Vector3d pos = player1.entity.position().scale(0.5).add(player2.entity.position().scale(0.5));
            world.playSound(null, pos.x, pos.y, pos.z, 
                    SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundCategory.AMBIENT, 1.0F, 2.0F);
        }
    }

    private void roundTie() {}

    private void roundWon(ServerWorld serverWorld, RockPaperScissorsPlayerData roundWinner, RockPaperScissorsPlayerData roundLoser) {
        LivingEntity winnerEntity = roundWinner.getGamePlayerEntity(serverWorld);
        LivingEntity loserEntity = roundLoser.getGamePlayerEntity(serverWorld);
        boy2Man(winnerEntity, loserEntity, roundWinner.getScore());
    }

    public void sendThoughtsToOpponent(Entity entity, Pick pick) {
        if (!entity.level.isClientSide()) {
            Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> players = playersPair(entity);
            if (players == null) return;
            if (!players.getLeft().canOpponentReadThoughts) {
                players.getLeft().pickThoughts = null;
            }
            else {
                players.getLeft().pickThoughts = pick;
                Entity opponent = players.getRight().getGamePlayerEntity(entity.level);
                if (opponent instanceof ServerPlayerEntity) {
                    PacketManager.sendToClient(new RPSOpponentPickThoughtsPacket(pick), (ServerPlayerEntity) opponent);
                }
            }
        }
    }

    public void setOpponentThoughts(Entity entity, Pick pick) {
        if (entity.level.isClientSide()) {
            Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> players = playersPair(entity);
            players.getRight().pickThoughts = pick;
        }
    }

    private void onGameOver(World world, RockPaperScissorsPlayerData winner) {
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            sendToBothPlayers(serverWorld, (player, opponent) -> RPSGameStatePacket.gameOver(player == winner));
            triggerAchievement(player1.getGamePlayerEntity(serverWorld));
            triggerAchievement(player2.getGamePlayerEntity(serverWorld));
            if (winner.entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) winner.entity;
                player.awardStat(ModCustomStats.RPS_WON);
            }
        }
    }
    
    private void triggerAchievement(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            ModCriteriaTriggers.ROCK_PAPER_SCISSORS_GAME.get().trigger((ServerPlayerEntity) entity, this, boyIIManTookStand);
        }
    }

    private boolean boyIIManTookStand = false;
    private void boy2Man(LivingEntity roundWinner, LivingEntity roundLoser, int round) {
        IStandPower winnerStand = IStandPower.getStandPowerOptional(roundWinner).orElse(null);
        IStandPower loserStand = IStandPower.getStandPowerOptional(roundLoser).orElse(null);
        if (winnerStand == null || loserStand == null) return;
        if (loserStand.hasPower() && winnerStand.getType() == ModStandsInit.BOY_II_MAN.get()) {
            // FIXME (BIIM)
            if (round < ROUNDS_TO_WIN) {
                StandPart limbs = round == 1 ? StandPart.ARMS : round == 2 ? StandPart.LEGS : null;
                if (limbs != null) {
                    loserStand.getStandInstance().ifPresent(stand -> {
                        if (stand.hasPart(limbs)) {
                            StandInstance takenParts = new StandInstance(stand.getType());
                            for (StandPart standPart : StandPart.values()) {
                                if (standPart != limbs) {
                                    takenParts.removePart(standPart);
                                }
                            }
                            stand.removePart(limbs);
                            winnerStand.getContinuousEffects().addEffect(new BoyIIManStandPartTakenEffect(takenParts).withTarget(roundLoser));
                        }
                    });
                }
            }
            else if (round == ROUNDS_TO_WIN) {
                // FIXME (BIIM)
                StandInstance mainStandBody = loserStand.putOutStand().get();
                winnerStand.getContinuousEffects().addEffect(new BoyIIManStandPartTakenEffect(mainStandBody).withTarget(roundLoser));
                boyIIManTookStand = true;
            }
        }
        else if (loserStand.getType() == ModStandsInit.BOY_II_MAN.get() && round == ROUNDS_TO_WIN) {
            StandEffectsTracker boyIIManEffects = loserStand.getContinuousEffects();
            boyIIManEffects.getEffects(effect -> effect.effectType == ModStandEffects.BOY_II_MAN_PART_TAKE.get()
                    && roundWinner.is(effect.getTarget())).forEach(effect -> {
                        if (winnerStand.hasPower()) {
                            // FIXME (BIIM)
                            StandInstance winnerStandPartsLeft = winnerStand.getStandInstance().get();
                            StandInstance partsTaken = ((BoyIIManStandPartTakenEffect) effect).getPartsTaken();
                            if (partsTaken.getType() == winnerStand.getType()) {
                                Set<StandPart> partsToReturn = partsTaken.getAllParts();
                                if (partsToReturn.stream().allMatch(part -> !winnerStandPartsLeft.hasPart(part))) {
                                    boyIIManEffects.removeEffect(effect);
                                    // FIXME (BIIM) remove the effects more precisely
                                    if (partsToReturn.contains(StandPart.ARMS)) {
                                        roundWinner.removeEffect(Effects.WEAKNESS);
                                        roundWinner.removeEffect(Effects.DIG_SLOWDOWN);
                                    }
                                    if (partsToReturn.contains(StandPart.LEGS)) {
                                        roundWinner.removeEffect(Effects.MOVEMENT_SLOWDOWN);
                                    }
                                }
                            }
                        }
                        else {
                            // FIXME (BIIM)
                            boyIIManEffects.removeEffect(effect);
                        }
                    });
        }
    }

    private void comparePicks(RockPaperScissorsPlayerData player1, RockPaperScissorsPlayerData player2) {
        if (player1.pick.beats(player2.pick)) {
            lastRoundWinner = player1;
            if (++player1.score == ROUNDS_TO_WIN) {
                gameWinner = player1;
            }
        }
        if (!player1.pick.ties(player2.pick)) {
            player1.previousPicks.add(player1.pick);
        }
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
    
    private void sendPick(ServerWorld world, RockPaperScissorsPlayerData player, boolean toOpponent) {
        if (player.pick != null) {
            Entity entity = (toOpponent ? getOpponent(player) : player).getGamePlayerEntity(world);
            if (entity instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(
                        toOpponent ? 
                                RPSGameStatePacket.setOpponentPick(player.pick, player.getGamePlayerEntity(world).getId())
                                : RPSGameStatePacket.setOwnPick(player.pick), (ServerPlayerEntity) entity);
            }
        }
    }

    private void sendToBothPlayers(ServerWorld world, BiFunction<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData, Object> packet) {
        Entity player1Entity = player1.getGamePlayerEntity(world);
        if (player1Entity instanceof ServerPlayerEntity) {
            Object msg = packet.apply(player1, player2);
            if (msg != null) {
                PacketManager.sendToClient(msg, (ServerPlayerEntity) player1Entity);
            }
        }

        Entity player2Entity = player2.getGamePlayerEntity(world);
        if (player2Entity instanceof ServerPlayerEntity) {
            Object msg = packet.apply(player2, player1);
            if (msg != null) {
                PacketManager.sendToClient(msg, (ServerPlayerEntity) player2Entity);
            }
        }
    }

    @Nullable
    private Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> playersPair(Entity player1Entity) {
        return playersPair(player1Entity.getUUID());
    }

    @Nullable
    private Pair<RockPaperScissorsPlayerData, RockPaperScissorsPlayerData> playersPair(UUID player1Id) {
        if (player1.uuid.equals(player1Id)) {
            return Pair.of(player1, player2);
        }
        else if (player2.uuid.equals(player1Id)) {
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

    public void leaveGame(Entity entity) {
        player1.isReady = false;
        player2.isReady = false;
        player1.pick = null;
        player2.pick = null;
        playerLeft = true;
        if (!entity.level.isClientSide()) {
            ServerWorld world = (ServerWorld) entity.level;
            sendToBothPlayers(world, (player, opponent) -> RPSGameStatePacket.leftGame());
        }
    }

    public boolean playerLeft() {
        return playerLeft;
    }

    private static void initCheat() {
        if (!cheatInitialized) {
            CHEATS = new HashMap<>();
            CHEATS.put(PowerClassification.NON_STAND, Util.make(Maps.newHashMap(), map -> {
                map.put(ModPowers.HAMON.get(), (game, player, world) -> {
                    if (!world.isClientSide()) {
                        ServerWorld serverWorld = (ServerWorld) world;
                        RockPaperScissorsPlayerData opponent = game.getOpponent(player);
                        game.sendPick(serverWorld, opponent, true);
                        
                        opponent.canOpponentReadThoughts = true;
                        Entity playerEntity = player.getGamePlayerEntity(serverWorld);
                        Entity opponentEntity = opponent.getGamePlayerEntity(serverWorld);
                        if (opponentEntity instanceof ServerPlayerEntity) {
                            PacketManager.sendToClient(RPSGameStatePacket.mindRead(playerEntity.getId()), (ServerPlayerEntity) opponentEntity);
                        }
                        
                        if (!player.hasCheatedBefore) {
                            world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), 
                                    ModSounds.HAMON_CONCENTRATION.get(), playerEntity.getSoundSource(), 1.0F, 1.0F);
                        }
                    }
                });
                map.put(ModPowers.VAMPIRISM.get(), (game, player, world) -> {
                    if (!world.isClientSide()) {
                        ServerWorld serverWorld = (ServerWorld) world;
                        RockPaperScissorsPlayerData opponent = game.getOpponent(player);
                        game.makeAPick(opponent.getGamePlayerEntity(serverWorld), Pick.ROCK, true);
                        game.sendPick(serverWorld, opponent, false);
                        
                        if (!player.hasCheatedBefore) {
                            Entity playerEntity = player.getGamePlayerEntity(serverWorld);
                            world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), 
                                    ModSounds.VAMPIRE_EVIL_ATMOSPHERE.get(), playerEntity.getSoundSource(), 1.0F, 1.0F);
                        }
                    }
                    else {
                        game.getOpponent(player).pick = Pick.ROCK;
                    }
                });
            }));
            CHEATS.put(PowerClassification.STAND, Util.make(Maps.newHashMap(), map -> {
            }));
            cheatInitialized = true;
        }
    }

    @Nullable
    public RPSCheat getCheat(LivingEntity entity, PowerClassification powerClassification) {
        IPower<?, ?> power = IPower.getPowerOptional(entity, powerClassification).orElse(null);
        return power.hasPower() ? getCheat(powerClassification, power.getType()) : null;
    }
    
    public RPSCheat getCheat(PowerClassification powerClassification, IPowerType<?, ?> powerType) {
        return CHEATS.get(powerClassification).get(powerType);
    }

    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("Round", round);
        nbt.put("Player1", player1.writeNBT());
        nbt.put("Player2", player2.writeNBT());
        return nbt;
    }

    @Nullable
    public static RockPaperScissorsGame fromNBT(CompoundNBT nbt) {
        if (!nbt.contains("Player1", MCUtil.getNbtId(CompoundNBT.class))) return null;
        RockPaperScissorsPlayerData player1 = RockPaperScissorsPlayerData.fromNBT(nbt.getCompound("Player1"));
        if (player1 == null) return null;
        
        if (!nbt.contains("Player2", MCUtil.getNbtId(CompoundNBT.class))) return null;
        RockPaperScissorsPlayerData player2 = RockPaperScissorsPlayerData.fromNBT(nbt.getCompound("Player2"));
        if (player2 == null) return null;
        
        RockPaperScissorsGame game = new RockPaperScissorsGame(player1, player2);
        game.round = nbt.getInt("Round");
        return game;
    }

    public static class RockPaperScissorsPlayerData {
        private final UUID uuid;
        private LivingEntity entity = null;
        private boolean isReady = false;
        private int score = 0;
        private List<Pick> previousPicks = new ArrayList<>();
        @Nullable
        private Pick pick = null;
        private boolean hasCheatedBefore = false;

        @Nullable
        private Pick pickThoughts = null;
        public boolean canOpponentReadThoughts = false;

        private RockPaperScissorsPlayerData(@Nonnull UUID uuid) {
            this.uuid = uuid;
        }

        private LivingEntity getGamePlayerEntity(World serverWorld) {
            if (this.entity == null && !serverWorld.isClientSide()) {
                Entity entity = ((ServerWorld) serverWorld).getEntity(uuid);
                if (entity instanceof LivingEntity) {
                    this.entity = (LivingEntity) entity;
                }
            }
            return this.entity;
        }
        
        public UUID getEntityUuid() {
            return uuid;
        }

        public void setIsReady(boolean isPlaying) {
            this.isReady = isPlaying;
        }
        
        public boolean isReady() {
            return isReady;
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

        @Nullable
        public Pick getPickThoughts() {
            return pickThoughts;
        }
        
        public void setCheated() {
            hasCheatedBefore = true;
        }

        private CompoundNBT writeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putUUID("Player", uuid);
            nbt.putByte("Score", (byte) score);
            if (pick != null) {
                nbt.putString("Pick", pick.name());
            }

            CompoundNBT previousPicksNBT = new CompoundNBT();
            int size = previousPicks.size();
            if (size > 0) {
                previousPicksNBT.putInt("Size", size);
                for (int i = 0; i < size; i++) {
                    previousPicksNBT.putString(String.valueOf(i), previousPicks.get(i).name());
                }
                nbt.put("PreviousPicks", previousPicksNBT);
            }

            return nbt;
        }

        @Nullable
        private static RockPaperScissorsPlayerData fromNBT(CompoundNBT nbt) {
            if (!nbt.hasUUID("Player")) return null;
            UUID uuid = nbt.getUUID("Player");
            RockPaperScissorsPlayerData player = new RockPaperScissorsPlayerData(uuid);
            player.score = nbt.getByte("Score");
            player.pick = GeneralUtil.enumValueOfNullable(Pick.class, nbt.getString("Pick"));

            if (nbt.contains("PreviousPicks", MCUtil.getNbtId(CompoundNBT.class))) {
                CompoundNBT previousPicksNBT = nbt.getCompound("PreviousPicks");
                int size = previousPicksNBT.getInt("Size");
                for (int i = 0; i < size; i++) {
                    Pick pick = Pick.valueOf(previousPicksNBT.getString(String.valueOf(i)));
                    if (pick != null) {
                        player.previousPicks.add(pick);
                    }
                }
            }

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
        ROCK {
            @Override
            public boolean beats(Pick opponentPick) {
                return opponentPick == SCISSORS;
            }

            @Override
            public IParticleData getParticle() {
                return ModParticles.RPS_ROCK.get();
            }
        },
        PAPER {
            @Override
            public boolean beats(Pick opponentPick) {
                return opponentPick == ROCK;
            }

            @Override
            public IParticleData getParticle() {
                return ModParticles.RPS_PAPER.get();
            }
        },
        SCISSORS {
            @Override
            public boolean beats(Pick opponentPick) {
                return opponentPick == PAPER;
            }

            @Override
            public IParticleData getParticle() {
                return ModParticles.RPS_SCISSORS.get();
            }
        };

        public abstract boolean beats(Pick opponentPick);
        public abstract IParticleData getParticle();

        public boolean ties(Pick opponentPick) {
            return this == opponentPick;
        }
    }

    @FunctionalInterface
    public static interface RPSCheat {
        void cheat(RockPaperScissorsGame game, RockPaperScissorsPlayerData player, World world);
    }
}
