package com.github.standobyte.jojo.power.impl.nonstand.type.zombie;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.non_stand.VampirismAction;
import com.github.standobyte.jojo.action.non_stand.ZombieAction;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;

public class ZombiePowerType extends NonStandPowerType<ZombieData> {
    public static final int COLOR = 0x99BB00;

    public ZombiePowerType(ZombieAction[] startingAttacks, ZombieAction[] startingAbilities, ZombieAction defaultMmb) {
        super(startingAttacks, startingAbilities, defaultMmb, ZombieData::new);
    }

    public ZombiePowerType(ZombieAction[] startingAttacks, ZombieAction[] startingAbilities) {
        super(startingAttacks, startingAbilities, startingAttacks[0], ZombieData::new);
    }
    
    @Override
    public boolean keepOnDeath(INonStandPower power) {
        return false;
    }
    
    @Override
    public void afterClear(INonStandPower power) {
        LivingEntity user = power.getUser();
        for (Effect effect : EFFECTS) {
            EffectInstance effectInstance = user.getEffect(effect);
            if (effectInstance != null && !effectInstance.isVisible() && !effectInstance.showIcon()) {
                user.removeEffect(effectInstance.getEffect());
            }
        }
    }
    
    @Override
    public float getMaxEnergy(INonStandPower power) {
        World world = power.getUser().level;
        return super.getMaxEnergy(power) * GeneralUtil.getOrLast(
                JojoModConfig.getCommonConfigInstance(world.isClientSide()).maxBloodMultiplier.get(), world.getDifficulty().getId())
                .floatValue();
    }
    
    @Override
    public float tickEnergy(INonStandPower power) {
        World world = power.getUser().level;
        float inc = -GeneralUtil.getOrLast(
                JojoModConfig.getCommonConfigInstance(world.isClientSide()).bloodTickDown.get(), world.getDifficulty().getId())
                .floatValue();
        if (power.isUserCreative()) {
            inc = Math.max(inc, 0);
        }
        return power.getEnergy() + inc;
    }
    
    @Override
    public float getMaxStaminaFactor(INonStandPower power, IStandPower standPower) {
        return Math.max((bloodLevel(power) - 4) * 2, 1);
    }
    
    @Override
    public float getStaminaRegenFactor(INonStandPower power, IStandPower standPower) {
        return Math.max((bloodLevel(power) - 4) * 4, 1);
    }
    
    private static int bloodLevel(INonStandPower power) {
        return bloodLevel(power, power.getUser().level.getDifficulty().getId());
    }
    
    // full blood bar on normal => 6
    public static int bloodLevel(INonStandPower power, int difficulty) {
        if (difficulty == 0) {
            return -1;
        }
        int bloodLevel = Math.min((int) (power.getEnergy() / power.getMaxEnergy() * 7.5F), 4);
        bloodLevel += difficulty;
        return bloodLevel;
    }
    
    @Override
    public void tickUser(LivingEntity entity, INonStandPower power) {
        ZombieData zombie = power.getTypeSpecificData(this).get();
        zombie.tick();
        if (!entity.level.isClientSide()) {
            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).getFoodData().setFoodLevel(17);
            }
            entity.setAirSupply(entity.getMaxAirSupply());
            int difficulty = entity.level.getDifficulty().getId();
            int bloodLevel = bloodLevel(power, difficulty);
            //Disguise toggle
            if(!(power.getTypeSpecificData(ModPowers.ZOMBIE.get()).get().isDisguiseEnabled())) {
                for (Effect effect : EFFECTS) {
                    if(effect != Effects.HEALTH_BOOST) {
                        entity.removeEffect(effect);
                    }
                }
            } else {
                for (Effect effect : EFFECTS) {
                    int amplifier = getEffectAmplifier(effect, bloodLevel, difficulty, power);
                    if (effect.isBeneficial() && effect != Effects.HEALTH_BOOST) {
                        entity.addEffect(new EffectInstance(effect, Integer.MAX_VALUE, amplifier, false, false));
                    }
                }
            }
            
