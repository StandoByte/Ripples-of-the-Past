package com.github.standobyte.jojo.power.nonstand.type.hamon;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillAddPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillRemovePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonStatsPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.HamonSkillType;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.HamonStat;
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
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.management.PlayerInteractionManager;
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
    
    private float breathingTrainingLevel;
    private float breathingTrainingDayBonus;
    private float hamonDamageFactor = 1F;
    private EnumMap<Exercise, Integer> exerciseTicks = new EnumMap<Exercise, Integer>(Exercise.class);
    private float avgExercisePoints;
    
    private HamonSkillSet hamonSkills;
    
    private boolean isMeditating;
    private int meditationTicks;
    
    private Set<PlayerEntity> newLearners = new HashSet<>();

    public HamonData() {
        hamonSkills = new HamonSkillSet();
        for (Exercise exercise : Exercise.values()) {
            exerciseTicks.put(exercise, 0);
        }
    }
    
    public void tick() {
        if (!power.getUser().level.isClientSide()) {
            tickNewPlayerLearners(power.getUser());
        }
    }
    
    public float tickEnergy() {
        float energyGain = 0;
        LivingEntity user = power.getUser();
        if (user.getAirSupply() >= user.getMaxAirSupply()) {
            energyGain = (1F + getHamonControlLevel() * 0.033F) * (1F + (int) getBreathingLevel() * 0.08F);
        }
        return power.getEnergy() + energyGain;
    }

    public float getMaxEnergy() {
        return NonStandPower.BASE_MAX_ENERGY * (1F + getHamonControlLevel() * 0.033F);
    }
    
    public static final float ALL_EXERCISES_EFFICIENCY_MULTIPLIER = 1.05F;
    public float getHamonEfficiency() {
        float efficiency = getBloodstreamEfficiency();
        if (allExercisesCompleted) {
            efficiency *= ALL_EXERCISES_EFFICIENCY_MULTIPLIER;
        }
        return efficiency;
    }

    public float getHamonDamageMultiplier() {
        return hamonDamageFactor;
    }
    
    
    
    private static final double STR_EXP_SCALING = 1.0333;
    private static final double BRTH_SCALING = 0.04;
    private static float dmgFormula(float strength, float breathingTraining) {
        return (float) (Math.pow(STR_EXP_SCALING, strength) * (1 + BRTH_SCALING * breathingTraining));
    }
    
    public static float reduceEnergyConsumed(float amount, INonStandPower power, LivingEntity user) {
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
        if (action == ModHamonActions.HAMON_SUNLIGHT_YELLOW_OVERDRIVE.get()) {
            return hamonSkills.isSkillLearned(HamonSkill.SUNLIGHT_YELLOW_OVERDRIVE);
        }
        return action == ModHamonActions.HAMON_OVERDRIVE.get() || action == ModHamonActions.HAMON_HEALING.get() || hamonSkills.unlockedActions.contains(action);
    }
    
    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType) {
        hamonSkills.addSkill(HamonSkill.OVERDRIVE);
        hamonSkills.addSkill(HamonSkill.HEALING);
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
            spentPoints = hamonSkills.getSpentStrengthPoints();
            break;
        case CONTROL:
            spentPoints = hamonSkills.getSpentControlPoints();
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
        if (isSkillLearned(HamonSkill.NATURAL_TALENT)) {
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
    public static final float SWIMMING_COMPLETED_BREATH_STABILITY_TIME_MULTIPLIER = 0.8F;
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
        
        if (isMeditating) {
            if (++meditationTicks >= 40) {
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
            if (isSkillLearned(HamonSkill.NATURAL_TALENT)) {
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
            if (isSkillLearned(HamonSkill.CHEAT_DEATH)) {
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
    
    
    
    public Set<HamonSkill> getSkillSetImmutable() {
        return Collections.unmodifiableSet(hamonSkills.wrappedSkillSet);
    }
    
    public boolean isSkillLearned(HamonSkill skill) {
        return hamonSkills.isSkillLearned(skill);
    }

    public ActionConditionResult canLearnSkillTeacherIrrelevant(LivingEntity user, HamonSkill skill) {
        return canLearnSkill(user, skill, false, null);
    }
    
    public ActionConditionResult canLearnSkill(LivingEntity user, HamonSkill skill, boolean isTeacherNearby, @Nullable Collection<HamonSkill> teachersSkills) {
        if (!hamonSkills.parentsLearned(skill)) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.parents"));
        }
        if (!haveSkillPoints(skill)) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.points"));
        }
        if (skill.getTechnique() != null) {
            if (!canLearnNewTechniqueSkill()) {
                return ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.technique.locked"));
            }
            if (!rightTechnique(skill)) {
                return ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.technique.bug"));
            }
        }
        if (teachersSkills != null && skill.requiresTeacher()) {
            if (!isTeacherNearby) {
                return ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.teacher.required"));
            }
            else if (!teachersSkills.contains(skill)) {
                return ActionConditionResult.createNegative(new TranslationTextComponent("hamon.closed.teacher.no_skill"));
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    private boolean haveSkillPoints(HamonSkill skill) {
        return skill.getStat() == null || getSkillPoints(skill.getStat()) > 0;
    }

    public boolean addHamonSkill(LivingEntity user, HamonSkill skill, boolean checkRequirements, boolean sync) {
        if (!checkRequirements || !isSkillLearned(skill) && canLearnSkill(user, skill, true, HamonPowerType.nearbyTeachersSkills(power.getUser())).isPositive()) {
            hamonSkills.addSkill(skill);
            addSkillAction(skill);
            serverPlayer.ifPresent(player -> {
                if (skill == HamonSkill.CHEAT_DEATH) {
                    HamonPowerType.updateCheatDeathEffect(player);
                }
                else if (skill == HamonSkill.SATIPOROJA_SCARF) {
                    player.addItem(new ItemStack(ModItems.SATIPOROJA_SCARF.get()));
                }
                if (sync) {
                    PacketManager.sendToClient(new HamonSkillAddPacket(skill), (ServerPlayerEntity) player);
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void updateExtraActions() {
        for (HamonSkill skill : getSkillSetImmutable()) {
            if (skill.getTechnique() != null) {
                addSkillAction(skill);
            }
        }
    }

    private void addSkillAction(HamonSkill skill) {
        if (skill.getRewardAction() != null && !skill.isBaseSkill()) {
            power.getActions(skill.getRewardType().getActionType()).addExtraAction(skill.getRewardAction());
        }
    }
    
    public void removeHamonSkill(HamonSkill skill) {
        if (!skill.isUnlockedByDefault() && isSkillLearned(skill)) {
            hamonSkills.removeSkill(skill);
            removeSkillAction(skill);
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClient(new HamonSkillRemovePacket(skill), player);
                if (skill == HamonSkill.CHEAT_DEATH) {
                    player.removeEffect(ModEffects.CHEAT_DEATH.get());
                }
            });
        }
    }

    private void removeSkillAction(HamonSkill skill) {
        if (skill.getRewardAction() != null && !skill.isBaseSkill()) {
            power.getActions(skill.getRewardType().getActionType()).removeAction(skill.getRewardAction());
        }
    }

    public void resetHamonSkills(LivingEntity user, HamonSkillType type) {
        for (HamonSkill skill : HamonSkill.values()) {
            if (!skill.isUnlockedByDefault() && skill.getSkillType() == type && isSkillLearned(skill)) {
                removeHamonSkill(skill);
            }
        }
    }
    
    
    
    public HamonSkill.Technique getTechnique() {
        return hamonSkills.technique;
    }
    
    public boolean hasTechniqueLevel(int techniqueSkillSlot) {
        if (techniqueSkillSlot >= HamonSkillSet.MAX_TECHNIQUE_SKILLS) {
            return false;
        }
        return getHamonStrengthLevel() >= HamonSkillSet.techniqueLevelReq(techniqueSkillSlot)
                && getHamonControlLevel() >= HamonSkillSet.techniqueLevelReq(techniqueSkillSlot);
    }
    
    public boolean canLearnNewTechniqueSkill() {
        return hasTechniqueLevel(hamonSkills.techniqueSkillsLearned);
    }
    
    private boolean rightTechnique(HamonSkill skill) {
        return skill.getTechnique() == null || hamonSkills.technique == null || skill.getTechnique() == hamonSkills.technique;
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

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("StrengthPoints", hamonStrengthPoints);
        nbt.putInt("ControlPoints", hamonControlPoints);
        nbt.putFloat("BreathingTechnique", breathingTrainingLevel);
        CompoundNBT skillsMapNbt = new CompoundNBT();
        for (HamonSkill skill : HamonSkill.values()) {
            skillsMapNbt.putBoolean(skill.getName(), hamonSkills.isSkillLearned(skill));
        }
        nbt.put("Skills", skillsMapNbt);
        CompoundNBT exercises = new CompoundNBT();
        for (Exercise exercise : Exercise.values()) {
            exercises.putInt(exercise.toString(), Math.min(exerciseTicks.get(exercise), exercise.getMaxTicks(this)));
        }
        nbt.put("Exercises", exercises);
        nbt.putFloat("TrainingBonus", breathingTrainingDayBonus);
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
        fillSkillsFromNbt(nbt.getCompound("Skills"));
        CompoundNBT exercises = nbt.getCompound("Exercises");
        int[] exercisesNbt = new int[4];
        for (Exercise exercise : Exercise.values()) {
            exercisesNbt[exercise.ordinal()] = exercises.getInt(exercise.toString());
        }
        setExerciseTicks(exercisesNbt[0], exercisesNbt[1], exercisesNbt[2], exercisesNbt[3], false);
        breathingTrainingDayBonus = nbt.getFloat("TrainingBonus");
    }

    private void fillSkillsFromNbt(CompoundNBT nbt) {
        for (HamonSkill skill : HamonSkill.values()) {
            if (nbt.contains(skill.getName()) && nbt.getBoolean(skill.getName())) {
                hamonSkills.addSkill(skill);
            }
        }
    }

    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        giveBreathingTrainingBuffs(user);
        for (HamonSkill skill : HamonSkill.values()) {
            if (isSkillLearned(skill)) {
                PacketManager.sendToClient(new HamonSkillAddPacket(skill), user);
            }
        }
        updateExerciseAttributes(user);
        PacketManager.sendToClient(new HamonExercisesPacket(this), user);
        ModCriteriaTriggers.HAMON_STATS.get().trigger(user, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
    }

    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrHamonStatsPacket(
                user.getId(), false, getHamonStrengthPoints(), getHamonControlPoints(), getBreathingLevel()), entity);
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
