package com.github.standobyte.jojo.power.impl.stand;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ControllerStand;
import com.github.standobyte.jojo.command.configpack.standassign.PlayerStandAssignmentConfig;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.StandControlStatusPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType.StandSurvivalGameplayPool;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class StandUtil {
    
    @Deprecated
    @Nullable
    public static StandType<?> randomStand(PlayerEntity entity, Random random) {
        return randomStandOrError(entity, random).left().orElse(null);
    }
    
    @Nonnull
    public static Either<StandType<?>, ITextComponent> randomStandOrError(PlayerEntity entity, Random random) {
        if (!entity.level.isClientSide()) {
            List<StandType<?>> stands = arrowStands(entity.level.isClientSide()).collect(Collectors.toList());
            if (stands.isEmpty()) {
                return Either.right(new TranslationTextComponent("jojo.arrow.no_stands"));
            }
            
            stands = PlayerStandAssignmentConfig.getInstance().limitToAssignedStands(entity, stands);
            if (stands.isEmpty()) {
                return Either.right(new TranslationTextComponent("jojo.arrow.assigned_banned", entity.getName()));
            }
            
            stands = JojoModConfig.getCommonConfigInstance(false).standRandomPoolFilter.get().limitStandPool((ServerWorld) entity.level, stands);
            if (stands.isEmpty()) {
                return Either.right(new TranslationTextComponent("jojo.arrow.all_stands_taken"));
            }
            
            stands = IStandPower.getStandPowerOptional(entity).resolve().get().getPreviousStandsSet().rigForUnusedStands(stands);
            Optional<StandType<?>> stand = MathUtil.getRandomWeightedDouble(stands, s -> s.getStats().getRandomWeight(), random);
            // fucking generics istg
            Optional<Either<StandType<?>, ITextComponent>> wtf = stand.map(Either::left);
            return wtf.orElse(Either.right(new TranslationTextComponent("jojo.arrow.no_stand_weights")));
            /* 
             * return stand.map(Either::left).orElse(Either.right(new TranslationTextComponent("jojo.arrow.no_stands")));
             *   ^ doesn't work because fuck you that's why
             */
        }
        else {
            throw new IllegalStateException("Can only use this function to get a random Stand on server side");
        }
    }
    
    
    
    public enum StandRandomPoolFilter {
        NONE {
            @Override
            public List<StandType<?>> limitStandPool(ServerWorld world, List<StandType<?>> availableStands) {
                return availableStands;
            }
        },
        LEAST_TAKEN {
            @Override
            public List<StandType<?>> limitStandPool(ServerWorld world, List<StandType<?>> availableStands) {
                return SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getLeastTakenStands(availableStands);
            }
        },
        NOT_TAKEN {
            @Override
            public List<StandType<?>> limitStandPool(ServerWorld world, List<StandType<?>> availableStands) {
                return SaveFileUtilCapProvider.getSaveFileCap(world.getServer()).getNotTakenStands(availableStands);
            }
        };
        
        public abstract List<StandType<?>> limitStandPool(ServerWorld world /*TODO get stand pool limit data on client*/, List<StandType<?>> availableStands);
    }
    
    public static Stream<StandType<?>> arrowStands(boolean clientSide) {
        return filterStands(stand -> StandUtil.canPlayerGetFromArrow(stand, clientSide));
    }
    
    public static Stream<StandType<?>> availableStands(boolean clientSide) {
        return filterStands(stand -> stand.getSurvivalGameplayPool().accessibleToPlayer(stand, clientSide));
    }
    
    public static Stream<StandType<?>> filterStands(Predicate<StandType<?>> filter) {
        Collection<StandType<?>> stands = JojoCustomRegistries.STANDS.getRegistry().getValues();
        return stands.stream().filter(filter);
    }
    
    public static boolean canPlayerGetFromArrow(StandType<?> standType, boolean clientSide) {
        return standType.getSurvivalGameplayPool() == StandSurvivalGameplayPool.PLAYER_ARROW && 
                StandSurvivalGameplayPool.PLAYER_ARROW.accessibleToPlayer(standType, clientSide);
    }
    
    public static boolean isStandBanned(StandType<?> standType, boolean clientSide) {
        return JojoModConfig.getCommonConfigInstance(clientSide).isConfigLoaded() && // to make it work when adding items to creative search tab on client initialization, when the config isn't loaded yet
                JojoModConfig.getCommonConfigInstance(clientSide).isStandBanned(standType);
    }
    
    public static boolean isEntityStandUser(LivingEntity entity) {
        return IStandPower.getStandPowerOptional(entity).map(IPower::hasPower).orElse(false);
    }
    
    public static boolean clStandEntityVisibleTo(PlayerEntity player) {
        if (player == ClientUtil.getClientPlayer()) {
            return ClientUtil.canSeeStands();
        }
        return playerCanSeeStands(player);
    }
    
    public static boolean playerCanSeeStands(PlayerEntity player) {
        return JojoModUtil.seesInvisibleAsSpectator(player)
                || isEntityStandUser(player) || player.hasEffect(ModStatusEffects.SPIRIT_VISION.get());
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
                        ClientUtil.setCameraEntityPreventShaderSwitch(manualControl ? standEntity : player);
                        if (manualControl) {
                            mc.player.xxa = 0;
                            mc.player.zza = 0;
                            mc.player.setJumping(false);
                            ControllerStand.setStartedControllingStand();
                        }
                    }
                }
                standEntity.manualMovementSpeed = 1;
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

    public static boolean isFinisherMechanicUnlocked(IStandPower stand) {
        return stand.hasPower() && (stand.getResolveLevel() >= 1
                || stand.getType().getStandFinisherPunch().map(action -> action.isUnlocked(stand)).orElse(false));
    }
}
