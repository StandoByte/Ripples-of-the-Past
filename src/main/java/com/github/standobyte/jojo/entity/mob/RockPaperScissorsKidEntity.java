package com.github.standobyte.jojo.entity.mob;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.RockPaperScissorsGame.Pick;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.RPSGameStatePacket;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RockPaperScissorsKidEntity extends VillagerEntity {
    // FIXME (!!) also keep pvp games (in the save file cap?)
    // FIXME (!!) nbt
    private Map<UUID, RockPaperScissorsGame> games = new HashMap<>();
    private Set<UUID> lostTo = new HashSet<>();
    @Nullable
    private RockPaperScissorsGame currentGame;

    public RockPaperScissorsKidEntity(World world) {
        this(ModEntityTypes.ROCK_PAPER_SCISSORS_KID.get(), world);
    }

    public RockPaperScissorsKidEntity(EntityType<? extends VillagerEntity> type, World world) {
        super(type, world);
        this.setBaby(true);
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
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseGet(null).setCurrentRockPaperScissorsGame(game);
            if (player instanceof ServerPlayerEntity) {
                PacketManager.sendToClient(RPSGameStatePacket.enteredGame(this.getId()), (ServerPlayerEntity) player);
            }
        }
    }
    
    // FIXME (!!) random pick
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && tickCount % 50 == 0) {
            makeRandomPick();
        }
    }
    
    public void makeRandomPick() {
        if (currentGame != null) {
            Pick pick = currentGame.getRound() == 1 ? Pick.SCISSORS : Pick.values()[random.nextInt(Pick.values().length)];
            currentGame.makeAPick(this, pick);
        }
    }
}
