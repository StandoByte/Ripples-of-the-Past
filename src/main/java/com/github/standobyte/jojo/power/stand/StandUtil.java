package com.github.standobyte.jojo.power.stand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.client.StandController;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.StandControlStatusPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

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
//    public static final int MAX_TIER = 6;
//    public static final int[] TIER_XP_LEVELS = {0, 1, 10, 20, 30, 40, 55};
    public static final int getMaxTier(boolean isClientSide) {
        return Math.max(JojoModConfig.getCommonConfigInstance(isClientSide).standTierXpLevels.get().size() - 1, 6);
    }
    
    public static StandType<?> randomStand(LivingEntity entity, Random random) {
        return randomStandFromTiers(null, entity, random);
    }
    
    public static StandType<?> randomStandFromTiers(@Nullable int[] tiers, LivingEntity entity, Random random) {
        if (!entity.level.isClientSide()) {
        	List<StandType<?>> stands = availableStands(tiers, entity);

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
    
    public static List<StandType<?>> availableStands(int[] tiers, LivingEntity entity) {
    	Collection<StandType<?>> stands = ModStandTypes.Registry.getRegistry().getValues();
    	return stands.stream()
    			.filter(stand -> (
    					tiers == null ||
    					Arrays.stream(tiers).anyMatch(tier -> tier == stand.getTier()))
    					&& !JojoModConfig.getCommonConfigInstance(entity.level.isClientSide()).isStandBanned(stand))
    			.collect(Collectors.toList());
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
        return JojoModUtil.getOrLast(xpBorders, tier).intValue();
    }
    
    public static boolean isEntityStandUser(LivingEntity entity) {
        return entity.getCapability(StandCapProvider.STAND_CAP).map(cap -> cap.hasPower()).orElse(false);
    }

    public static boolean shouldStandsRender(PlayerEntity player) {
        return isEntityStandUser(player) || player.hasEffect(ModEffects.SPIRIT_VISION.get());
    }
    
    public static boolean shouldHearStands(PlayerEntity player) {
    	return shouldStandsRender(player);
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
                    mc.setCameraEntity(manualControl ? standEntity : player);
                    if (manualControl) {
                    	// FIXME clear player input to prevent player from constant jumping
                    	// (these don't work)
                    	mc.player.input.jumping = false;
                    	mc.player.input.forwardImpulse = 0;
                    	mc.player.input.leftImpulse = 0;
                        StandController.setStartedControllingStand();
                    }
                }
            }
        }
    }
    
    public static boolean standIgnoresStaminaDebuff(IStandPower power) {
        return power.getUser() == null || power.getUser().hasEffect(ModEffects.RESOLVE.get()) || power.isUserCreative();
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
        if (StandUtil.worthyTarget(target)) {
            for (PowerClassification classification : PowerClassification.values()) {
                points *= IPower.getPowerOptional(target, classification).map(power -> {
                    if (power.hasPower()) {
                        return power.getTargetResolveMultiplier(stand);
                    }
                    return 1F;
                }).orElse(1F);
            }
            
            float pts = points;
            stand.getResolveCounter().addResolveOnAttack(pts);
        }
    }
    
    public static boolean worthyTarget(Entity target) {
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
    
    public static boolean isComboUnlocked(IStandPower power) {
    	return power.getResolveLevel() >= 1;
    }
}
