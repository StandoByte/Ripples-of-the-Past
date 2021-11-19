package com.github.standobyte.jojo.power.stand;

import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.client.StandControlHandler;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandControlStatusPacket;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class StandUtil {
    
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
        return player.abilities.instabuild || standTierFromXp(player) >= stand.getTier() || playerTier >= stand.getTier();
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
