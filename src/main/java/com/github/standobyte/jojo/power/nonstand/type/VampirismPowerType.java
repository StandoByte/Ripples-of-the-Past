package com.github.standobyte.jojo.power.nonstand.type;

import java.util.Map;
import java.util.function.IntUnaryOperator;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.Difficulty;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionRemoveEvent;

public class VampirismPowerType extends NonStandPowerType<VampirismFlags> {
    private static Map<Effect, IntUnaryOperator> EFFECTS_AMPLIFIERS;
    
    public static void initVampiricEffectsMap() {
        EFFECTS_AMPLIFIERS = ImmutableMap.<Effect, IntUnaryOperator>builder()
                .put(ModEffects.UNDEAD_REGENERATION.get(), (bl) -> bl - 2)
                .put(Effects.HEALTH_BOOST, (bl) -> 3 * (bl - 3))
                .put(Effects.DAMAGE_BOOST, (bl) -> bl - 4)
                .put(Effects.MOVEMENT_SPEED, (bl) -> bl - 4)
                .put(Effects.DIG_SPEED, (bl) -> bl - 4)
                .put(Effects.JUMP, (bl) -> bl - 4)
                .put(Effects.DAMAGE_RESISTANCE, (bl) -> bl - 5)
                .put(Effects.NIGHT_VISION, (bl) -> 0)
                .build();
    }

    public VampirismPowerType(int color, Action[] startingAttacks, Action[] startingAbilities, float manaRegenPoints) {
        super(color, startingAttacks, startingAbilities, manaRegenPoints, VampirismFlags::new);
    }
    
    @Override
    public void onClear(INonStandPower power) {
        LivingEntity user = power.getUser();
        for (Map.Entry<Effect, IntUnaryOperator> entry : EFFECTS_AMPLIFIERS.entrySet()) {
            EffectInstance effectInstance = user.getEffect(entry.getKey());
            if (effectInstance != null && !effectInstance.isVisible() && !effectInstance.showIcon()) {
                user.removeEffect(effectInstance.getEffect());
            }
        }
    }

    /* x = difficultyLevel (1 - easy, 2 - normal, 3 - hard)
     * x:     0 <= mana < 150
     * x + 1: 150 <= mana < 450
     * x + 2: 450 <= mana < 750
     * x + 3: 750 <= mana <= 1000
     */
    public static int bloodLevel(IPower<?> power, Difficulty difficulty) {
        if (difficulty == Difficulty.PEACEFUL) {
            return -1;
        }
        return (int) ((power.getMana() + 150F) * 10F / (power.getMaxMana() * 3F)) + difficulty.getId();
    }

    @Override
    public void tickUser(LivingEntity entity, IPower<?> power) {
        if (!entity.level.isClientSide()) {
            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).getFoodData().setFoodLevel(17);
            }
            entity.setAirSupply(entity.getMaxAirSupply());
            int bloodLevel = bloodLevel(power, entity.level.getDifficulty());
            if (((INonStandPower) power).getTypeSpecificData(this).get().refreshBloodLevel(bloodLevel)) {
                int standStaminaIncreaseLvl = bloodLevel;
                IStandPower.getStandPowerOptional(entity).ifPresent(standPower -> {
                    standPower.setManaRegenPoints(Math.max(standStaminaIncreaseLvl, 1));
                    standPower.setManaLimitFactor(Math.max(standStaminaIncreaseLvl / 2F, 1));
                });
                for (Map.Entry<Effect, IntUnaryOperator> entry : EFFECTS_AMPLIFIERS.entrySet()) {
                    Effect effect = entry.getKey();
                    int amplifier = entry.getValue().applyAsInt(bloodLevel);
                    float missingHp = effect == Effects.HEALTH_BOOST ? entity.getMaxHealth() - entity.getHealth() : -1;
                    if (amplifier >= 0) {
                        entity.removeEffectNoUpdate(effect);
                        entity.addEffect(new EffectInstance(entry.getKey(), Integer.MAX_VALUE, amplifier, false, false));
                    }
                    else {
                        entity.removeEffect(effect);
                    }
                    if (missingHp > -1) {
                        entity.setHealth(entity.getMaxHealth() - missingHp);
                    }
                }
            }
        }
    }

    @Override
    public boolean isReplaceableWith(NonStandPowerType<?> newType) {
        return false;
    }

    @Override
    public int getExpRewardMultiplier() {
        return 5;
    }
    
    @Override
    public boolean isLeapUnlocked(INonStandPower power) {
        return true;
    }
    
    @Override
    public float getLeapStrength(INonStandPower power) {
        VampirismFlags vampirism = power.getTypeSpecificData(this).get();
        float leapStrength = Math.max(bloodLevel(power, power.getUser().level.getDifficulty()), 0);
        if (!vampirism.isVampireAtFullPower()) {
            leapStrength *= 0.25F;
        }
        return leapStrength;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 100;
    }
    
    @Override
    public float getLeapManaCost() {
        return 0;
    }

    
    
    public static void cancelVampiricEffectRemoval(PotionRemoveEvent event) {
        EffectInstance effectInstance = event.getPotionEffect();
        if (effectInstance != null) {
            LivingEntity entity = event.getEntityLiving();
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                if (power.getTypeSpecificData(ModNonStandPowers.VAMPIRISM.get()).isPresent()) {
                    int bloodLevel = bloodLevel(power, entity.level.getDifficulty());
                    Effect effect = event.getPotion();
                    if (EFFECTS_AMPLIFIERS.containsKey(effect) && EFFECTS_AMPLIFIERS.get(effect).applyAsInt(bloodLevel) == effectInstance.getAmplifier() && 
                            !effectInstance.isVisible() && !effectInstance.showIcon()) {
                        event.setCanceled(true);
                    }
                }
            });
        }
    }
    
    public static void consumeManaOnHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.isAlive()) {
            INonStandPower.getNonStandPowerOptional(entity).ifPresent(power -> {
                if (power.getType() == ModNonStandPowers.VAMPIRISM.get()) {
                    float healCost = healCost(entity.level.getDifficulty());
                    float actualHeal = Math.min(event.getAmount(), power.getMana() / healCost);
                    actualHeal = Math.min(actualHeal, entity.getMaxHealth() - entity.getHealth());
                    if (actualHeal > 0) {
                        power.consumeMana(Math.min(actualHeal, entity.getMaxHealth() - entity.getHealth()) * healCost);
                        event.setAmount(actualHeal);
                    }
                    else {
                        event.setCanceled(true);
                    }
                }
            });
        }
    }
    
    public static float healCost(Difficulty difficulty) {
        return 6.0F / difficulty.getId();
    }
    
    // TODO smite enchantment damage
}
