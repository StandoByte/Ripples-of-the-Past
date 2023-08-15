package com.github.standobyte.jojo.power.impl.stand;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.github.standobyte.jojo.power.impl.stand.type.StandType.StandSurvivalGameplayPool;

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
    
    public static StandType<?> randomStand(LivingEntity entity, Random random) {
        if (!entity.level.isClientSide()) {
            List<StandType<?>> stands = availableStands(entity.level.isClientSide()).collect(Collectors.toList());

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
    
    public static Stream<StandType<?>> availableStands(boolean clientSide) {
        Collection<StandType<?>> stands = JojoCustomRegistries.STANDS.getRegistry().getValues();
        return stands.stream()
                .filter(stand -> StandUtil.canPlayerGetFromArrow(stand, clientSide));
    }
    
    public static boolean canPlayerGetFromArrow(StandType<?> standType, boolean clientSide) {
        return standType.getSurvivalGameplayPool() == StandSurvivalGameplayPool.PLAYER_ARROW && 
                (!JojoModConfig.getCommonConfigInstance(clientSide).isConfigLoaded() || // to make it work when adding items to creative search tab on client initialization, when the config isn't loaded yet
                !JojoModConfig.getCommonConfigInstance(clientSide).isStandBanned(standType));
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
        IStandPower.getStandPowerOptional(player).ifPresent(standPower -> {
            if (standPower.getStandManifestation() instanceof StandEntity) {
                StandEntity standEntity = ((StandEntity) standPower.getStandManifestation());
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
        });
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
        if (!hitSelf && attackingTargetGivesResolve(target)) {
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
    
    public static boolean attackingTargetGivesResolve(Entity target) {
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
