package com.github.standobyte.jojo.power.nonstand.type;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillLearnPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillsResetPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncHamonStatsPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonSkillType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class HamonData extends TypeSpecificData {
    public static final int MAX_STAT_LEVEL = 60;
    public static final float MAX_BREATHING_LEVEL = 100;
    public static final int MIN_BREATHING_EXCEED = 5;
    
    private static final int LVL_1_POINTS = 2;
    private static final int NEXT_LVL_DIFF = 3;
    public static final int MAX_HAMON_POINTS = pointsAtLevel(MAX_STAT_LEVEL);
    
    private final Random random = new Random();

    private int hamonStrengthPoints;
    private int hamonStrengthLevel;
    private int hamonControlPoints;
    private int hamonControlLevel;
    private float breathingTechniqueLevel;
    private float breathingTrainingBonus;
    private float hamonDamageFactor = 1F;
    private HamonSkillSet hamonSkills;
    private EnumMap<Exercise, Integer> exerciseTicks = new EnumMap<Exercise, Integer>(Exercise.class);
    private long lastTickedDay = -1;
    private boolean isMeditating;
    private Vector3d meditationPosition;
    private float meditationYRot;
    private float meditationXRot;
    private float avgExercisePoints;
    private Set<PlayerEntity> newLearners = new HashSet<>();

    public HamonData() {
        hamonSkills = new HamonSkillSet();
        for (Exercise exercise : Exercise.values()) {
            exerciseTicks.put(exercise, 0);
        }
    }
    
    public void tick() {
        tickNewPlayerLearners(power.getUser());
    }

    @Override
    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower powerData) {
        if (action == ModActions.HAMON_SUNLIGHT_YELLOW_OVERDRIVE.get()) {
            return hamonSkills.isSkillLearned(HamonSkill.SUNLIGHT_YELLOW_OVERDRIVE);
        }
        return action == ModActions.HAMON_OVERDRIVE.get() || action == ModActions.HAMON_HEALING.get() || hamonSkills.unlockedActions.contains(action);
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
                PacketManager.sendToClientsTrackingAndSelf(new TrSyncHamonStatsPacket(player.getId(), true, stat, newPoints), player);
                ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTechniqueLevel);
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

    public float getHamonDamageMultiplier() {
        float reducingFactor = 1;
        LivingEntity user = power.getUser();
        float healthRatio = user.getHealth() / user.getMaxHealth();
        if (healthRatio < 0.5F) {
            reducingFactor *= healthRatio * 1.5F + 0.25F;
        }
        EffectInstance freezeEffect = power.getUser().getEffect(ModEffects.FREEZE.get());
        if (freezeEffect != null) {
            reducingFactor *= Math.max(1 - (freezeEffect.getAmplifier() + 1) * 0.2F, 0);
        }
        return hamonDamageFactor * reducingFactor;
    }

    public static final float MAX_HAMON_DAMAGE = (float) (Math.pow(1.03, MAX_STAT_LEVEL) * (1 + 0.05 * MAX_BREATHING_LEVEL)); // 35.34962
    private void recalcHamonDamage() {
        hamonDamageFactor = (float) (Math.pow(1.03, hamonStrengthLevel) * (1 + 0.05 * breathingTechniqueLevel));
    }

    public float getEnergyLimitFactor() {
        return 1F + getHamonControlLevel() * 0.033F;
    }

    public float getEnergyRegenPoints() {
        return (1F + getHamonControlLevel() * 0.033F) * (1F + (int) getBreathingLevel() * 0.08F);
    }

    private static final float ENERGY_PER_POINT = 500F;
    public void hamonPointsFromAction(HamonStat stat, float energyCost) {
        if (isSkillLearned(HamonSkill.NATURAL_TALENT)) {
            energyCost *= 2;
        }
        energyCost *= JojoModConfig.COMMON.hamonPointsMultiplier.get().floatValue();
        int points = (int) (energyCost / ENERGY_PER_POINT);
        if (random.nextFloat() < (energyCost % ENERGY_PER_POINT) / ENERGY_PER_POINT) points++;
        setHamonStatPoints(stat, getStatPoints(stat) + points, false, false);
    }

    public float getBreathingLevel() {
        return breathingTechniqueLevel;
    }

    public void setBreathingLevel(float level) {
        float oldLevel = breathingTechniqueLevel;
        breathingTechniqueLevel = MathHelper.clamp(level, 0, MAX_BREATHING_LEVEL);
        if (oldLevel != breathingTechniqueLevel) {
            recalcHamonDamage();
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrSyncHamonStatsPacket(player.getId(), true, getBreathingLevel()), player);
                ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTechniqueLevel);
            });
        }
        giveBreathingTechniqueBuffs(power.getUser());
    }

    private static final AttributeModifier ATTACK_DAMAGE = new AttributeModifier(
            UUID.fromString("8dcb2ad7-6067-4615-b7b6-af5256537c10"), "Attack damage from Hamon Training", 0.01D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier ATTACK_SPEED = new AttributeModifier(
            UUID.fromString("995b2915-9053-472c-834c-f94251e81659"), "Attack speed from Hamon Training", 0.03D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MOVEMENT_SPEED = new AttributeModifier(
            UUID.fromString("ffa9ba4e-3811-44f7-a4a9-887ffbd47390"), "Movement speed from Hamon Training", 0.0005D, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier SWIMMING_SPEED = new AttributeModifier(
            UUID.fromString("34dcb563-6759-4a2b-9dd8-ad2dd7e70404"), "Swimming speed from Hamon Training", 0.01D, AttributeModifier.Operation.ADDITION);

    private void giveBreathingTechniqueBuffs(LivingEntity entity) {
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

    public int getExerciseTicks(Exercise exercise) {
        return exerciseTicks.get(exercise);
    }

    public void incExerciseTicks(Exercise exercise, float multiplier) {
        int ticks = exerciseTicks.get(exercise);
        if (ticks < exercise.maxTicks) {
            int inc = 1;
            if (multiplier > 1F) {
                inc = MathHelper.floor(multiplier);
                if (random.nextFloat() < MathHelper.frac(multiplier)) inc++;
            }
            inc = Math.min(inc, exercise.maxTicks - ticks);
            exerciseTicks.put(exercise, ticks + inc);
            avgExercisePoints += (double) inc / exercise.maxTicks / exerciseTicks.size();
        }
    }

    public void setExerciseTicks(int mining, int running, int swimming, int meditation) {
        exerciseTicks.put(Exercise.MINING, mining);
        exerciseTicks.put(Exercise.RUNNING, running);
        exerciseTicks.put(Exercise.SWIMMING, swimming);
        exerciseTicks.put(Exercise.MEDITATION, meditation);
        avgExercisePoints = (
                (float)mining / Exercise.MINING.maxTicks + 
                (float)running / Exercise.RUNNING.maxTicks + 
                (float)swimming / Exercise.SWIMMING.maxTicks + 
                (float)meditation / Exercise.MEDITATION.maxTicks) 
                / exerciseTicks.size();
    }

    public void startMeditating(Vector3d position, float headYRot, float headXRot) {
        this.isMeditating = true;
        this.meditationPosition = position;
        this.meditationYRot = headYRot;
        this.meditationXRot = headXRot;
    }

    public boolean updateMeditation(Vector3d position, float headYRot, float headXRot) {
        if (isMeditating) {
            isMeditating = meditationPosition.closerThan(position, 0.1D)
                    && (headYRot - meditationYRot) < 1F
                    && (headXRot - meditationXRot) < 1F;
            if (isMeditating) {
                meditationPosition = position;
                meditationYRot = headYRot;
                meditationXRot = headXRot;
            }
        }
        return isMeditating;
    }
    
    public float getTrainingBonus() {
        return breathingTrainingBonus;
    }
    
    public void setTrainingBonus(float trainingBonus) {
        this.breathingTrainingBonus = trainingBonus;
    }

    public void newDayCheck(World world) {
        long day = world.getDayTime() / 24000;
        long prevDay = lastTickedDay;
        lastTickedDay = day;
        if (prevDay == -1 || prevDay == day) {
            return;
        }
        float lvlInc = (2 * MathHelper.clamp(getAverageExercisePoints(), 0F, 1F)) - 1F;
        if (lvlInc < 0) {
            if (!JojoModConfig.COMMON.breathingTechniqueDeterioration.get()) {
                lvlInc = 0;
            }
            else {
                lvlInc *= 0.25F;
            }
            breathingTrainingBonus = 0;
        }
        else {
            float bonus = breathingTrainingBonus;
            breathingTrainingBonus += lvlInc * 0.25F;
            lvlInc += bonus;
            lvlInc *= JojoModConfig.COMMON.breathingTechniqueMultiplier.get().floatValue();
            if (isSkillLearned(HamonSkill.NATURAL_TALENT)) {
                lvlInc *= 2;
            }
        }
        setBreathingLevel(getBreathingLevel() + lvlInc);
        avgExercisePoints = 0;
        for (Exercise exercise : exerciseTicks.keySet()) {
            exerciseTicks.put(exercise, 0);
        }
        if (isSkillLearned(HamonSkill.CHEAT_DEATH)) {
            HamonPowerType.updateCheatDeathEffect(power.getUser());
        }
    }

    public float getAverageExercisePoints() {
        return avgExercisePoints;
    }



    public Set<HamonSkill> getSkillSetImmutable() {
        return Collections.unmodifiableSet(hamonSkills.wrappedSkillSet);
    }

    public HamonSkill.Technique getTechnique() {
        return hamonSkills.technique;
    }

    public boolean isSkillLearned(HamonSkill skill) {
        return hamonSkills.isSkillLearned(skill);
    }

    public boolean canLearnSkill(HamonSkill skill, @Nullable Set<HamonSkill> teachersSkills) {
        return hamonSkills.parentsLearned(skill) && haveSkillPoints(skill) && techniqueSkillRequirement(skill) 
                && rightTechnique(skill) && (teachersSkills == null || !skill.requiresTeacher() || teachersSkills.contains(skill));
    }

    public String skillClosedReason(HamonSkill skill, boolean isTeacherNearby, Set<HamonSkill> teachersSkills) {
        if (!hamonSkills.parentsLearned(skill)) {
            return "hamon.closed.parents";
        }
        if (!haveSkillPoints(skill)) {
            return "hamon.closed.points";
        }
        if (!techniqueSkillRequirement(skill)) {
            return "hamon.closed.technique.locked";
        }
        if (!(skill.getTechnique() == null) && hamonSkills.technique != null && skill.getTechnique() != hamonSkills.technique) {
            return "hamon.closed.technique.bug";
        }
        if (skill.requiresTeacher()) {
            if (!isTeacherNearby) {
                return "hamon.closed.teacher.required";
            }
            else if (!teachersSkills.contains(skill)) {
                return "hamon.closed.teacher.no_skill";
            }
        }
        return "";
    }

    private boolean haveSkillPoints(HamonSkill skill) {
        return skill.getStat() == null || getSkillPoints(skill.getStat()) > 0;
    }

    private boolean techniqueSkillRequirement(HamonSkill skill) {
        if (!(skill.getTechnique() == null)) {
            return haveTechniqueLevel();
        }
        return true;
    }

    public boolean haveTechniqueLevel() {
        return hamonSkills.techniqueSkillsLearned < 3 && 
                getHamonStrengthLevel() >= hamonSkills.getTechniqueLevelReq()
                && getHamonControlLevel() >= hamonSkills.getTechniqueLevelReq();
    }
    
    public boolean techniquesUnlocked() {
        return getHamonStrengthLevel() >= HamonSkillSet.TECHNIQUE_MINIMAL_STAT_LVL
                && getHamonControlLevel() >= HamonSkillSet.TECHNIQUE_MINIMAL_STAT_LVL;
    }

    private boolean rightTechnique(HamonSkill skill) {
        return skill.getTechnique() == null || hamonSkills.technique == null || skill.getTechnique() == hamonSkills.technique;
    }

    public boolean learnHamonSkill(HamonSkill skill, boolean checkRequirements) {
        if (!isSkillLearned(skill) && (!checkRequirements || canLearnSkill(skill, HamonPowerType.nearbyTeachersSkills(power.getUser())))) {
            hamonSkills.addSkill(skill);
            onSkillAdded(skill);
            serverPlayer.ifPresent(player -> {
                switch (skill) {
                case CHEAT_DEATH:
                    HamonPowerType.updateCheatDeathEffect(player);
                    break;
                case SATIPOROJA_SCARF:
                    player.addItem(new ItemStack(ModItems.SATIPOROJA_SCARF.get()));
                    break;
                default:
                    break;
                }
                PacketManager.sendToClient(new HamonSkillLearnPacket(skill), (ServerPlayerEntity) player);
            });
            return true;
        }
        return false;
    }
    
    private void onSkillAdded(HamonSkill skill) {
        if (skill.getRewardAction() != null) {
            if (skill.getTechnique() != null) {
                if (skill.getTechnique() == getTechnique()) {
                    switch (skill.getRewardType()) {
                    case ATTACK:
                        power.getAttacks().add(skill.getRewardAction());
                        break;
                    case ABILITY:
                        power.getAbilities().add(skill.getRewardAction());
                        break;
                    default:
                        break;
                    }
                }
            }
            else if (skill == HamonSkill.ANIMAL_INFUSION) {
                List<Action<INonStandPower>> attacks = power.getAttacks();
                int index = attacks.indexOf(HamonSkill.PLANT_INFUSION.getRewardAction());
                if (index > -1) {
                    attacks.set(index, HamonSkill.ANIMAL_INFUSION.getRewardAction());
                }
            }
        }
    }
    
    public void resetHamonSkills(HamonSkillType type) {
        for (HamonSkill skill : HamonSkill.values()) {
            if (skill != HamonSkill.OVERDRIVE && skill != HamonSkill.HEALING && skill.getSkillType() == type && isSkillLearned(skill)) {
                hamonSkills.removeSkill(skill);
                onSkillRemoved(skill);
            }
        }
        serverPlayer.ifPresent(player -> {
            PacketManager.sendToClient(new HamonSkillsResetPacket(type), (ServerPlayerEntity) player);
        });
    }
    
    private void onSkillRemoved(HamonSkill skill) {
        if (skill.getRewardAction() != null) {
            if (skill.getTechnique() != null) {
                switch (skill.getRewardType()) {
                case ATTACK:
                    power.getAttacks().remove(skill.getRewardAction());
                    break;
                case ABILITY:
                    power.getAbilities().remove(skill.getRewardAction());
                    break;
                default:
                    break;
                }
            }
            else if (skill == HamonSkill.ANIMAL_INFUSION) {
                List<Action<INonStandPower>> attacks = power.getAttacks();
                int index = attacks.indexOf(HamonSkill.ANIMAL_INFUSION.getRewardAction());
                if (index > -1) {
                    attacks.set(index, HamonSkill.PLANT_INFUSION.getRewardAction());
                }
            }
        }
    }
    
    public SoundEvent getBreathingSound() {
        Technique technique = getTechnique();
        if (technique == null) {
            return ModSounds.BREATH_DEFAULT.get();
        }
        switch (getTechnique()) {
        case JONATHAN:
            return ModSounds.BREATH_JONATHAN.get();
        case ZEPPELI:
            return ModSounds.BREATH_ZEPPELI.get();
        case JOSEPH:
            return ModSounds.BREATH_JOSEPH.get();
        case CAESAR:
            return ModSounds.BREATH_CAESAR.get();
        case LISA_LISA:
            return ModSounds.BREATH_LISA_LISA.get();
        default:
            return ModSounds.BREATH_DEFAULT.get();
        }
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
        nbt.putFloat("BreathingTechnique", breathingTechniqueLevel);
        CompoundNBT skillsMapNbt = new CompoundNBT();
        for (HamonSkill skill : HamonSkill.values()) {
            skillsMapNbt.putBoolean(skill.getName(), hamonSkills.isSkillLearned(skill));
        }
        nbt.put("Skills", skillsMapNbt);
        CompoundNBT exercises = new CompoundNBT();
        for (Exercise exercise : Exercise.values()) {
            exercises.putInt(exercise.toString(), exerciseTicks.get(exercise));
        }
        nbt.put("Exercises", exercises);
        nbt.putLong("LastDay", lastTickedDay);
        nbt.putFloat("TrainingBonus", breathingTrainingBonus);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        hamonStrengthPoints = nbt.getInt("StrengthPoints");
        hamonStrengthLevel = levelFromPoints(hamonStrengthPoints);
        hamonControlPoints = nbt.getInt("ControlPoints");
        hamonControlLevel = levelFromPoints(hamonControlPoints);
        breathingTechniqueLevel = nbt.getFloat("BreathingTechnique");
        serverPlayer.ifPresent(player -> {
            ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTechniqueLevel);
        });
        recalcHamonDamage();
        fillSkillsFromNbt(nbt.getCompound("Skills"));
        CompoundNBT exercises = nbt.getCompound("Exercises");
        int[] exercisesNbt = new int[4];
        for (Exercise exercise : Exercise.values()) {
            exercisesNbt[exercise.ordinal()] = exercises.getInt(exercise.toString());
        }
        setExerciseTicks(exercisesNbt[0], exercisesNbt[1], exercisesNbt[2], exercisesNbt[3]);
        lastTickedDay = nbt.getLong("LastDay");
        breathingTrainingBonus = nbt.getFloat("TrainingBonus");
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
        giveBreathingTechniqueBuffs(user);
        for (HamonSkill skill : HamonSkill.values()) {
            if (isSkillLearned(skill)) {
                PacketManager.sendToClient(new HamonSkillLearnPacket(skill), user);
                onSkillAdded(skill);
            }
        }
    }

    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrSyncHamonStatsPacket(
                user.getId(), false, getHamonStrengthPoints(), getHamonControlPoints(), getBreathingLevel()), entity);
    }

    public enum Exercise {
        MINING(300),
        RUNNING(180),
        SWIMMING(180),
        MEDITATION(90);

        public final int maxTicks;

        private Exercise(float seconds) {
            this.maxTicks = (int) (seconds * 20F);
        }
    }
}
