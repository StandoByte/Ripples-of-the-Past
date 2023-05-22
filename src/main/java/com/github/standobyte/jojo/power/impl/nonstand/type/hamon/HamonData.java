package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.non_stand.HamonMetalSilverOverdrive;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BarType;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket.HamonSkillsTab;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillAddPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillRemovePacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonUiEffectPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonAuraColorPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonAuraColorPacket.HamonAuraColor;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonBreathStabilityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonEnergyTicksPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonStatsPacket;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.CharacterHamonTechnique;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.HamonTechniqueManager;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.github.standobyte.jojo.util.mod.ModInteractionUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class HamonData extends TypeSpecificData {
    public static final int MAX_STAT_LEVEL = 60;
    public static final float MAX_BREATHING_LEVEL = 100;
    public static final int MIN_BREATHING_EXCEED = 10;
    
    private static final int LVL_1_POINTS = 2;
    private static final int NEXT_LVL_DIFF = 3;
    public static final int MAX_HAMON_POINTS = pointsAtLevel(MAX_STAT_LEVEL);
    
    private final Random random = new Random();

    private int hamonStrengthPoints;
    private int hamonStrengthLevel;
    private int hamonControlPoints;
    private int hamonControlLevel;
    private float hamonDamageFactor = 1F;
    
    private float breathingTrainingLevel;
    private float breathingTrainingDayBonus;
    private EnumMap<Exercise, Integer> exerciseTicks = new EnumMap<Exercise, Integer>(Exercise.class);
    private float avgExercisePoints;
    
    private boolean isMeditating;
    private int meditationTicks;

    private MainHamonSkillsManager hamonSkills;
    
    private Set<PlayerEntity> newLearners = new HashSet<>();

    private int noEnergyDecayTicks = 0;
    private boolean playedEnergySound = false;
    private float breathStability;
    private float prevBreathStability;

    public HamonData() {
        hamonSkills = new MainHamonSkillsManager();
        for (Exercise exercise : Exercise.values()) {
            exerciseTicks.put(exercise, 0);
        }
    }
    
    public void tick() {
        updateHeight = false;
        if (!power.getUser().level.isClientSide()) {
            tickNewPlayerLearners(power.getUser());
        }
        tickChargeParticles();
        tickBreathStability();
    }
    
    public float tickEnergy() {
        LivingEntity user = power.getUser();
        if (power.getHeldAction() == ModHamonActions.HAMON_BREATH.get() && user.getAirSupply() >= user.getMaxAirSupply()) {
            if (user.level.isClientSide() && power.getEnergy() > 0 && !playedEnergySound) {
                ClientTickingSoundsHelper.playHamonEnergyConcentrationSound(user, 1.0F);
                playedEnergySound = true;
            }
            updateNoEnergyDecayTicks();
            return power.getEnergy() + getMaxBreathStability() / fullEnergyTicks();
        }
        else {
            playedEnergySound = false;
            if (noEnergyDecayTicks > 0) {
                noEnergyDecayTicks--;
                return power.getEnergy();
            }
            else {
                return power.getEnergy() - 20F;
            }
        }
    }

    public float getMaxEnergy() {
        return getBreathStability();
    }
    
    public float getBreathStability() {
        return breathStability;
    }
    
    public float getPrevBreathStability() {
        return prevBreathStability;
    }
    
    public void setBreathStability(float value) {
        value = Math.min(value, getMaxBreathStability());
        boolean send = this.breathStability != value;
        this.breathStability = value;
        this.prevBreathStability = value;
        if (send) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonBreathStabilityPacket(player.getId(), getBreathStability()), player);
            });
        }
    }
    
    private int prevAir = 300;
    private void tickBreathStability() {
        LivingEntity user = power.getUser();
        boolean outOfBreath = user.getAirSupply() < user.getMaxAirSupply();
        int meditationTicks = isMeditating() ? Math.max(this.meditationTicks - MEDITATION_INC_START, 0) : 0;
        float inc = 0;
        
        if (!outOfBreath) {
            inc = getMaxBreathStability() / fullBreathStabilityTicks();
            if (meditationTicks > 0) {
                inc *= MathHelper.sqrt(meditationTicks);
            }
        }
        
        breathStability = MathHelper.clamp(breathStability + inc, 0, getMaxBreathStability());
        int air = user.getAirSupply();
        if (!user.level.isClientSide()) {
            if (breathStability == 0 && (prevBreathStability > 0 || prevAir > air && air > 0)) {
                outOfBreath();
            }
        }
        else if (user == ClientUtil.getClientPlayer()) {
            if (breathStability >= getMaxBreathStability()) {
                BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).resetRedHighlight();
            }
        }
        prevBreathStability = breathStability;
        prevAir = air;
    }
    
    private float fullEnergyTicks() {
        float ticks = 60F - (30F * breathingTrainingLevel / MAX_BREATHING_LEVEL);
        if (meditationCompleted) {
            ticks -= MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION;
        }
        return ticks;
    }
    
    private float fullBreathStabilityTicks() {
        float ticks = 1000F - (600F * breathingTrainingLevel / MAX_BREATHING_LEVEL);
        return ticks;
    }
    
    public float getMaxBreathStability() {
        float max = NonStandPower.BASE_MAX_ENERGY * (1F + getHamonControlLevel() * 0.1F);
        if (swimmingCompleted) {
            max *= SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER;
        }
        return max;
    }
    
    public void setNoEnergyDecayTicks(TrHamonEnergyTicksPacket packet) {
        this.noEnergyDecayTicks = packet.getTicks();
    }
    
    private void updateNoEnergyDecayTicks() {
        noEnergyDecayTicks = 20 + MathUtil.fractionRandomInc(180F * getBreathingLevel() / HamonData.MAX_BREATHING_LEVEL);
    }
    
    
    
    public static final float ALL_EXERCISES_EFFICIENCY_MULTIPLIER = 1.05F;
    public float getHamonEfficiency(float energyCost) {
        float efficiency = getHamonEnergyUsageEfficiency(energyCost, false) * getBloodstreamEfficiency();
        if (allExercisesCompleted) {
            efficiency *= ALL_EXERCISES_EFFICIENCY_MULTIPLIER;
        }
        return efficiency;
    }
    
    @Nullable
    public <T> T consumeHamonEnergyTo(Function<Float, T> actionWithHamonEfficiency, float energyCost) {
        float efficiency = getHamonEfficiency(energyCost);
        if (efficiency > 0) {
            T result = actionWithHamonEfficiency.apply(efficiency);
            getHamonEnergyUsageEfficiency(energyCost, true);
            return result;
        }
        return null;
    }

    public float getHamonDamageMultiplier() {
        return hamonDamageFactor;
    }
    
    
    
    private static final double STR_EXP_SCALING = 1.0333;
    private static final double BRTH_SCALING = 0.04;
    private static float dmgFormula(float strength, float breathingTraining) {
        return (float) (Math.pow(STR_EXP_SCALING, strength) * (1 + BRTH_SCALING * breathingTraining));
    }
    
    float getHamonEnergyUsageEfficiency(float energyNeeded, boolean doConsume) {
        doConsume &= !power.getUser().level.isClientSide() && !power.isUserCreative();
        energyNeeded = reduceEnergyConsumed(energyNeeded, power, power.getUser());
        
        if (power.getEnergy() >= energyNeeded || energyNeeded == 0) {
            if (doConsume) {
                power.setEnergy(power.getEnergy() - energyNeeded);
            }
            return 1;
        }
        
        else if (power.getEnergy() > 0) {
            float energyRatio = power.getEnergy() / energyNeeded;
            if (doConsume) {
                power.setEnergy(0);
            }
            return 0.25F + 0.75F * energyRatio;
        }
        
        else {
            float energyFromStability = getBreathStability() * 3;
            if (energyFromStability == 0) {
                return 0;
            }
            float energyRatio = Math.min(energyFromStability / energyNeeded, 1);
            if (doConsume) {
                if (energyFromStability < energyNeeded) {
                    setBreathStability(0);
                    outOfBreath();
                }
                else {
                    setBreathStability((energyFromStability - energyNeeded) / 3);
                }
                
                serverPlayer.ifPresent(player -> {
                    PacketManager.sendToClient(new HamonUiEffectPacket(HamonUiEffectPacket.Type.NO_ENERGY), player);
                });
            }
            return 0.25F * energyRatio;
        }
    }
    
    private void outOfBreath() {
        power.getUser().setAirSupply(0);
        serverPlayer.ifPresent(player -> {
            PacketManager.sendToClient(new HamonUiEffectPacket(HamonUiEffectPacket.Type.OUT_OF_BREATH), player);
        });
    }
    
    private float reduceEnergyConsumed(float amount, INonStandPower power, LivingEntity user) {
        if (user.getItemBySlot(EquipmentSlotType.HEAD).getItem() == ModItems.SATIPOROJA_SCARF.get()) {
            amount *= 0.6F;
        }
        return amount;
    }
    
    public float getBloodstreamEfficiency() {
        float efficiency = 1;
        LivingEntity user = power.getUser();
        
        float healthRatio = user.getHealth() / user.getMaxHealth();
        if (healthRatio < 0.5F) {
            efficiency *= healthRatio * 1.5F + 0.25F;
        }
        
        float freeze = 0;
        EffectInstance freezeEffect = user.getEffect(ModEffects.FREEZE.get());
        if (freezeEffect != null) {
            freeze = Math.min((freezeEffect.getAmplifier() + 1) * 0.25F, 1);
        }
        freeze = Math.max(ModInteractionUtil.getEntityFreeze(user), freeze);
        efficiency *= (1F - freeze);
        
        return efficiency;
    }
    
    
    
    @Override
    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower powerData) {
        return action == ModHamonActions.HAMON_OVERDRIVE.get()
                || action == ModHamonActions.HAMON_HEALING.get()
                || action == ModHamonActions.HAMON_BREATH.get()
                || hamonSkills.isUnlockedFromSkills(action);
    }
    
    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType) {
        hamonSkills.addSkill(ModHamonSkills.OVERDRIVE.get());
        hamonSkills.addSkill(ModHamonSkills.HEALING.get());
        breathStability = getMaxBreathStability();
        prevBreathStability = breathStability;
    }
    
    
    public static int pointsAtLevel(int level) {
        return level * (
                LVL_1_POINTS
                + LVL_1_POINTS + (level - 1) * NEXT_LVL_DIFF)
                / 2;
    }
    
    public static int levelFromPoints(int points) {
        int b = 2 * LVL_1_POINTS - NEXT_LVL_DIFF;
        return MathHelper.floor(Math.sqrt((b * b + 8 * NEXT_LVL_DIFF * points)) - b) / (2 * NEXT_LVL_DIFF);
    }
    
    public static int pointsAtLevelFraction(float level) {
        int lvlFloored = MathHelper.floor(level);
        int pointsFullLvls = pointsAtLevel(lvlFloored);
        int pointsNextLvl = pointsAtLevel(lvlFloored + 1);
        return pointsFullLvls + MathHelper.floor((float) (pointsNextLvl - pointsFullLvls) * MathHelper.frac(level));
    }
    
    public static float levelFractionFromPoints(int points) {
        int curLvl = levelFromPoints(points);
        int curLvlPointsInt = pointsAtLevel(curLvl);
        int pointsNextLvl = pointsAtLevel(curLvl + 1);
        return (float) curLvl + (float) (points - curLvlPointsInt) / (float) (pointsNextLvl - curLvlPointsInt);
    }
    
    public void setHamonStatPoints(HamonStat stat, int points, boolean ignoreTraining, boolean allowLesserValue) {
        int oldPoints = getStatPoints(stat);
        int oldLevel = getStatLevel(stat);
        if (!ignoreTraining) {
            int levelLimit = (int) getBreathingLevel() + HamonData.MIN_BREATHING_EXCEED;
            if (levelFromPoints(points) > levelLimit) {
                points = pointsAtLevel(levelLimit + 1) - 1;
            }
        }
        if (!allowLesserValue && points <= oldPoints) {
            return;
        }
        int newPoints = MathHelper.clamp(points, 0, MAX_HAMON_POINTS);
        switch (stat) {
        case STRENGTH:
            hamonStrengthPoints = newPoints;
            hamonStrengthLevel = levelFromPoints(newPoints);
            break;
        case CONTROL:
            hamonControlPoints = newPoints;
            hamonControlLevel = levelFromPoints(newPoints);
            break;
        }
        if (oldPoints != newPoints) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonStatsPacket(player.getId(), true, stat, newPoints), player);
                ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
            });
            if (oldLevel != getStatLevel(stat)) {
                switch (stat) {
                case STRENGTH:
                    recalcHamonDamage();
                    break;
                case CONTROL:
                    break;
                }
            }
        }
    }
    
    public static final float MAX_HAMON_STRENGTH_MULTIPLIER = dmgFormula(MAX_STAT_LEVEL, MAX_BREATHING_LEVEL); // 35.6908
    private void recalcHamonDamage() {
        hamonDamageFactor = dmgFormula(hamonStrengthLevel, breathingTrainingLevel);
    }
    
    public int getHamonStrengthPoints() {
        return hamonStrengthPoints;
    }
    
    public int getHamonStrengthLevel() {
        return hamonStrengthLevel;
    }
    
    public int getHamonControlPoints() {
        return hamonControlPoints;
    }
    
    public int getHamonControlLevel() {
        return hamonControlLevel;
    }
    
    public float getHamonStrengthLevelRatio() {
        return (float) getHamonStrengthLevel() / (float) MAX_STAT_LEVEL;
    }
    
    public float getHamonControlLevelRatio() {
        return (float) getHamonControlLevel() / (float) MAX_STAT_LEVEL;
    }
    
    private int getStatPoints(HamonStat stat) {
        switch (stat) {
        case STRENGTH:
            return getHamonStrengthPoints();
        case CONTROL:
            return getHamonControlPoints();
        default:
            throw new IllegalArgumentException("Unexpected HamonStat constant: " + stat.name());
        }
    }
    
    private int getStatLevel(HamonStat stat) {
        switch (stat) {
        case STRENGTH:
            return getHamonStrengthLevel();
        case CONTROL:
            return getHamonControlLevel();
        default:
            throw new IllegalArgumentException("Unexpected HamonStat constant: " + stat.name());
        }
    }
    
    public static final int MAX_SKILL_POINTS_LVL = 55;
    public int getSkillPoints(HamonStat stat) {
        int lvl = getStatLevel(stat);
        int spentPoints;
        switch (stat) {
        case STRENGTH:
            spentPoints = hamonSkills.getBaseSkills().getSpentStrengthPoints();
            break;
        case CONTROL:
            spentPoints = hamonSkills.getBaseSkills().getSpentControlPoints();
            break;
        default:
            throw new IllegalArgumentException("Unexpected HamonStat constant: " + stat.name());
        }
        return MathHelper.clamp(lvl, 0, MAX_SKILL_POINTS_LVL) / 5 - spentPoints;
    }
    
    public int nextSkillPointLvl(HamonStat stat) {
        return MathHelper.clamp(getStatLevel(stat), 0, MAX_SKILL_POINTS_LVL - 1) / 5 * 5 + 5;
    }
    
    private static final float ENERGY_PER_POINT = 750F;
    public void hamonPointsFromAction(HamonStat stat, float energyCost) {
        if (isSkillLearned(ModHamonSkills.NATURAL_TALENT.get())) {
            energyCost *= 2;
        }
        energyCost *= JojoModConfig.getCommonConfigInstance(false).hamonPointsMultiplier.get().floatValue();
        int points = (int) (energyCost / ENERGY_PER_POINT);
        if (random.nextFloat() < (energyCost % ENERGY_PER_POINT) / ENERGY_PER_POINT) points++;
        setHamonStatPoints(stat, getStatPoints(stat) + points, false, false);
    }
    
    public float getBreathingLevel() {
        return breathingTrainingLevel;
    }
    
    public void setBreathingLevel(float level) {
        float oldLevel = breathingTrainingLevel;
        breathingTrainingLevel = MathHelper.clamp(level, 0, MAX_BREATHING_LEVEL);
        if (oldLevel != breathingTrainingLevel) {
            recalcHamonDamage();
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonStatsPacket(player.getId(), true, getBreathingLevel()), player);
                ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
            });
        }
        if (!power.getUser().level.isClientSide()) {
            giveBreathingTrainingBuffs(power.getUser());
        }
    }
    
    private static final AttributeModifier ATTACK_DAMAGE = new AttributeModifier(
            UUID.fromString("8dcb2ad7-6067-4615-b7b6-af5256537c10"), "Attack damage from Hamon Training", 0.02D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier ATTACK_SPEED = new AttributeModifier(
            UUID.fromString("995b2915-9053-472c-834c-f94251e81659"), "Attack speed from Hamon Training", 0.025D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MOVEMENT_SPEED = new AttributeModifier(
            UUID.fromString("ffa9ba4e-3811-44f7-a4a9-887ffbd47390"), "Movement speed from Hamon Training", 0.0004D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier SWIMMING_SPEED = new AttributeModifier(
            UUID.fromString("34dcb563-6759-4a2b-9dd8-ad2dd7e70404"), "Swimming speed from Hamon Training", 0.01D, AttributeModifier.Operation.ADDITION);
    
    private void giveBreathingTrainingBuffs(LivingEntity entity) {
        int lvl = (int) getBreathingLevel();
        applyAttributeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE, lvl);
        applyAttributeModifier(entity, Attributes.ATTACK_SPEED, ATTACK_SPEED, lvl);
        applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED, lvl);
        applyAttributeModifier(entity, ForgeMod.SWIM_SPEED.get(), SWIMMING_SPEED, lvl);
    }
    
    private static void applyAttributeModifier(LivingEntity entity, Attribute attribute, AttributeModifier modifier, int lvl) {
        ModifiableAttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(modifier);
            attributeInstance.addTransientModifier(new AttributeModifier(modifier.getId(), modifier.getName() + " " + lvl, modifier.getAmount() * lvl, modifier.getOperation()));
        }
    }
    
    public static final AttributeModifier RUNNING_COMPLETED = new AttributeModifier(
            UUID.fromString("b730b24e-e970-4a94-b300-57e2555b42b5"), "Movement speed from running exercise", 0.1D, AttributeModifier.Operation.MULTIPLY_BASE);
    public static final AttributeModifier MINING_COMPLETED = new AttributeModifier(
            UUID.fromString("8674ea35-6eaf-4e22-98da-4ec0c5a4d20d"), "Attack speed from running exercise", 0.05D, AttributeModifier.Operation.MULTIPLY_BASE);
    public static final float SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER = 1.1F;
    public static final float MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION = 10;
    private boolean swimmingCompleted = false;
    private boolean meditationCompleted = false;
    private boolean allExercisesCompleted = false;
    
    private void updateExerciseAttributes(LivingEntity entity) {
        boolean allComplete = true;
        for (Exercise exercise : Exercise.values()) {
            if (isExerciseComplete(exercise)) {
                switch (exercise) {
                case RUNNING:
                    if (!entity.level.isClientSide()) {
                        applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED, RUNNING_COMPLETED, 1);
                    }
                    break;
                case MINING:
                    if (!entity.level.isClientSide()) {
                        applyAttributeModifier(entity, Attributes.ATTACK_SPEED, MINING_COMPLETED, 1);
                    }
                    break;
                case SWIMMING:
                    if (!entity.level.isClientSide()) {
                        setBreathStability(getBreathStability() * SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER);
                    }
                    swimmingCompleted = true;
                    break;
                case MEDITATION:
                    meditationCompleted = true;            
                    break;
                }
            }
            
            else {
                allComplete = false;
                switch (exercise) {
                case RUNNING:
                    if (!entity.level.isClientSide()) {
                        ModifiableAttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                        if (attributeInstance != null) {
                            attributeInstance.removeModifier(RUNNING_COMPLETED);
                        }
                    }
                    break;
                case MINING:
                    if (!entity.level.isClientSide()) {
                        ModifiableAttributeInstance attributeInstance = entity.getAttribute(Attributes.ATTACK_SPEED);
                        if (attributeInstance != null) {
                            attributeInstance.removeModifier(MINING_COMPLETED);
                        }
                    }
                    break;
                case SWIMMING:
                    swimmingCompleted = false;
                    break;
                case MEDITATION:
                    meditationCompleted = false;
                    break;
                }
            }
        }
        
        allExercisesCompleted = allComplete;
    }
    
    public boolean isExerciseComplete(Exercise exercise) {
        return getExerciseTicks(exercise) >= exercise.getMaxTicks(this);
    }
    
    public boolean allExercisesCompleted() {
        return allExercisesCompleted;
    }
    
    private boolean incExerciseLastTick;
    private boolean incExerciseThisTick;
    private boolean exerciseCompleted;
    private Vector3d prevPos = null;
    
    private int blocksMiningDelay;
    public void tickExercises(PlayerEntity user) {
        Vector3d pos = user.position();
        boolean positionChanged = prevPos == null || prevPos.x != pos.x || prevPos.y != pos.y;
        this.prevPos = pos;
        incExerciseThisTick = false;
        exerciseCompleted = false;
        
        float multiplier = user.getItemBySlot(EquipmentSlotType.HEAD).getItem() == ModItems.BREATH_CONTROL_MASK.get() ? 2F : 0;
        
        boolean isMining;
        if (!user.level.isClientSide()) {
            PlayerInteractionManager gamemode = ((ServerPlayerEntity) user).gameMode;
            boolean isDestroying = gamemode.isDestroyingBlock;
            boolean delayedDestroy = gamemode.hasDelayedDestroy;
            isMining = isDestroying || delayedDestroy;
        }
        else {
            isMining = ClientUtil.isDestroyingBlock();
        }
        if (isMining) {
            blocksMiningDelay = 6;
        }
        else {
            isMining = blocksMiningDelay-- > 0;
        }
        if (isMining) {
            incExerciseTicks(Exercise.MINING, multiplier, user.level.isClientSide());
        }
        
        if (positionChanged && user.isSwimming() && JojoModUtil.playerHasClientInput(user)) {
            incExerciseTicks(Exercise.SWIMMING, multiplier, user.level.isClientSide());
        }
        
        else if (positionChanged && user.isSprinting() && user.isOnGround() && !user.isSwimming()) {
            incExerciseTicks(Exercise.RUNNING, multiplier, user.level.isClientSide());
        }
        
        if (isMeditating()) {
            if (++meditationTicks >= MEDITATION_INC_START) {
                incExerciseTicks(Exercise.MEDITATION, multiplier, user.level.isClientSide());
            }
            updateBbHeight(user);
            if (!user.level.isClientSide()) {
                user.getFoodData().addExhaustion(-0.0025F);
                if (user.tickCount % 200 == 0 && user.isHurt() && user.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
                    user.heal(1.0F);
                }
            }
        }
        
        if (incExerciseThisTick) {
            recalcAvgExercisePoints();
        }
        if (incExerciseLastTick && !incExerciseThisTick || exerciseCompleted) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClient(new HamonExercisesPacket(this), player);
            });
        }
        if (exerciseCompleted) {
            updateExerciseAttributes(user);
        }
        incExerciseLastTick = incExerciseThisTick;
    }
    
    private static final int MEDITATION_INC_START = 40;
    // FIXME !!! do not update if playerAnimator isn't installed
    private float bbHeightMult = 1;
    private boolean updateHeight = false;
    private void updateBbHeight(LivingEntity user) {
        if (isMeditating) {
            if (meditationTicks <= 35) {
                bbHeightMult = 1 - meditationTicks * 0.0085F;
                updateHeight = true;
                user.refreshDimensions();
            }
        }
        else {
            bbHeightMult = 1;
            updateHeight = true;
            user.refreshDimensions();
        }
    }
    
    private void actuallyUpdateBbHeight(EntityEvent.Size event) {
        if (updateHeight) {
            EntitySize size = event.getNewSize();
            float width = size.width;
            float height = size.height * bbHeightMult;
            float heightDiff = size.height - height;
            size = size.fixed ? EntitySize.fixed(width, height) : EntitySize.scalable(width, height);
            event.setNewSize(size, bbHeightMult == 1);
            if (bbHeightMult != 1) {
                event.setNewEyeHeight(1.62F - heightDiff);
            }
        }
    }
    
    @SubscribeEvent
    public static void updateBoundingBox(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity user = (LivingEntity) entity;
            if (user.getAttributes() == null) { // means that the event was created in the constructor, when capabilities haven't been initialized yet
                return;
            }
            INonStandPower.getNonStandPowerOptional(user).ifPresent(cap -> {
                cap.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.actuallyUpdateBbHeight(event);
                });
            });
        }
    }
    
    public int getExerciseTicks(Exercise exercise) {
        return exerciseTicks.get(exercise);
    }
    
    private void incExerciseTicks(Exercise exercise, float multiplier, boolean clientSide) {
        int ticks = exerciseTicks.get(exercise);
        int maxTicks = exercise.getMaxTicks(this);
        if (ticks < maxTicks) {
            int inc = 1;
            if (multiplier > 1F) {
                inc = MathHelper.floor(multiplier);
                if (random.nextFloat() < MathHelper.frac(multiplier)) inc++;
            }
            inc = Math.min(inc, maxTicks - ticks);
            if (ticks + inc == maxTicks) {
                if (clientSide) {
                    return;
                }
                else {
                    this.exerciseCompleted = true;
                }
            }
            setExerciseValue(exercise, ticks + inc, clientSide);
            this.incExerciseThisTick = true;
        }
    }
    
    public void setExerciseTicks(int mining, int running, int swimming, int meditation, boolean clientSide) {
        setExerciseValue(Exercise.MINING, mining, clientSide);
        setExerciseValue(Exercise.RUNNING, running, clientSide);
        setExerciseValue(Exercise.SWIMMING, swimming, clientSide);
        setExerciseValue(Exercise.MEDITATION, meditation, clientSide);
        recalcAvgExercisePoints();
        updateExerciseAttributes(power.getUser());
    }
    
    private void setExerciseValue(Exercise exercise, int value, boolean clientSide) {
        if (exerciseTicks.put(exercise, value) != value && clientSide) {
            ActionsOverlayGui.getInstance().onHamonExerciseValueChanged(exercise);
        }
    }
    
    private void recalcAvgExercisePoints() {
        avgExercisePoints = (float) exerciseTicks.entrySet()
                .stream()
                .mapToDouble(entry -> (double) entry.getValue() / (double) entry.getKey().getMaxTicks(this))
                .reduce(Double::sum)
                .getAsDouble()
                / (float) exerciseTicks.size();
    }
    
    public void setIsMeditating(LivingEntity user, boolean isMeditating) {
        if (this.isMeditating != isMeditating) {
            this.isMeditating = isMeditating;
            this.meditationTicks = 0;
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonMeditationPacket(user.getId(), isMeditating), user);
            }
            if (!isMeditating) {
                updateBbHeight(user);
            }
            
            user.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                if (isMeditating) {
                    cap.lockYRot(user.yBodyRot);
                    cap.lockXRot(0);
                }
                else {
                    cap.clearLockedYRot();
                    cap.clearLockedXRot();
                }
            });
        }
    }
    
    public boolean isMeditating() {
        return isMeditating;
    }
    
    public int getMeditationTicks() {
        return meditationTicks;
    }
    
    public float getTrainingBonus() {
        return breathingTrainingDayBonus;
    }
    
    public float multiplyPositiveBreathingTraining(float training) {
        if (training > 0) {
            if (isSkillLearned(ModHamonSkills.NATURAL_TALENT.get())) {
                training *= 2;
            }
            training *= JojoModConfig.getCommonConfigInstance(false).breathingTrainingMultiplier.get().floatValue();
        }
        return training;
    }
    
    public void setTrainingBonus(float trainingBonus) {
        this.breathingTrainingDayBonus = trainingBonus;
    }
    
    public void breathingTrainingDay(PlayerEntity user) {
        World world = user.level;
        if (!world.isClientSide()) {
            float lvlInc = (2 * MathHelper.clamp(getAverageExercisePoints(), 0F, 1F)) - 1F;
            recalcAvgExercisePoints();
            if (lvlInc < 0) {
                if (!JojoModConfig.getCommonConfigInstance(false).breathingTrainingDeterioration.get() || user.abilities.instabuild) {
                    lvlInc = 0;
                }
                else {
                    lvlInc *= 0.25F;
                }
                breathingTrainingDayBonus = 0;
            }
            else {
                float bonus = breathingTrainingDayBonus;
                breathingTrainingDayBonus += lvlInc * 0.25F;
                lvlInc = multiplyPositiveBreathingTraining(lvlInc + bonus);
            }
            setBreathingLevel(getBreathingLevel() + lvlInc);
            avgExercisePoints = 0;
            if (isSkillLearned(ModHamonSkills.CHEAT_DEATH.get())) {
                HamonPowerType.updateCheatDeathEffect(power.getUser());
            }
        }
        for (Exercise exercise : exerciseTicks.keySet()) {
            setExerciseValue(exercise, 0, world.isClientSide());
            avgExercisePoints = 0;
        }
        updateExerciseAttributes(user);
    }
    
    public float getAverageExercisePoints() {
        return avgExercisePoints;
    }
    
    
    
    public boolean isSkillLearned(AbstractHamonSkill skill) {
        return hamonSkills.containsSkill(skill);
    }
    
    public ActionConditionResult canLearnSkillTeacherIrrelevant(LivingEntity user, AbstractHamonSkill skill) {
        return hamonSkills.canLearnSkill(user, this, skill);
    }
    
    public ActionConditionResult canLearnSkill(LivingEntity user, AbstractHamonSkill skill, @Nullable Collection<? extends AbstractHamonSkill> teachersSkills) {
        return hamonSkills.canLearnSkill(user, this, skill, teachersSkills);
    }

    public boolean addHamonSkill(LivingEntity user, AbstractHamonSkill skill, boolean checkRequirements, boolean sync) {
        if (!checkRequirements || !isSkillLearned(skill) && canLearnSkill(user, skill, HamonPowerType.nearbyTeachersSkills(power.getUser())).isPositive()) {
            hamonSkills.addSkill(skill);
            addSkillAction(skill);
            serverPlayer.ifPresent(player -> {
                if (skill == ModHamonSkills.CHEAT_DEATH.get()) {
                    HamonPowerType.updateCheatDeathEffect(player);
                }
                else if (skill == ModHamonSkills.SATIPOROJA_SCARF.get()) {
                    player.addItem(new ItemStack(ModItems.SATIPOROJA_SCARF.get()));
                }
                if (sync) {
                    PacketManager.sendToClient(new HamonSkillAddPacket(skill), (ServerPlayerEntity) player);
                    hamonSkills.getTechniqueData().updateTechnique(user);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void updateExtraActions() {
        HamonTechniqueManager data = hamonSkills.getTechniqueData();
        if (data.getTechnique() != null) {
            data.getTechnique().getPerksOnPick().forEach(techniquePerk -> {
                addSkillAction(techniquePerk);
            });
        }
        for (AbstractHamonSkill techniqueSkill : data.getLearnedSkills()) {
            addSkillAction(techniqueSkill);
        }
    }

    private void addSkillAction(AbstractHamonSkill skill) {
        if (skill.getRewardAction() != null && skill.addsActionToHUD()) {
            ActionType hotbar = skill.getRewardType().getActionType();
            if (hotbar != null) {
                power.getActions(hotbar).addExtraAction(skill.getRewardAction());
            }
        }
    }
    
    public void removeHamonSkill(AbstractHamonSkill skill) {
        if (!skill.isUnlockedByDefault() && isSkillLearned(skill)) {
            hamonSkills.removeSkill(skill);
            removeSkillAction(skill);
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClient(new HamonSkillRemovePacket(skill), player);
                if (skill == ModHamonSkills.CHEAT_DEATH.get()) {
                    player.removeEffect(ModEffects.CHEAT_DEATH.get());
                }
                hamonSkills.getTechniqueData().updateTechnique(player);
            });
        }
    }

    private void removeSkillAction(AbstractHamonSkill skill) {
        if (skill.getRewardAction() != null && skill.addsActionToHUD()) {
            ActionType hotbar = skill.getRewardType().getActionType();
            if (hotbar != null) {
                power.getActions(hotbar).removeAction(skill.getRewardAction());
            }
        }
    }

    public void resetHamonSkills(LivingEntity user, HamonSkillsTab type) {
        Stream<? extends AbstractHamonSkill> toReset;
        switch (type) {
        case STRENGTH:
            toReset = JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues().stream()
                    .filter(skill -> skill instanceof BaseHamonSkill && ((BaseHamonSkill) skill).getStat() == HamonStat.STRENGTH);
            break;
        case CONTROL:
            toReset = JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues().stream()
                    .filter(skill -> skill instanceof BaseHamonSkill && ((BaseHamonSkill) skill).getStat() == HamonStat.CONTROL);
            break;
        case TECHNIQUE:
            toReset = JojoCustomRegistries.HAMON_SKILLS.getRegistry().getValues().stream()
                    .filter(skill -> !skill.isBaseSkill());
            break;
        default:
            toReset = Stream.empty();
            break;
        }
        toReset.forEach(this::removeHamonSkill);
    }
    
    public Iterable<AbstractHamonSkill> getLearnedSkills() {
        return hamonSkills.getLearnedSkills();
    }
    
    
    
    @Nullable
    public CharacterHamonTechnique getCharacterTechnique() {
        return hamonSkills.getTechniqueData().getTechnique();
    }
    
    public boolean characterIs(CharacterHamonTechnique character) {
        return getCharacterTechnique() == character;
    }
    
    public boolean hasTechniqueLevel(int techniqueSkillSlot) {
        if (techniqueSkillSlot > HamonTechniqueManager.MAX_TECHNIQUE_SKILLS) {
            return false;
        }
        return getHamonStrengthLevel() >= HamonTechniqueManager.techniqueSkillRequirement(techniqueSkillSlot)
                && getHamonControlLevel() >= HamonTechniqueManager.techniqueSkillRequirement(techniqueSkillSlot);
    }
    
    public HamonTechniqueManager.Accessor getTechniqueData() {
        return new HamonTechniqueManager.Accessor(hamonSkills.getTechniqueData());
    }
    
    
    
    public void addNewPlayerLearner(PlayerEntity player) {
        newLearners.add(player);
        LivingEntity user = power.getUser();
        if (user instanceof PlayerEntity) {
            ((PlayerEntity) user).displayClientMessage(new TranslationTextComponent("jojo.chat.message.new_hamon_learner", player.getDisplayName()), true);
        }
    }
    
    private void tickNewPlayerLearners(LivingEntity user) {
        for (Iterator<PlayerEntity> it = newLearners.iterator(); it.hasNext(); ) {
            PlayerEntity player = it.next();
            if (user.distanceToSqr(player) > 64) {
                it.remove();
            }
        }
    }
    
    public void interactWithNewLearner(PlayerEntity player) {
        if (newLearners.contains(player)) {
            HamonPowerType.startLearningHamon(player.level, player, INonStandPower.getPlayerNonStandPower(player), power.getUser(), this);
            newLearners.remove(player);
        }
    }
    
    
    
    private static final Map<HamonAuraColor, Supplier<? extends IParticleData>> PARTICLE_TYPE = Util.make(new HashMap<>(), map -> {
        map.put(HamonAuraColor.ORANGE, ModParticles.HAMON_AURA);
        map.put(HamonAuraColor.BLUE, ModParticles.HAMON_AURA_BLUE);
        map.put(HamonAuraColor.YELLOW, ModParticles.HAMON_AURA_YELLOW);
        map.put(HamonAuraColor.RED, ModParticles.HAMON_AURA_RED);
        map.put(HamonAuraColor.SILVER, ModParticles.HAMON_AURA_SILVER);
    });
    
    private HamonAuraColor auraColor = HamonAuraColor.ORANGE;
    private Action<?> lastUsedAction = null;
    
    private void tickChargeParticles() {
        LivingEntity user = power.getUser();
        HamonAuraColor auraColor = getThisTickAuraColor(user);
        if (auraColor != this.auraColor) {
            this.auraColor = auraColor;
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTracking(new TrHamonAuraColorPacket(user.getId(), auraColor), user);
            }
        }
        if (user.level.isClientSide()) {
            float particlesPerTick = power.getEnergy() / power.getMaxEnergy() * getHamonDamageMultiplier();
            boolean isUserTheCameraEntity = user == ClientUtil.getClientPlayer();
            IParticleData particleType = PARTICLE_TYPE.get(auraColor).get();
            
            GeneralUtil.doFractionTimes(() -> {
                CustomParticlesHelper.createHamonAuraParticle(particleType, user, 
                        user.getX() + (random.nextDouble() - 0.5) * (user.getBbWidth() + 0.5F), 
                        user.getY() + random.nextDouble() * (user.getBbHeight() * 0.5F), 
                        user.getZ() + (random.nextDouble() - 0.5) * (user.getBbWidth() + 0.5F));
            }, particlesPerTick);
            if (isUserTheCameraEntity) {
                CustomParticlesHelper.summonHamonAuraParticlesFirstPerson(particleType, user, particlesPerTick);
            }
        }
    }
    
    private HamonAuraColor getThisTickAuraColor(LivingEntity user) {
        if (power.getEnergy() == 0) {
             lastUsedAction = null;
        }
        else {
            if (lastUsedAction == ModHamonActions.HAMON_TURQUOISE_BLUE_OVERDRIVE.get()) {
                return HamonAuraColor.BLUE;
            }
            if (lastUsedAction == ModHamonActions.HAMON_SUNLIGHT_YELLOW_OVERDRIVE.get()) {
                return HamonAuraColor.YELLOW;
            }
            if (lastUsedAction == ModHamonActions.JONATHAN_SCARLET_OVERDRIVE.get()) {
                return HamonAuraColor.RED;
            }
            if (lastUsedAction == ModHamonActions.JONATHAN_METAL_SILVER_OVERDRIVE.get()) {
                return HamonAuraColor.SILVER;
            }
        }
        
        if (isSkillLearned(ModHamonSkills.METAL_SILVER_OVERDRIVE.get()) && HamonMetalSilverOverdrive.itemUsesMSO(user)) {
            return HamonAuraColor.SILVER;
        }
        
        if (isSkillLearned(ModHamonSkills.TURQUOISE_BLUE_OVERDRIVE.get()) && user.isUnderWater()) {
            return HamonAuraColor.BLUE;
        }
        
        return HamonAuraColor.ORANGE;
    }
    
    public void setLastUsedAction(@Nullable Action<?> action) {
        this.lastUsedAction = action;
    }
    
    public void setAuraColor(HamonAuraColor color) {
        this.auraColor = color;
    }
    
    
    
    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("StrengthPoints", hamonStrengthPoints);
        nbt.putInt("ControlPoints", hamonControlPoints);
        nbt.putFloat("BreathingTechnique", breathingTrainingLevel);
        nbt.put("Skills", hamonSkills.toNBT());
        CompoundNBT exercises = new CompoundNBT();
        for (Exercise exercise : Exercise.values()) {
            exercises.putInt(exercise.toString(), Math.min(exerciseTicks.get(exercise), exercise.getMaxTicks(this)));
        }
        nbt.put("Exercises", exercises);
        nbt.putFloat("TrainingBonus", breathingTrainingDayBonus);
        nbt.putFloat("BreathStability", breathStability);
        nbt.putInt("EnergyTicks", noEnergyDecayTicks);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        hamonStrengthPoints = nbt.getInt("StrengthPoints");
        hamonStrengthLevel = levelFromPoints(hamonStrengthPoints);
        hamonControlPoints = nbt.getInt("ControlPoints");
        hamonControlLevel = levelFromPoints(hamonControlPoints);
        breathingTrainingLevel = nbt.getFloat("BreathingTechnique");
        recalcHamonDamage();
        hamonSkills.fromNbt(nbt.getCompound("Skills"));
        CompoundNBT exercises = nbt.getCompound("Exercises");
        int[] exercisesNbt = new int[4];
        for (Exercise exercise : Exercise.values()) {
            exercisesNbt[exercise.ordinal()] = exercises.getInt(exercise.toString());
        }
        setExerciseTicks(exercisesNbt[0], exercisesNbt[1], exercisesNbt[2], exercisesNbt[3], false);
        breathingTrainingDayBonus = nbt.getFloat("TrainingBonus");
        breathStability = nbt.contains("BreathStability") ? nbt.getFloat("BreathStability") : getMaxBreathStability();
        prevBreathStability = breathStability;
        noEnergyDecayTicks = nbt.getInt("EnergyTicks");
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        giveBreathingTrainingBuffs(user);
        updateExerciseAttributes(user);
        hamonSkills.syncWithUser(user, this);
        PacketManager.sendToClient(new HamonExercisesPacket(this), user);
        ModCriteriaTriggers.HAMON_STATS.get().trigger(user, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrHamonStatsPacket(
                user.getId(), false, getHamonStrengthPoints(), getHamonControlPoints(), getBreathingLevel()), entity);
        PacketManager.sendToClient(new TrHamonBreathStabilityPacket(user.getId(), getBreathStability()), entity);
        PacketManager.sendToClient(new TrHamonEnergyTicksPacket(user.getId(), noEnergyDecayTicks), entity);
        hamonSkills.syncWithTrackingOrUser(user, entity, this);
        PacketManager.sendToClient(new TrHamonAuraColorPacket(user.getId(), auraColor), entity);
    }
    
    public enum Exercise {
        MINING(150),
        RUNNING(135),
        SWIMMING(135),
        MEDITATION(60);

        private final float maxTicks;

        private Exercise(float seconds) {
            this.maxTicks = seconds * 20F;
        }
        
        public int getMaxTicks(@Nullable HamonData hamon) {
            float multiplier = hamon != null ? (MAX_BREATHING_LEVEL - hamon.getBreathingLevel()) / MAX_BREATHING_LEVEL * 0.75F + 0.25F : 1;
            return MathHelper.floor(maxTicks * multiplier);
        }
    }
}
