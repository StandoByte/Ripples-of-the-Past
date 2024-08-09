package com.github.standobyte.jojo.power.impl.nonstand.type.zombie;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.non_stand.ZombieAction;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

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
    
    public static int bloodLevel(INonStandPower power, int difficulty) {
        if (difficulty == 0) {
            return -1;
        }
        int bloodLevel = Math.min((int) (power.getEnergy() / power.getMaxEnergy() * 7.5F), 4);
        bloodLevel += difficulty;
        return bloodLevel;
    }
    
    @Override
    protected void initPassiveEffects() {
        initAllPossibleEffects(
                () -> Effects.HEALTH_BOOST,
                () -> Effects.DAMAGE_BOOST,
                () -> Effects.MOVEMENT_SPEED,
                () -> Effects.DIG_SPEED,
                () -> Effects.JUMP,
                () -> Effects.NIGHT_VISION);
    }
    
    @Override
    public int getPassiveEffectLevel(Effect effect, INonStandPower power) {
        LivingEntity entity = power.getUser();
        int difficulty = entity.level.getDifficulty().getId();
        int bloodLevel = bloodLevel(power, difficulty);
        boolean disguiseEnabled = power.getTypeSpecificData(this).get().isDisguiseEnabled();
        
        if (effect == Effects.HEALTH_BOOST)                                 return difficulty * 2;
        if (effect == Effects.DAMAGE_BOOST)                                 return disguiseEnabled ? -1 : bloodLevel - 5;
        if (effect == Effects.MOVEMENT_SPEED)                               return disguiseEnabled ? -1 : bloodLevel - 5;
        if (effect == Effects.DIG_SPEED)                                    return disguiseEnabled ? -1 : bloodLevel - 5;
        if (effect == Effects.JUMP)                                         return disguiseEnabled ? -1 : bloodLevel - 5;
        if (effect == Effects.NIGHT_VISION)                                 return 0;
        
        return -1;
    }
    
    @Override
    public void tickUser(LivingEntity entity, INonStandPower power) {
        super.tickUser(entity, power);
        ZombieData zombie = power.getTypeSpecificData(this).get();
        zombie.tick();
        if (!entity.level.isClientSide()) {
            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).getFoodData().setFoodLevel(17);
            }
            entity.setAirSupply(entity.getMaxAirSupply());
            
            int difficulty = entity.level.getDifficulty().getId();
            int bloodLevel = bloodLevel(power, difficulty);
            if (zombie.refreshBloodLevel(bloodLevel)) {
                updatePassiveEffects(entity, power);
            }
        }
    }
    
    @Override
    public boolean isReplaceableWith(NonStandPowerType<?> newType) {
        return false;
    }
    
    @Override
    public float getTargetResolveMultiplier(INonStandPower power, IStandPower attackingStand) {
        return 1;
    }
    
    @Override
    public boolean isLeapUnlocked(INonStandPower power) {
        return true;
    }
    
    @Override
    public float getLeapStrength(INonStandPower power) {
        float leapStrength = Math.max(bloodLevel(power), 0);
        return leapStrength * 0.2F;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 20;
    }
    
    @Override
    public float getLeapEnergyCost() {
        return 0;
    }
}
