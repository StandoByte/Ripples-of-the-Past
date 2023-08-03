package com.github.standobyte.jojo.power.impl.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.StandController;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.StandControlStatusPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class StandUtil {
    public static final int getMaxTier(boolean isClientSide) {
        return Collections.max(getAvailableTiers(JojoModConfig.getCommonConfigInstance(isClientSide)));
    }
    
    public static StandType<?> randomStand(LivingEntity entity, Random random) {
        return randomStandFromTiers(null, entity, random);
    }
    
    public static StandType<?> randomStandFromTiers(@Nullable int[] tiers, LivingEntity entity, Random random) {
        if (!entity.level.isClientSide()) {
            List<StandType<?>> stands = availableStands(tiers, entity.level.isClientSide()).collect(Collectors.toList());

            if (stands.isEmpty()) {
                return null;
            }
            
            if (JojoModConfig.getCommonConfigInstance(false).prioritizeLeastTakenStands.get()) {
                stands = SaveFileUtilCapProvider.getSaveFileCap(((ServerWorld) entity.level).getServer()).leastTakenStands(stands);
            }
            
            if (!stands.isEmpty()) {
                return stands.get(random.nextInt(stands.size()));
            }
        }
        return null;
    }
    
    public static Stream<StandType<?>> availableStands(@Nullable int[] tiers, boolean clientSide) {
        Collection<StandType<?>> stands = JojoCustomRegistries.STANDS.getRegistry().getValues();
        return stands.stream()
                .filter(stand -> (
                        tiers == null ||
                        Arrays.stream(tiers).anyMatch(tier -> tier == stand.getTier()))
                        && !JojoModConfig.getCommonConfigInstance(clientSide).isStandBanned(stand));
    }
    
    public static int[] standTiersFromXp(int playerXpLvl, boolean withConfigBans, boolean isClientSide) {
        List<Integer> tiers = new ArrayList<>();
        int closestLvlBorder = -1;
        for (int i = getMaxTier(isClientSide); i >= 0; i--) {
            int tierLvlBorder = tierLowerBorder(i, isClientSide);
            if (closestLvlBorder == -1 || closestLvlBorder == tierLvlBorder) {
                if (playerXpLvl >= tierLvlBorder
                        && (!withConfigBans || JojoModConfig.getCommonConfigInstance(isClientSide).tierHasUnbannedStands(i))) {
                    closestLvlBorder = tierLvlBorder;
                    tiers.add(i);
                }
            }
            else {
                break;
            }
        }
        return tiers.stream().mapToInt(Integer::intValue).toArray();
    }
    
    public static int arrowPoolNextTier(int startingFrom, boolean isClientSide) {
        for (int i = startingFrom; i <= getMaxTier(isClientSide); i++) {
            if (JojoModConfig.getCommonConfigInstance(isClientSide).tierHasUnbannedStands(i)) {
                return i;
            }
        }
        return -1;
    }
    
    public static int tierLowerBorder(int tier, boolean isClientSide) {
        List<? extends Integer> xpBorders = JojoModConfig.getCommonConfigInstance(isClientSide).standTierXpLevels.get();
        return GeneralUtil.getOrLast(xpBorders, tier).intValue();
    }
    
    public static Set<Integer> getAvailableTiers(JojoModConfig.Common config) {
        return JojoCustomRegistries.STANDS.getRegistry().getValues()
                .stream()
                .filter(stand -> !config.isStandBanned(stand))
                .map(StandType::getTier)
                .collect(Collectors.toSet());
    }
    
    public static boolean isEntityStandUser(LivingEntity entity) {
        return entity.getCapability(StandCapProvider.STAND_CAP).map(cap -> cap.hasPower()).orElse(false);
    }
    
    public static boolean playerCanSeeStands(PlayerEntity player) {
        return isEntityStandUser(player) || player.hasEffect(ModStatusEffects.SPIRIT_VISION.get());
    }
    
    public static boolean playerCanHearStands(PlayerEntity player) {
        return playerCanSeeStands(player);
    }
    
    public static void setManualControl(PlayerEntity player, boolean manualControl, boolean keepPosition) {
        IStandPower standPower = IStandPower.getPlayerStandPower(player);
        IStandManifestation stand = standPower.getStandManifestation();
        if (stand instanceof StandEntity) {
            StandEntity standEntity = ((StandEntity) stand);
            if (!standEntity.isArmsOnlyMode()) {
                if (!player.level.isClientSide()) {
                    standEntity.setManualControl(manualControl, keepPosition);
                    PacketManager.sendToClient(new StandControlStatusPacket(manualControl, keepPosition), (ServerPlayerEntity) player);
                }
                else {
                    Minecraft mc = Minecraft.getInstance();
                    ClientUtil.setCameraEntityPreventShaderSwitch(mc, manualControl ? standEntity : player);
                    if (manualControl) {
                        mc.player.xxa = 0;
                        mc.player.zza = 0;
                        mc.player.setJumping(false);
                        StandController.setStartedControllingStand();
                    }
                }
            }
        }
    }
    
    public static boolean standIgnoresStaminaDebuff(IStandPower power) {
        return power.getUser() == null || power.getUser().hasEffect(ModStatusEffects.RESOLVE.get()) || power.isUserCreative();
    }
    
    public static LivingEntity getStandUser(LivingEntity standOrUser) {
        if (standOrUser instanceof StandEntity) {
            LivingEntity user = ((StandEntity) standOrUser).getUser();
            if (user != null) standOrUser = user;
        }
        return standOrUser;
    }
    
    public static void addResolve(IStandPower stand, LivingEntity target, float points) {
        target = getStandUser(target);
        boolean hitSelf = target != null && stand.getUser() != null && getStandUser(target).is(stand.getUser());
        if (!hitSelf && worthyTarget(target)) {
            for (PowerClassification classification : PowerClassification.values()) {
                points *= IPower.getPowerOptional(target, classification).map(power -> {
                    if (power.hasPower()) {
                        return power.getTargetResolveMultiplier(stand);
                    }
                    return 1F;
                }).orElse(1F);
            }
            if (target.hasEffect(ModStatusEffects.RESOLVE.get())) {
                points *= Math.max(1 / (stand.getResolveRatio() + 0.2F), 1);
            }
            
            stand.getResolveCounter().addResolveOnAttack(points);
        }
    }
    
    public static boolean worthyTarget(Entity target) {
        if (!target.isAlive()) {
            return false;
        }
        if (target.getClassification(false) == EntityClassification.MONSTER || target.getType() == EntityType.PLAYER) {
            return true;
        }
        if (target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) target;
            if (livingEntity instanceof StandEntity) {
                return true;
            }
            if (livingEntity instanceof MobEntity) {
                if (livingEntity instanceof MonsterEntity) {
                    return true;
                }
                MobEntity mobEntity = (MobEntity) livingEntity;
                return mobEntity.isAggressive();
            }
        }
        return false;
    }
    
    public static boolean isFinisherUnlocked(IStandPower power) {
        return power.getResolveLevel() >= 1;
    }
}
