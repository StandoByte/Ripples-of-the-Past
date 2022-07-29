package com.github.standobyte.jojo.entity.mob.rps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.villager.VillagerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class RockPaperScissorsKidEntity extends VillagerEntity implements IMobStandUser {
    private final IStandPower standPower = new StandPower(this);
    // FIXME (!!) also keep pvp games (in the save file cap?)
    private Map<UUID, RockPaperScissorsGame> games = new HashMap<>();
    private Set<UUID> lostTo = new HashSet<>();
    @Nullable
    private RockPaperScissorsGame currentGame;
    @Nullable
    private UUID currentOpponent;

    public RockPaperScissorsKidEntity(World world) {
        this(ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), world);
    }

    public RockPaperScissorsKidEntity(EntityType<? extends VillagerEntity> type, World world) {
        super(type, world);
    }

    public boolean isPlaying() {
        return this.currentGame != null;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (isBaby() || player.isShiftKeyDown()) {
            if (hand == Hand.MAIN_HAND) {
                startRockPaperScissorsGame(player);
            }
            return ActionResultType.sidedSuccess(level.isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    private void startRockPaperScissorsGame(PlayerEntity player) {
        if (!level.isClientSide()) {
            RockPaperScissorsGame game = games.computeIfAbsent(player.getUUID(), 
                    opponentId -> new RockPaperScissorsGame(player, this));
            currentGame = game;
            currentOpponent = player.getUUID();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseGet(null).setCurrentRockPaperScissorsGame(game);
            game.gameStarted((ServerWorld) level);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        // FIXME (!!) random pick
        if (!level.isClientSide() && tickCount % 10 == 0) {
            makeRandomPick();
        }
        if (currentGame != null && (/*currentGame.playerLeft() || */currentGame.isGameOver())) {
            if (currentGame.isGameOver()) {
                games.remove(currentOpponent);
            }
            currentGame = null;
            currentOpponent = null;
        }
    }
    
    public void makeRandomPick() {
        if (currentGame != null) {
            Pick pick = currentGame.getRound() == 1 ? Pick.SCISSORS : Pick.values()[random.nextInt(Pick.values().length)];
            currentGame.makeAPick(this, pick, false);
        }
    }
    
    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, 
            @Nullable ILivingEntityData additionalData, @Nullable CompoundNBT nbt) {
        standPower.givePower(ModStandTypes.BOY_II_MAN.get());
        AgeableEntity.AgeableData ageableData = new AgeableEntity.AgeableData(1);
        ageableData.increaseGroupSizeByOne();
        additionalData = ageableData;
        
        return super.finalizeSpawn(world, difficulty, reason, additionalData, nbt);
    }

    @Override
    public IStandPower getStandPower() {
        return standPower;
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.put("StandPower", standPower.writeNBT());
        
        CompoundNBT lostToMapNBT = new CompoundNBT();
        MutableInt i = new MutableInt(0);
        lostTo.forEach(winner -> lostToMapNBT.putUUID(String.valueOf(i.incrementAndGet()), winner));
        nbt.put("LostTo", lostToMapNBT);

        CompoundNBT unfinishedGamesNBT = new CompoundNBT();
        games.forEach((playerUUID, game) -> unfinishedGamesNBT.put(playerUUID.toString(), game.writeNBT()));
        nbt.put("UnfinishedGames", unfinishedGamesNBT);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("StandPower", JojoModUtil.getNbtId(CompoundNBT.class))) {
            standPower.readNBT(nbt.getCompound("StandPower"));
        }

        if (nbt.contains("LostTo", JojoModUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT lostToMapNBT = nbt.getCompound("LostTo");
            lostToMapNBT.getAllKeys().forEach(key -> {
                if (lostToMapNBT.hasUUID(key)) {
                    lostTo.add(lostToMapNBT.getUUID(key));
                }
            });
        }

        if (nbt.contains("UnfinishedGames", JojoModUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT unfinishedGamesNBT = nbt.getCompound("UnfinishedGames");
            unfinishedGamesNBT.getAllKeys().forEach(key -> {
                try {
                    UUID id = UUID.fromString(key);
                    if (unfinishedGamesNBT.contains(key, JojoModUtil.getNbtId(CompoundNBT.class))) {
                        RockPaperScissorsGame game = RockPaperScissorsGame.fromNBT(unfinishedGamesNBT.getCompound(key));
                        if (game != null) {
                            games.put(id, game);
                        }
                    }
                }
                catch (IllegalArgumentException e) {}
            });
        }
    }
    
    
    
    public static boolean canTurnFromArrow(Entity entity) {
        if (entity.getType() == EntityType.VILLAGER) {
            MobEntity villager = (MobEntity) entity;
            return villager.isBaby() && villager.getRandom().nextDouble() < 0.5;
        }
        return false;
    }
    
    public static void turnFromArrow(Entity entity) {
        World world = entity.level;
        if (!world.isClientSide()) {
            MobEntity villagerKid = (MobEntity) entity;
            if (ForgeEventFactory.canLivingConvert(villagerKid, ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), (timer) -> {})) {
                RockPaperScissorsKidEntity RPSkid = villagerKid.convertTo(ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), true);
                RPSkid.finalizeSpawn(
                        (ServerWorld) world, 
                        world.getCurrentDifficultyAt(RPSkid.blockPosition()), 
                        SpawnReason.CONVERSION, 
                        null, 
                        null);
                RPSkid.setVillagerData(RPSkid.getVillagerData().setType(VillagerType.byBiome(world.getBiomeName(RPSkid.blockPosition()))));
                ForgeEventFactory.onLivingConvert(villagerKid, RPSkid);
            }
        }
    }
}
