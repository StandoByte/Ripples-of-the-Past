package com.github.standobyte.jojo.power.stand;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.client.StandControlHandler;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandControlStatusPacket;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class StandUtil {
    
    public static StandType randomStandByTier(int tier, LivingEntity entity, Random random) {
        if (!entity.level.isClientSide()) {
            Collection<StandType> stands = ModStandTypes.Registry.getRegistry().getValues();
    
            Stream<StandType> stream = stands.stream();
            List<StandType> filtered = tier >= 0 ? 
                    stream.filter(stand -> stand.getTier() == tier).collect(Collectors.toList())
                    : stream.collect(Collectors.toList());
            JojoMod.LOGGER.debug(JojoModConfig.COMMON.prioritizeLeastTakenStands.get());
            if (JojoModConfig.COMMON.prioritizeLeastTakenStands.get()) {
                filtered = SaveFileUtilCapProvider.getSaveFileCap((ServerWorld) entity.level).leastTakenStands(filtered);
            }
            if (!filtered.isEmpty()) {
                return filtered.get(random.nextInt(filtered.size()));
            }
        }
        return null;
    }
    
    public static int standTierFromXp(PlayerEntity player) {
        int lvl = player.experienceLevel;
        for (int i = 0; i < 6; i++) {
            if (lvl < tierLowerBorder(i + 1)) {
                return i;
            }
        }
        return 6;
    }
    
    public static final int[] TIER_XP_LEVELS = {0, 1, 10, 20, 30, 40, 55};
    public static int tierLowerBorder(int tier) {
        return TIER_XP_LEVELS[tier];
    }

    public static boolean canGainStand(PlayerEntity player, int playerTier, StandType stand) {
        return player.abilities.instabuild || !JojoModConfig.COMMON.standTiers.get()
                || standTierFromXp(player) >= stand.getTier() || playerTier >= stand.getTier();
    }
    
    public static boolean isPlayerStandUser(PlayerEntity player) {
        return player.getCapability(StandCapProvider.STAND_CAP).map(cap -> cap.hasPower()).orElse(false);
    }
    
    public static void setManualControl(PlayerEntity player, boolean manualControl, boolean keepPosition) {
        IStandPower standPower = IStandPower.getPlayerStandPower(player);
        IStandManifestation stand = standPower.getStandManifestation();
        if (stand instanceof StandEntity) {
            StandEntity standEntity = ((StandEntity) stand);
            if (!standEntity.isArmsOnlyMode()) {
                if (!player.level.isClientSide()) {
                    standEntity.setManualControl(manualControl, keepPosition);
                    PacketManager.sendToClient(new SyncStandControlStatusPacket(manualControl, keepPosition), (ServerPlayerEntity) player);
                }
                else {
                    Minecraft.getInstance().setCameraEntity(manualControl ? standEntity : null);
                    if (manualControl) {
                        StandControlHandler.setStartedControllingStand();
                    }
                }
            }
        }
    }
}
