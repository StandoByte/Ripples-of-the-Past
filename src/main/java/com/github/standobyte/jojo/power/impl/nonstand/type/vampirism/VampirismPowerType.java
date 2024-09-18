package com.github.standobyte.jojo.power.impl.nonstand.type.vampirism;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.non_stand.VampirismAction;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class VampirismPowerType extends NonStandPowerType<VampirismData> {
    public static final int COLOR = 0xFF0000;

    public VampirismPowerType(VampirismAction[] startingAttacks, VampirismAction[] startingAbilities, VampirismAction defaultMmb) {
        super(startingAttacks, startingAbilities, defaultMmb, VampirismData::new);
    }
    
    @Deprecated
    public VampirismPowerType(VampirismAction[] startingAttacks, VampirismAction[] startingAbilities) {
        super(startingAttacks, startingAbilities, startingAttacks[0], VampirismData::new);
    }
    
    @Override
    public boolean keepOnDeath(INonStandPower power) {
        return JojoModConfig.getCommonConfigInstance(false).keepVampirismOnDeath.get() && power.getTypeSpecificData(this).get().isVampireAtFullPower();
    }
    
    
    @Override
    public void clAddMissingActions(ControlScheme controlScheme, INonStandPower power) {
        super.clAddMissingActions(controlScheme, power);
        if (power.getTypeSpecificData(this).get().isVampireHamonUser()) {
            controlScheme.addIfMissing(ControlScheme.Hotbar.RIGHT_CLICK, ModVampirismActions.VAMPIRISM_HAMON_SUICIDE.get());
        }
    }
    
    @Override
    public boolean isActionLegalInHud(Action<INonStandPower> action, INonStandPower power) {
        if (action == ModVampirismActions.VAMPIRISM_HAMON_SUICIDE.get()) {
            return power.getTypeSpecificData(this).get().isVampireHamonUser();
        }
        return super.isActionLegalInHud(action, power);
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
        VampirismData vampirism = power.getTypeSpecificData(this).get();
        if (vampirism.isBeingCured()) {
            if (vampirism.getCuringStage() >= 4) {
                return 0;
            }
            return power.getEnergy() - power.getMaxEnergy() * Math.max(vampirism.getCuringProgress(), 0.25F) / 200;
        }
        
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
        int bloodLevel = Math.min((int) (power.getEnergy() / power.getMaxEnergy() * 5F), 4);
        bloodLevel += difficulty;
        if (!power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).get().isVampireAtFullPower()) {
            bloodLevel = Math.max(bloodLevel - 2, 1);
        }
        return bloodLevel;
    }
    
    @Override
    protected void initPassiveEffects() {
        initAllPossibleEffects(
                () -> Effects.HEALTH_BOOST,
                () -> Effects.REGENERATION,
                () -> Effects.DAMAGE_BOOST,
                () -> Effects.MOVEMENT_SPEED,
                () -> Effects.DIG_SPEED,
                () -> Effects.JUMP,
                () -> Effects.NIGHT_VISION,

                () -> Effects.MOVEMENT_SLOWDOWN,
                () -> Effects.DIG_SLOWDOWN,
                () -> Effects.WEAKNESS,
                () -> Effects.BLINDNESS);
    }
    
    @Override
    public int getPassiveEffectLevel(Effect effect, INonStandPower power) {
        LivingEntity entity = power.getUser();
        VampirismData vampirism = power.getTypeSpecificData(this).get();
        int difficulty = entity.level.getDifficulty().getId();
        int bloodLevel = bloodLevel(power, difficulty);
        int curingStage = vampirism.getCuringStage();
        
        if (effect.isBeneficial() && curingStage > 0) {
            if (curingStage >= 3) {
                bloodLevel = -1;
            } else {
                bloodLevel -= curingStage;
            }
        }
        if (effect.getCategory() == EffectType.HARMFUL)                     return curingStage >= 4 ? effect == Effects.BLINDNESS ? 0 : 3 - difficulty : -1;
        if (effect == Effects.HEALTH_BOOST)                                 return difficulty * (curingStage > 0 ? 5 - curingStage * 2 : 5) - 1;
        if (effect == Effects.REGENERATION)                                 return Math.min(bloodLevel - 2, 4);
        if (effect == Effects.DAMAGE_BOOST)                                 return bloodLevel - 5;
        if (effect == Effects.MOVEMENT_SPEED)                               return bloodLevel - 4;
        if (effect == Effects.DIG_SPEED)                                    return bloodLevel - 4;
        if (effect == Effects.JUMP)                                         return bloodLevel - 4;
        if (effect == Effects.NIGHT_VISION)                                 return 0;
        return -1;
    }
    
    @Override
    public void tickUser(LivingEntity entity, INonStandPower power) {
        super.tickUser(entity, power);
        VampirismData vampirism = power.getTypeSpecificData(this).get();
        if (!entity.level.isClientSide()) {
            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).getFoodData().setFoodLevel(17);
            }
            entity.setAirSupply(entity.getMaxAirSupply());

            int difficulty = entity.level.getDifficulty().getId();
            int bloodLevel = bloodLevel(power, difficulty);
            if (vampirism.refreshBloodLevel(bloodLevel)) {
                updatePassiveEffects(entity, power);
            }
        }
        vampirism.tickCuring(entity, power);
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
        return power.getTypeSpecificData(this).get().getCuringStage() < 3;
    }
    
    @Override
    public float getLeapStrength(INonStandPower power) {
        VampirismData vampirism = power.getTypeSpecificData(this).get();
        float leapStrength = Math.max(bloodLevel(power), 0);
        if (!vampirism.isVampireAtFullPower()) {
            leapStrength *= 0.15F;
        }
        return leapStrength * 0.25F;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 20;
    }
    
    @Override
    public float getLeapEnergyCost() {
        return 0;
    }
    
    
    
    public boolean isHighOnBlood(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity).map(power -> {
            return power.getType() == this && power.getEnergy() / power.getMaxEnergy() >= 0.8F;
        }).orElse(false);
    }
}