            if (zombie.refreshBloodLevel(bloodLevel)) {
                for (Effect effect : EFFECTS) {
                    int amplifier = getEffectAmplifier(effect, bloodLevel, difficulty, power);
                    float missingHp = -1;
                    boolean maxHpIncreased = false;
                    if (effect == Effects.HEALTH_BOOST) {
                        missingHp = entity.getMaxHealth() - entity.getHealth();
                        maxHpIncreased = amplifier >= 0 && 
                                amplifier > Optional.ofNullable(entity.getEffect(Effects.HEALTH_BOOST)).map(EffectInstance::getAmplifier).orElse(-1);
                    }
                    if (amplifier >= 0) {
                        entity.removeEffectNoUpdate(effect);
                        entity.addEffect(new EffectInstance(effect, Integer.MAX_VALUE, amplifier, false, false));
                    }
                    else {
                        entity.removeEffect(effect);
                    }
                    if (missingHp > -1) {
                        if (maxHpIncreased) {
                            entity.setHealth(entity.getMaxHealth() - missingHp);
                        }
                        else {
                            entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));
                        }
                    }
                }
            }
        }
    }
    
    private static int getEffectAmplifier(Effect effect, int bloodLevel, int difficulty, INonStandPower power) {
        if (effect == Effects.HEALTH_BOOST)                                 return difficulty * 2;
        if (effect == Effects.DAMAGE_BOOST)                                 return bloodLevel - 5;
        if (effect == Effects.MOVEMENT_SPEED)                               return bloodLevel - 5;
        if (effect == Effects.DIG_SPEED)                                    return bloodLevel - 5;
        if (effect == Effects.JUMP)                                         return bloodLevel - 5;
        if (effect == Effects.NIGHT_VISION)                                 return 0;
        return -1;
    }
    
    private static final Set<Effect> EFFECTS = new HashSet<>();
    public static void initZombieEffects() {
        Collections.addAll(EFFECTS, 
                Effects.HEALTH_BOOST,
                Effects.DAMAGE_BOOST,
                Effects.MOVEMENT_SPEED,
                Effects.DIG_SPEED,
                Effects.JUMP,
                Effects.NIGHT_VISION,
                
                Effects.MOVEMENT_SLOWDOWN,
                Effects.DIG_SLOWDOWN,
                Effects.WEAKNESS,
                Effects.BLINDNESS);
    }
    
    @Override
    public boolean isReplaceableWith(NonStandPowerType<?> newType) {
        return false;
    }
    
    @Override
    public float getTargetResolveMultiplier(INonStandPower power, IStandPower attackingStand) {
        LivingEntity entity = power.getUser();
        if (entity != null) {
            return (float) Math.pow(2, Math.max(entity.level.getDifficulty().getId() - 1, 0));
        }
        return 1;
    }
    
    @Override
    public boolean isLeapUnlocked(INonStandPower power) {
        return true;
    }
    
    @Override
    public float getLeapStrength(INonStandPower power) {
        float leapStrength = Math.max(bloodLevel(power), 0);
        return leapStrength * 0.20F;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 20;
    }
    
    @Override
    public float getLeapEnergyCost() {
        return 0;
    }
    
    
    
    public static void cancelZombieEffectRemoval(PotionRemoveEvent event) {
        EffectInstance effectInstance = event.getPotionEffect();
        if (effectInstance != null) {
            LivingEntity entity = event.getEntityLiving();
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.ZOMBIE.get()).ifPresent(zombie -> {
                    int difficulty = entity.level.getDifficulty().getId();
                    int bloodLevel = bloodLevel(power, difficulty);
                    Effect effect = event.getPotion();
                    if (EFFECTS.contains(effect) && 
                            getEffectAmplifier(effect, bloodLevel, difficulty, power) == effectInstance.getAmplifier() && 
                            !effectInstance.isVisible() && !effectInstance.showIcon() && 
                            power.getTypeSpecificData(ModPowers.ZOMBIE.get()).get().isDisguiseEnabled()) {
                        event.setCanceled(true);
                    }
                });
            });
        }
    }
}
