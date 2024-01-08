package com.github.standobyte.jojo.power.impl.nonstand.type.vampirism;

import java.util.Set;
import java.util.function.Predicate;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.block.WoodenCoffinBlock;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.potion.VampireSunBurnEffect;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHealEvent;

public class VampirismUtil {

    public static void tickSunDamage(LivingEntity entity) {
        if (!entity.level.isClientSide() && entity.invulnerableTime <= 10) {
            float sunDamage = getSunDamage(entity);
            if (    
                    sunDamage > 0
                    && DamageUtil.dealUltravioletDamage(entity, sunDamage, null, null, true)) {
                incSunBurn(entity, 1);
            }
        }
    }
    
    public static void incSunBurn(LivingEntity entity, int tickUpAmount) {
        EffectInstance sunBurnEffect = entity.getEffect(ModStatusEffects.VAMPIRE_SUN_BURN.get());
        int duration;
        int amplifier;
        if (sunBurnEffect == null) {
            duration = 60 * tickUpAmount;
            amplifier = tickUpAmount - 1;
        }
        else {
            int difficulty = Math.max(entity.level.getDifficulty().getId(), 1);
            duration = sunBurnEffect.getDuration() + 60 * tickUpAmount / difficulty;
            amplifier = duration / 60;
        }
        VampireSunBurnEffect.giveEffectTo(entity, duration, amplifier);
    }
    
//    private static final float MAX_SUN_DAMAGE = 10;
//    private static final float MIN_SUN_DAMAGE = 2;
    private static float getSunDamage(LivingEntity entity) {
        if (entity.hasEffect(ModStatusEffects.SUN_RESISTANCE.get())
                || !(entity instanceof PlayerEntity || JojoModConfig.getCommonConfigInstance(false).undeadMobsSunDamage.get())
                || entity.isSleeping() && entity.getSleepingPos().map(sleepingPos -> {
                    BlockState blockState = entity.level.getBlockState(sleepingPos);
                    return blockState.getBlock() instanceof WoodenCoffinBlock && blockState.getValue(WoodenCoffinBlock.CLOSED);
                }).orElse(false)) {
            return 0;
        }
        World world = entity.level;
        if (
                world.dimensionType().hasSkyLight()
                && !world.dimensionType().hasCeiling()
                && world.isDay()
//                && !world.isRainingAt(new BlockPos(
//                        entity.blockPosition().getX(), 
//                        entity.getBoundingBox().maxY,
//                        entity.blockPosition().getZ()))
                && !world.isRaining()
                && !world.isThundering()) {
            float brightness = entity.getBrightness();
            BlockPos blockPos = entity.getVehicle() instanceof BoatEntity ? 
                    (new BlockPos(entity.getX(), (double)Math.round(entity.getY(1.0)), entity.getZ())).above()
                    : new BlockPos(entity.getX(), (double)Math.round(entity.getY(1.0)), entity.getZ());
            if (brightness > 0.5F && world.canSeeSky(blockPos)) {
                return 4;
//                int time = (int) (world.getDayTime() % 24000L);
//                float damage = MAX_SUN_DAMAGE;
//                float diff = MAX_SUN_DAMAGE - MIN_SUN_DAMAGE;
//
//                // sunrise
//                if (time > 23460) { 
//                    time -= 24000;
//                }
//                if (time <= 60) {
//                    damage -= diff * (1F - (float) (time + 540) / 600F);
//                }
//
//                // sunset
//                else if (time > 11940 && time <= 12540) {
//                    damage -= diff * (float) (time - 11940) / 600F;
//                }
//
//                return damage;
            }
        }
        return 0;
    }
    
    
    
    public static void editMobAiGoals(MobEntity mob) {
        if (mob.getClassification(false) == EntityClassification.MONSTER) {
            VampirismUtil.makeMobNeutralToVampirePlayers(mob);
        }
        else if (mob instanceof IronGolemEntity) {
            mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, PlayerEntity.class, 5, false, false, 
                    target -> target instanceof PlayerEntity && JojoModUtil.isPlayerUndead((PlayerEntity) target)));
        }
    }
    
    private static void makeMobNeutralToVampirePlayers(MobEntity mob) {
        if (JojoModConfig.getCommonConfigInstance(false).vampiresAggroMobs.get()) return;
        
        Set<PrioritizedGoal> goals = CommonReflection.getGoalsSet(mob.targetSelector);
        for (PrioritizedGoal prGoal : goals) {
            Goal goal = prGoal.getGoal();
            if (goal instanceof NearestAttackableTargetGoal) {
                NearestAttackableTargetGoal<?> targetGoal = (NearestAttackableTargetGoal<?>) goal;
                Class<? extends LivingEntity> targetClass = CommonReflection.getTargetClass(targetGoal);
                
                if (targetClass == PlayerEntity.class) {
                    EntityPredicate selector = CommonReflection.getTargetConditions(targetGoal);
                    if (selector != null) {
                        Predicate<LivingEntity> oldPredicate = CommonReflection.getTargetSelector(selector);
                        Predicate<LivingEntity> undeadPredicate = target -> 
                            target instanceof PlayerEntity && !(
//                                    JojoModUtil.isPlayerUndead((PlayerEntity) target) &&
                                    INonStandPower.getNonStandPowerOptional(target).map(
                                            power -> power.getTypeSpecificData(ModPowers.VAMPIRISM.get())
                                            .map(vampirism -> vampirism.getCuringStage() < 3).orElse(false)).orElse(false));
                        CommonReflection.setTargetConditions(targetGoal, new EntityPredicate().range(CommonReflection.getTargetDistance(targetGoal)).selector(
                                oldPredicate != null ? oldPredicate.and(undeadPredicate) : undeadPredicate));
                    }
                }
            }
        }
    }
    
    
    
    public static void onEnchantedGoldenAppleEaten(LivingEntity entity) {
        if (!entity.level.isClientSide()) {
            EffectInstance weakness = entity.getEffect(Effects.WEAKNESS);
            if (!(weakness != null && weakness.getAmplifier() >= 4)) {
                return;
            }
            
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> {
                    if (!entity.isSilent()) {
                        entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                                ModSounds.VAMPIRE_CURE_START.get(), entity.getSoundSource(), 1.0F, 1.0F);
                    }
                    vampirism.setCuringTicks(1);
                });
            });
        }
    }
    
    
    
    public static void consumeEnergyOnHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.isAlive()) {
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                if (power.getType() == ModPowers.VAMPIRISM.get()) {
                    float healCost = healCost(entity.level);
                    if (healCost > 0) {
                        float actualHeal = Math.min(event.getAmount(), power.getEnergy() / healCost);
                        actualHeal = Math.min(actualHeal, entity.getMaxHealth() - entity.getHealth());
                        if (actualHeal > 0) {
                            power.consumeEnergy(Math.min(actualHeal, entity.getMaxHealth() - entity.getHealth()) * healCost);
                            event.setAmount(actualHeal);
                        }
                        else {
                            event.setCanceled(true);
                        }
                    }
                }
            });
        }
    }
    
    public static float healCost(World world) {
        return GeneralUtil.getOrLast(
                JojoModConfig.getCommonConfigInstance(world.isClientSide()).bloodHealCost.get(), 
                world.getDifficulty().getId()).floatValue();
    }
    
    // TODO smite enchantment damage
}
