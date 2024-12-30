package com.github.standobyte.jojo.power.impl.nonstand.type.hamon;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;
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
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.non_stand.HamonSunlightYellowOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.HamonStatIncNotif;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BarType;
import com.github.standobyte.jojo.entity.HamonProjectileShieldEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.modcompat.ModInteractionUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonResetSkillsButtonPacket.HamonSkillsTab;
import com.github.standobyte.jojo.network.packets.fromserver.HamonExercisesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillAddPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSkillRemovePacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonSyncOnLoadPacket;
import com.github.standobyte.jojo.network.packets.fromserver.HamonUiEffectPacket;
import com.github.standobyte.jojo.network.packets.fromserver.MultiLineOverlayMsgPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonAuraColorPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonAuraColorPacket.HamonAuraColor;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonBreathStabilityPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonCharacterTechniquePacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonEnergyTicksPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonLiquidWalkingPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonMeditationPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonProtectionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonStatsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHamonSyncPlayerLearnerPacket;
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
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
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
    
    private static final int[] POINTS_AT_LEVEL;
    static {
        POINTS_AT_LEVEL = new int[MAX_STAT_LEVEL + 1];
        int diff = 0;
        
        POINTS_AT_LEVEL[0] = 0;
        POINTS_AT_LEVEL[1] = 2;
        for (int i = 2; i < POINTS_AT_LEVEL.length; i++) {
            diff += 3 + (i - 1) / 20;
            
            POINTS_AT_LEVEL[i] = POINTS_AT_LEVEL[i - 1] + POINTS_AT_LEVEL[1] + diff;
        }
    }
    public static final int MAX_HAMON_POINTS = pointsAtLevel(MAX_STAT_LEVEL);
    
    private final Random random = new Random();
    private boolean tcsa = true;

    private int hamonStrengthPoints;
    private int hamonStrengthLevel;
    private int hamonControlPoints;
    private int hamonControlLevel;
    private float hamonDamageFactor = 1F;
    private float pointsIncFrac = 0;
    
    private float breathingTrainingLevel;
    private float breathingTrainingDayBonus;
//    private float prevDayExercisesCount;
    private int canSkipTrainingDays;
    private EnumMap<Exercise, Integer> exerciseTicks = new EnumMap<Exercise, Integer>(Exercise.class);
    
    private boolean isMeditating;
    private int meditationTicks;
    private int breathStabilityIncTicks;

    private MainHamonSkillsManager hamonSkills;
    
    private Set<PlayerEntity> newLearners = new HashSet<>();

    private int noEnergyDecayTicks = 0;
    private boolean playedEnergySound = false;
    private float breathStability;
    private float prevBreathStability;
    private int ticksMaskWithNoHamonBreath;
    private int ticksNoBreathStabilityInc;
    
    public HamonProjectileShieldEntity shieldEntity;
    private boolean hamonProtection = false;
    public OptionalInt regenImpliedDuration = OptionalInt.empty();
    
    private boolean waterWalkingPrevTick = false;
    private boolean waterWalkingThisTick = false;
    private boolean trWaterWalking = false;
    private boolean clLargeSpark = false;
    private boolean clTickSpark = false;

    public HamonData() {
        hamonSkills = new MainHamonSkillsManager();
        for (Exercise exercise : Exercise.values()) {
            exerciseTicks.put(exercise, 0);
        }
    }
    
    public void onClear() {
        clearBreathingTrainingBuffs(power.getUser());
    }
    
    public void tick() {
        updateHeight = false;
        LivingEntity user = power.getUser();
        if (user.isAlive()) {
            /*if (hamonProtection) {
                if (user.level.isClientSide) {
                    tickHamonProtection();
                }
            }*/
            
            HamonWallClimbing2.tickWallClimbing(power, this, user);
            tickNewPlayerLearners(user);
            if (!user.level.isClientSide()) {
                tickAirSupply(user);
                
                if (tcsa && (power.isUserCreative() || getCharacterTechnique() != null)) {
                    tcsa = false;
                }
                
                if (shieldEntity != null && !shieldEntity.isAlive()) {
                    shieldEntity = null;
                }
            }
            tickChargeParticles();
            tickBreathStability();
        }
        else {
            setIsMeditating(user, false);
            
            if (shieldEntity != null) {
                shieldEntity.remove();
                shieldEntity = null;
            }
            hamonProtection = false;
        }
        
        waterWalkingThisTick = false;
    }
    
    public static final float ENERGY_TICK_DOWN_AMOUNT = 20;
    public float tickEnergy() {
        LivingEntity user = power.getUser();
        if (power.getHeldAction() == ModHamonActions.HAMON_BREATH.get() && user.getAirSupply() >= user.getMaxAirSupply()) {
            return power.getEnergy() + tickHamonBreath(ModHamonActions.HAMON_BREATH.get());
        }
        else {
            if (isUserWearingBreathMask() && !isMeditating()) {
                ticksMaskWithNoHamonBreath++;
            }
            else {
                ticksMaskWithNoHamonBreath = 0;
            }
            
            if (power.getEnergy() <= 0) {
                setHamonProtection(false);
            }
            
            playedEnergySound = false;
            if (noEnergyDecayTicks > 0) {
                noEnergyDecayTicks--;
                return power.getEnergy();
            }
            else if (JojoModConfig.getCommonConfigInstance(user.level.isClientSide()).hamonEnergyTicksDown.get()) {
                return power.getEnergy() - ENERGY_TICK_DOWN_AMOUNT;
            }
            else {
                return power.getEnergy();
            }
        }
    }
    
    public float tickHamonBreath(Action<?> hamonBreathAction) {
        LivingEntity user = power.getUser();
        ticksMaskWithNoHamonBreath = 0;
        if (user.level.isClientSide() && power.getEnergy() > 0 && !playedEnergySound) {
            ClientTickingSoundsHelper.playHamonEnergyConcentrationSound(user, 1.0F, hamonBreathAction);
            playedEnergySound = true;
            if (user == ClientUtil.getClientPlayer()) {
                BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).resetRedHighlight();
            }
        }
        updateNoEnergyDecayTicks();
        return getMaxBreathStability() / fullEnergyTicks();
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
    
    public void reduceBreathStability(float value) {
        setBreathStability(value, 80);
    }
    
    public void setBreathStability(float value, int noIncTicks) {
        value = MathHelper.clamp(value, 0, getMaxBreathStability());
        boolean send = this.breathStability != value;
        this.breathStability = value;
        this.prevBreathStability = value;
        this.ticksNoBreathStabilityInc = Math.max(noIncTicks, this.ticksNoBreathStabilityInc);
        if (send) {
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonBreathStabilityPacket(
                        user.getId(), getBreathStability(), ticksNoBreathStabilityInc), user);
            }
        }
    }
    
    private int prevAir = 300;
    private void tickBreathStability() {
        LivingEntity user = power.getUser();
        boolean canBreath = user.getAirSupply() >= user.getMaxAirSupply();
        float inc;
        float maxStability = getMaxBreathStability();
        boolean maskNoBreath = false;
        
        if (isUserWearingBreathMask()) {
            float ticksCanBreatheWithMask = 400 + breathingTrainingLevel * 16;
            float breathMaskHandicap = 0;
            if (breathingTrainingLevel < MAX_BREATHING_LEVEL) {
                breathMaskHandicap = MathHelper.clamp((ticksCanBreatheWithMask - ticksMaskWithNoHamonBreath) / (ticksCanBreatheWithMask / 2), -1, 1);
            }
            boolean canIndicateInHud = user.level.isClientSide() && ClientUtil.getClientPlayer() == user;
            if (canIndicateInHud && breathMaskHandicap == 0) {
                BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).triggerRedHighlight(4);
            }
            // normal recovery, slowed down when not using hamon breath for too long (10s)
            if (breathMaskHandicap >= 0) {
                inc = maxStability / fullBreathStabilityTicks() * breathMaskHandicap;
            }
            // go down when not using hamon breath for even longer (20s)
            else {
                inc = maxStability / 1200 * breathMaskHandicap;
                maskNoBreath = true;
                
                if (canIndicateInHud) {
                    BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).triggerRedHighlight(999999);
                    if ((ticksMaskWithNoHamonBreath - ticksCanBreatheWithMask > 400 || (breathStability + inc) / maxStability < 0.2F)
                            && breathStability + inc > 0
                            && ModHamonActions.HAMON_BREATH.get().checkConditions(user, power, ActionTarget.EMPTY).isPositive()) {
                        ClientUtil.setOverlayMessage(new TranslationTextComponent("hamon.breath_control_mask.restore_stab"));
                    }
                }
            }
        }
        else {
            // normal recovery
            inc = maxStability / fullBreathStabilityTicks();
        }
        
        // meditation speeding up the recovery (if there's no mask handicap)
        if (inc >= 0 && isMeditating() && breathStabilityIncTicks > 0) {
            inc *= MathHelper.sqrt((float) Math.min(breathStabilityIncTicks, 100));
        }
        
        if (!canBreath) {
            inc = Math.min(inc, 0);
        }
        
        if (inc > 0 && ticksNoBreathStabilityInc > 0) {
            ticksNoBreathStabilityInc--;
            inc = 0;
        }
        breathStability = MathHelper.clamp(breathStability + inc, 0, getMaxBreathStability());
        int air = user.getAirSupply();
        if (!user.level.isClientSide()) {
            if (breathStability == 0 && prevBreathStability > 0 || air == 0 && prevAir > 0) {
                outOfBreath(maskNoBreath && air > 0);
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
        float ticks = 80F - (40F * breathingTrainingLevel / MAX_BREATHING_LEVEL);
        if (meditationCompleted) {
            ticks -= MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION;
        }
        return ticks;
    }
    
    private float fullBreathStabilityTicks() {
        float ticks = 1200F - (600F * breathingTrainingLevel / MAX_BREATHING_LEVEL);
        return ticks;
    }
    
    public float getMaxBreathStability() {
        return getMaxBreathStabilityAt(getHamonControlLevel());
    }
    
    protected float getMaxBreathStabilityAt(int controlLvl) {
        float max = NonStandPower.BASE_MAX_ENERGY * (1F + controlLvl * 0.1F);
        if (swimmingCompleted) {
            max *= SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER;
        }
        return max;
    }
    
    public void setNoEnergyDecayTicks(TrHamonEnergyTicksPacket packet) {
        this.noEnergyDecayTicks = packet.getTicks();
    }
    
    private void updateNoEnergyDecayTicks() {
        noEnergyDecayTicks = 50 + MathUtil.fractionRandomInc(150F * getBreathingLevel() / HamonData.MAX_BREATHING_LEVEL);
    }
    
    public float getEnergyRatio() {
        return power.getEnergy() / getMaxBreathStability();
    }
    
    
    
    public static final float ALL_EXERCISES_EFFICIENCY_ADD_MULTIPLIER = 0.05F;
    @Deprecated
    public float getActionEfficiency(float energyCost, boolean handSwingTimer) {
        return getActionEfficiency(energyCost, handSwingTimer, null);
    }
    
    public float getActionEfficiency(float energyCost, boolean handSwingTimer, @Nullable AbstractHamonSkill hamonSkill) {
        float efficiency = getHamonEnergyUsageEfficiency(energyCost, false) * getBloodstreamEfficiency();
        
        if (efficiency > 0) {
            float multiplier = 1;
            if (exercisesCompleted >= MAX_EXERCISES_NEEDED) {
                multiplier += ALL_EXERCISES_EFFICIENCY_ADD_MULTIPLIER;
            }
            if (hamonSkill != null) {
                CharacterHamonTechnique technique = getCharacterTechnique();
                if (technique != null) {
                    multiplier += technique.getAddSkillEfficiency(hamonSkill);
                }
            }
            efficiency *= multiplier;
            
            if (handSwingTimer && power.getUser() instanceof PlayerEntity) {
                float swingStrengthScale = ((PlayerEntity) power.getUser()).getAttackStrengthScale(1);
                efficiency *= (0.2F + swingStrengthScale * swingStrengthScale * 0.8F);
            }
            
            float stab = getBreathStability();
            float maxStab = getMaxBreathStability();
            if (stab < maxStab) {
                efficiency *= 0.5F + 0.5F * stab / maxStab;
            }
        }
        
        return efficiency;
    }
    
//    @Deprecated
//    @Nullable
//    public <T> T consumeHamonEnergyTo(Function<Float, T> actionWithHamonEfficiency, float energyCost) {
//        return consumeHamonEnergyTo(actionWithHamonEfficiency, energyCost, null);
//    }
    
    @Nullable
    public <T> T consumeHamonEnergyTo(Function<Float, T> actionWithHamonEfficiency, float energyCost, @Nullable AbstractHamonSkill usedSkill) {
        float efficiency = getActionEfficiency(energyCost, false, usedSkill);
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
    
    
    
    private static final float NO_ENERGY_EFFICIENCY = 0.5f;
    private static final float ENERGY_STABILITY_USAGE_RATIO = 2.5F;
    float getHamonEnergyUsageEfficiency(float energyNeeded, boolean doConsume) {
        LivingEntity user = power.getUser();
        doConsume &= !user.level.isClientSide() && !power.isUserCreative();
        energyNeeded = reduceEnergyConsumed(energyNeeded, power, user);
        
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
            return NO_ENERGY_EFFICIENCY + (1 - NO_ENERGY_EFFICIENCY) * energyRatio;
        }
        
        else {
            if (doConsume) {
                serverPlayer.ifPresent(player -> {
                    PacketManager.sendToClient(new HamonUiEffectPacket(HamonUiEffectPacket.Type.NO_ENERGY), player);
                });
            }
            
            float energyFromStability = getBreathStability();
            if (!isUserWearingBreathMask()) {
                energyFromStability *= ENERGY_STABILITY_USAGE_RATIO;
            }
            
            float energyRatio = Math.min(energyFromStability / energyNeeded, 1);
            if (doConsume) {
                if (energyFromStability < energyNeeded) {
                    reduceBreathStability(0);
                    outOfBreath(false);
                }
                else {
                    reduceBreathStability((energyFromStability - energyNeeded) / ENERGY_STABILITY_USAGE_RATIO);
                }
            }
            return NO_ENERGY_EFFICIENCY * energyRatio;
        }
    }
    
    private boolean isUserWearingBreathMask() {
        ItemStack headItem = power.getUser().getItemBySlot(EquipmentSlotType.HEAD);
        return !headItem.isEmpty() && headItem.getItem() == ModItems.BREATH_CONTROL_MASK.get();
    }
    
    private void outOfBreath(boolean mask) {
        power.getUser().setAirSupply(0);
        serverPlayer.ifPresent(player -> {
            PacketManager.sendToClient(new HamonUiEffectPacket(
                    mask ? HamonUiEffectPacket.Type.OUT_OF_BREATH_MASK : HamonUiEffectPacket.Type.OUT_OF_BREATH), player);
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
        
        float bleeding = 0;
        EffectInstance bleedingEffect = user.getEffect(ModStatusEffects.BLEEDING.get());
        if (bleedingEffect != null) {
            bleeding = Math.min((bleedingEffect.getAmplifier() + 1) * 0.2F, 0.8F);
        }
        efficiency *= (1F - bleeding);
        
        float freeze = 0;
        EffectInstance freezeEffect = user.getEffect(ModStatusEffects.FREEZE.get());
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
                || action == ModHamonActions.HAMON_BEAT.get()
                || action == ModHamonActions.HAMON_HEALING.get()
                || action == ModHamonActions.HAMON_BREATH.get()
                || action == ModHamonActions.CAESAR_BUBBLE_CUTTER_GLIDING.get()
                || hamonSkills.isUnlockedFromSkills(action);
    }
    
    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType, TypeSpecificData oldData) {
        hamonSkills.addSkill(ModHamonSkills.OVERDRIVE.get());
        hamonSkills.addSkill(ModHamonSkills.HEALING.get());
        breathStability = getMaxBreathStability();
        prevBreathStability = breathStability;
        super.onPowerGiven(oldType, oldData);
    }
    
    
    public static int pointsAtLevel(int level) {
        level = MathHelper.clamp(level, 0, MAX_STAT_LEVEL);
        return POINTS_AT_LEVEL[level];
    }
    
    public static int levelFromPoints(int points) {
        return GeneralUtil.largestLessOrEqualBinarySearch(POINTS_AT_LEVEL, points);
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
        setHamonStatPoints(stat, points, ignoreTraining, allowLesserValue, false, false);
    }
    
    public void setHamonStatPoints(HamonStat stat, int points, boolean ignoreTraining, 
            boolean allowLesserValue, boolean clientSide, boolean notifyInUI) {
        int oldPoints = getStatPoints(stat);
        int oldLevel = getStatLevel(stat);
        if (!ignoreTraining) {
            int levelLimit = getStatLevelLimit(clientSide);
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
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonStatsPacket(user.getId(), true, stat, newPoints), user);
                serverPlayer.ifPresent(player -> {
                    ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
                });
            }
            int newLevel = getStatLevel(stat);
            if (oldLevel != newLevel) {
                switch (stat) {
                case STRENGTH:
                    recalcHamonDamage();
                    break;
                case CONTROL:
                    float energyRatio = getMaxBreathStabilityAt(newLevel) / getMaxBreathStabilityAt(oldLevel);
                    breathStability *= energyRatio;
                    power.setEnergy(power.getEnergy() * energyRatio);
                    break;
                }
                if (newLevel > oldLevel && notifyInUI && user.level.isClientSide() && user == ClientUtil.getClientPlayer()) {
                    ActionsOverlayGui.getInstance().onHamonStatIncreased(stat == HamonStat.STRENGTH ? HamonStatIncNotif.STRENGTH : HamonStatIncNotif.CONTROL);
                }
            }
        }
    }
    
    public int getStatLevelLimit(boolean clientSide) {
        int config = JojoModConfig.getCommonConfigInstance(clientSide).breathingHamonStatGap.get();
        if (config < 0) {
            return Integer.MAX_VALUE;
        }
        return (int) getBreathingLevel() + config;
    }
    
    public static final float MAX_HAMON_STRENGTH_MULTIPLIER = dmgFormula(MAX_STAT_LEVEL); // 7
    private void recalcHamonDamage() {
        hamonDamageFactor = dmgFormula(hamonStrengthLevel);
    }
    
    private static float dmgFormula(float strength) {
        return (float) 1F + strength * 0.1F;
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
    
    public int getStatLevel(HamonStat stat) {
        switch (stat) {
        case STRENGTH:
            return getHamonStrengthLevel();
        case CONTROL:
            return getHamonControlLevel();
        default:
            throw new IllegalArgumentException("Unexpected HamonStat constant: " + stat.name());
        }
    }
    
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
        return MathHelper.clamp(lvl, 0, MAX_STAT_LEVEL) / 5 - spentPoints;
    }
    
    public int nextSkillPointLvl(HamonStat stat) {
        return MathHelper.clamp(getStatLevel(stat), 0, MAX_STAT_LEVEL - 1) / 5 * 5 + 5;
    }
    
    private static final float ENERGY_PER_POINT = 750F;
    public void hamonPointsFromAction(HamonStat stat, float energyCost) {
        if (isSkillLearned(ModHamonSkills.NATURAL_TALENT.get())) {
            energyCost *= 2;
        }
        energyCost *= JojoModConfig.getCommonConfigInstance(false).hamonPointsMultiplier.get().floatValue();
        int points = (int) (energyCost / ENERGY_PER_POINT);
        pointsIncFrac += (energyCost % ENERGY_PER_POINT) / ENERGY_PER_POINT;
        if (pointsIncFrac >= 1) {
            points++;
            pointsIncFrac--;
        }
        setHamonStatPoints(stat, getStatPoints(stat) + points, false, false);
    }
    
    public float getBreathingLevel() {
        return breathingTrainingLevel;
    }
    
    public void setBreathingLevel(float level) {
        setBreathingLevel(level, false);
    }
    
    public void setBreathingLevel(float level, boolean notifyInUI) {
        float oldLevel = breathingTrainingLevel;
        breathingTrainingLevel = MathHelper.clamp(level, 0, MAX_BREATHING_LEVEL);
        LivingEntity user = power.getUser();
        if (oldLevel != breathingTrainingLevel) {
            recalcHamonDamage();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonStatsPacket(user.getId(), true, getBreathingLevel()), user);
                serverPlayer.ifPresent(player -> {
                    ModCriteriaTriggers.HAMON_STATS.get().trigger(player, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
                });
            }
            else {
                if ((int) breathingTrainingLevel > (int) oldLevel && notifyInUI && user == ClientUtil.getClientPlayer()) {
                    ActionsOverlayGui.getInstance().onHamonStatIncreased(HamonStatIncNotif.BREATHING);
                }
            }
        }
        if (!user.level.isClientSide()) {
            giveBreathingTrainingBuffs(user);
        }
    }
    
    private static final AttributeModifier ATTACK_DAMAGE = new AttributeModifier(
            UUID.fromString("8dcb2ad7-6067-4615-b7b6-af5256537c10"), "Attack damage from Hamon Training", 0.03, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier ATTACK_SPEED = new AttributeModifier(
            UUID.fromString("995b2915-9053-472c-834c-f94251e81659"), "Attack speed from Hamon Training", 0.015, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MOVEMENT_SPEED = new AttributeModifier(
            UUID.fromString("ffa9ba4e-3811-44f7-a4a9-887ffbd47390"), "Movement speed from Hamon Training", 0.0005, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier SWIMMING_SPEED = new AttributeModifier(
            UUID.fromString("34dcb563-6759-4a2b-9dd8-ad2dd7e70404"), "Swimming speed from Hamon Training", 0.01, AttributeModifier.Operation.ADDITION);
    
    private void giveBreathingTrainingBuffs(LivingEntity entity) {
        setBreathingTrainingAttributes(entity, (int) getBreathingLevel());
    }
    
    private void clearBreathingTrainingBuffs(LivingEntity entity) {
        setBreathingTrainingAttributes(entity, 0);
    }
    
    private void setBreathingTrainingAttributes(LivingEntity entity, int lvl) {
        MCUtil.applyAttributeModifierMultiplied(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE, lvl);
        MCUtil.applyAttributeModifierMultiplied(entity, Attributes.ATTACK_SPEED, ATTACK_SPEED, lvl);
        MCUtil.applyAttributeModifierMultiplied(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED, lvl);
        MCUtil.applyAttributeModifierMultiplied(entity, ForgeMod.SWIM_SPEED.get(), SWIMMING_SPEED, lvl);
    }
    
    public static final AttributeModifier RUNNING_COMPLETED = new AttributeModifier(
            UUID.fromString("b730b24e-e970-4a94-b300-57e2555b42b5"), "Movement speed from running exercise", 0.1D, AttributeModifier.Operation.MULTIPLY_BASE);
    public static final AttributeModifier MINING_COMPLETED = new AttributeModifier(
            UUID.fromString("8674ea35-6eaf-4e22-98da-4ec0c5a4d20d"), "Attack speed from running exercise", 0.05D, AttributeModifier.Operation.MULTIPLY_BASE);
    public static final float SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER = 1.1F;
    public static final float MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION = 20;
    private boolean swimmingCompleted = false;
    private boolean meditationCompleted = false;
    private int exercisesCompleted = 0;
    
    private void updateExerciseAttributes(LivingEntity entity) {
        exercisesCompleted = 0;
        for (Exercise exercise : Exercise.values()) {
            if (isExerciseComplete(exercise)) {
                ++exercisesCompleted;
                switch (exercise) {
                case RUNNING:
                    if (!entity.level.isClientSide()) {
                        MCUtil.applyAttributeModifier(entity, Attributes.MOVEMENT_SPEED, RUNNING_COMPLETED);
                    }
                    break;
                case MINING:
                    if (!entity.level.isClientSide()) {
                        MCUtil.applyAttributeModifier(entity, Attributes.ATTACK_SPEED, MINING_COMPLETED);
                    }
                    break;
                case SWIMMING:
                    // FIXME also update energy count
                    swimmingCompleted = true;
                    break;
                case MEDITATION:
                    meditationCompleted = true;            
                    break;
                default:
                    break;
                }
            }
            
            else {
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
                default:
                    break;
                }
            }
        }
    }
    
    public int getExerciseTicks(Exercise exercise) {
        return Math.min(exerciseTicks.get(exercise), exercise.getMaxTicks(this));
    }
    
    public boolean isExerciseComplete(Exercise exercise) {
        return getExerciseTicks(exercise) >= exercise.getMaxTicks(this);
    }
    
    public int getCompleteExercisesCount() {
        return exercisesCompleted;
    }
    
    public float getMaxIncompleteExercise() {
        return exerciseTicks.entrySet().stream()
                .filter(entry -> entry.getValue() < entry.getKey().getMaxTicks(this))
                .map(entry -> (float) entry.getValue() / (float) entry.getKey().getMaxTicks(this))
                .max(Float::compare).orElse(0F);
    }
    
    public boolean has4ExercisesBonus() {
        return exercisesCompleted >= MAX_EXERCISES_NEEDED;
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
            incExerciseTicks(Exercise.MINING, 1, user.level.isClientSide());
        }
        
        if (positionChanged && user.isSwimming() && JojoModUtil.playerHasClientInput(user)) {
            incExerciseTicks(Exercise.SWIMMING, 1, user.level.isClientSide());
        }
        
        else if (positionChanged && user.isSprinting() && user.isOnGround() && !user.isSwimming()) {
            incExerciseTicks(Exercise.RUNNING, 1, user.level.isClientSide());
        }
        
        if (isMeditating()) {
            if (++meditationTicks >= MEDITATION_INC_START) {
                incExerciseTicks(Exercise.MEDITATION, 1, user.level.isClientSide());
                breathStabilityIncTicks++;
            }
            updateBbHeight(user);
            if (!user.level.isClientSide()) {
                user.getFoodData().addExhaustion(-0.0025F);
                if (user.tickCount % 200 == 0 && user.isHurt() && user.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
                    user.heal(1.0F);
                }
            }
        }
        
        if (incExerciseLastTick && !incExerciseThisTick || exerciseCompleted) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClient(HamonExercisesPacket.exercisesOnly(this), player);
            });
        }
        if (breathingTrainingLevel < MAX_BREATHING_LEVEL && exerciseCompleted && exercisesCompleted <= MAX_EXERCISES_NEEDED) {
            updateExerciseAttributes(user);
            serverPlayer.ifPresent(player -> {
                IFormattableTextComponent message1 = new TranslationTextComponent("hamon.exercise.all.count.message" + (exercisesCompleted >= 4 ? ".4" : ""), 
                        exercisesCompleted, MAX_EXERCISES_NEEDED);
                IFormattableTextComponent message2 = null;
                switch (exercisesCompleted) {
                case 3:
                    message2 = new TranslationTextComponent("hamon.exercise.all.count.message2.3", 
                            new DecimalFormat("#.##").format(getBreathingIncrease(player, false)));
                    break;
                case 4:
                    message2 = new TranslationTextComponent("hamon.exercise.all.count.message2.4", 
                            CAN_SKIP_DAYS);
                    break;
                }
                
                if (message2 == null) {
                    player.sendMessage(message1, ChatType.GAME_INFO, Util.NIL_UUID);
                }
                else {
                    PacketManager.sendToClient(new MultiLineOverlayMsgPacket(ImmutableList.of(message1, message2)), player);
                }
            });
        }
        incExerciseLastTick = incExerciseThisTick;
    }
    
    public static final int MAX_EXERCISES_NEEDED = 4;
    public static final int CAN_SKIP_DAYS = 2;
    
    private static final int MEDITATION_INC_START = 40;
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
    
    public void setExerciseTicks(int[] ticks, boolean clientSide) {
        Exercise[] exercises = Exercise.values();
        for (int i = 0; i < exercises.length; i++) {
            setExerciseValue(exercises[i], ticks[i], clientSide);
        }
        updateExerciseAttributes(power.getUser());
    }
    
    private void setExerciseValue(Exercise exercise, int value, boolean clientSide) {
        if (exerciseTicks.put(exercise, value) != value && clientSide) {
            ActionsOverlayGui.getInstance().onHamonExerciseValueChanged(exercise);
        }
    }
    
    public void setIsMeditating(LivingEntity user, boolean isMeditating) {
        if (this.isMeditating != isMeditating) {
            this.isMeditating = isMeditating;
            this.meditationTicks = 0;
            this.breathStabilityIncTicks = 0;
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonMeditationPacket(user.getId(), isMeditating), user);
            }
            if (isMeditating) {
                user.yBodyRot = user.yRot;
            }
            else {
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
    
    public float getTrainingBonus(boolean perksAndConfigMult) {
        if (!isUserWearingBreathMask()) {
            return 0;
        }
        return perksAndConfigMult ? multiplyPositiveBreathingTraining(breathingTrainingDayBonus) : breathingTrainingDayBonus;
    }
    
    private float multiplyPositiveBreathingTraining(float training) {
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
    
    public int getCanSkipTrainingDays() {
        return canSkipTrainingDays;
    }
    
    public void setCanSkipTrainingDays(int days) {
        this.canSkipTrainingDays = days;
    }
    
    public void breathingTrainingDay(PlayerEntity user) {
        World world = user.level;
        if (!world.isClientSide()) {
            float lvlInc = getBreathingIncrease(user, true);
            setBreathingLevel(getBreathingLevel() + lvlInc);
            if (isSkillLearned(ModHamonSkills.CHEAT_DEATH.get())) {
                HamonUtil.updateCheatDeathEffect(power.getUser());
            }
        }
        for (Exercise exercise : exerciseTicks.keySet()) {
            setExerciseValue(exercise, 0, world.isClientSide());
        }
        updateExerciseAttributes(user);
    }
    
    public boolean breathingCanGoDown(PlayerEntity user) {
        return JojoModConfig.getCommonConfigInstance(false).breathingTrainingDeterioration.get() 
                && breathingTrainingLevel < MAX_BREATHING_LEVEL;
    }
    
    public float getBreathingIncrease(PlayerEntity user, boolean newTrainingDay) {
        float completedExercises = getCompleteExercisesCount() + getMaxIncompleteExercise();
        /* at least 2 exercises to get positive increase, 
           >= 3 exercises give max increase */
        float lvlInc = MathHelper.clamp(completedExercises - 2, -1, 1);
        float bonusIncrease = lvlInc * 0.25f;
        boolean keepLvlThisDay = canSkipTrainingDays > 0;
        
        if (lvlInc <= 0) {
            if (!breathingCanGoDown(user) || keepLvlThisDay) {
                lvlInc = 0;
            }
            else {
                lvlInc *= 0.25F;
            }
            bonusIncrease = 0;
        }
        else {
            lvlInc = multiplyPositiveBreathingTraining(lvlInc + getTrainingBonus(false));
        }
        
        if (newTrainingDay) {
            if (lvlInc <= 0 && !keepLvlThisDay) {
                breathingTrainingDayBonus = 0;
            }
            else if (isUserWearingBreathMask()) {
                breathingTrainingDayBonus += bonusIncrease;
            }
            
            if (canSkipTrainingDays > 0) --canSkipTrainingDays;
            if (completedExercises >= MAX_EXERCISES_NEEDED) {
                canSkipTrainingDays = Math.max(canSkipTrainingDays, CAN_SKIP_DAYS);
            }
        }
        
        lvlInc = MathHelper.clamp(lvlInc, -breathingTrainingLevel, MAX_BREATHING_LEVEL - breathingTrainingLevel);
        return lvlInc;
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
        if (!checkRequirements || !isSkillLearned(skill) && canLearnSkill(user, skill, HamonUtil.nearbyTeachersSkills(power.getUser())).isPositive()) {
            hamonSkills.addSkill(skill);
            power.clUpdateHud();
            serverPlayer.ifPresent(player -> {
                if (skill == ModHamonSkills.CHEAT_DEATH.get()) {
                    HamonUtil.updateCheatDeathEffect(player);
                }
                else if (skill == ModHamonSkills.SATIPOROJA_SCARF.get()
                        && user.getCapability(LivingUtilCapProvider.CAPABILITY).map(cap -> cap.onScarfPerk()).orElse(true)) {
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
    
    public void removeHamonSkill(AbstractHamonSkill skill) {
        if (!skill.isUnlockedByDefault() && isSkillLearned(skill)) {
            hamonSkills.removeSkill(skill);
            power.clUpdateHud();
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClient(new HamonSkillRemovePacket(skill), player);
                if (skill == ModHamonSkills.CHEAT_DEATH.get()) {
                    player.removeEffect(ModStatusEffects.CHEAT_DEATH.get());
                }
            });
        }
    }
    
    public static boolean canResetTab(PlayerEntity user, HamonSkillsTab type) {
        return user.abilities.instabuild;
    }

    public void resetHamonSkills(LivingEntity user, HamonSkillsTab type) {
        if (user instanceof PlayerEntity && !canResetTab((PlayerEntity) user, type)) return;
        
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
        if (type == HamonSkillsTab.TECHNIQUE) {
            resetCharacterTechnique(user);
        }
    }
    
    public Iterable<AbstractHamonSkill> getLearnedSkills() {
        return hamonSkills.getLearnedSkills();
    }
    
    public void pickHamonTechnique(LivingEntity user, CharacterHamonTechnique technique) {
        HamonTechniqueManager data = hamonSkills.getTechniqueData();
        if (data.canPickTechnique(user)) {
            data.setTechnique(technique);
            data.addPerks(user, this);
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonCharacterTechniquePacket(user.getId(), technique, true), user);
            }
        }
    }
    
    public void resetCharacterTechnique(LivingEntity user) {
        HamonTechniqueManager data = hamonSkills.getTechniqueData();
        if (data.getTechnique() != null) {
            data.resetTechnique();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(TrHamonCharacterTechniquePacket.reset(user.getId()), user);
            }
        }
    }
    
    
    
    @Nullable
    public CharacterHamonTechnique getCharacterTechnique() {
        return hamonSkills.getTechniqueData().getTechnique();
    }
    
    public boolean characterIs(CharacterHamonTechnique character) {
        return getCharacterTechnique() == character;
    }
    
    public boolean hasTechniqueLevel(int techniqueSkillSlot, boolean clientSide) {
        if (techniqueSkillSlot > HamonTechniqueManager.techniqueSlotsCount(clientSide)) {
            return false;
        }
        return getHamonStrengthLevel() >= HamonTechniqueManager.techniqueSkillRequirement(techniqueSkillSlot, clientSide)
                && getHamonControlLevel() >= HamonTechniqueManager.techniqueSkillRequirement(techniqueSkillSlot, clientSide);
    }
    
    public HamonTechniqueManager.Accessor getTechniqueData() {
        return new HamonTechniqueManager.Accessor(hamonSkills.getTechniqueData());
    }
    
    
    
    public boolean playerWantsToLearn(PlayerEntity playerEntity) {
        return newLearners.contains(playerEntity);
    }
    
    public void addNewPlayerLearner(PlayerEntity learnerPlayer) {
        newLearners.add(learnerPlayer);
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(
                    new TrHamonSyncPlayerLearnerPacket(user.getId(), learnerPlayer.getId(), true), user);
        }
    }
    
    private void tickNewPlayerLearners(LivingEntity user) {
        for (Iterator<PlayerEntity> it = newLearners.iterator(); it.hasNext(); ) {
            PlayerEntity player = it.next();
            if (!player.isAlive() || user.distanceToSqr(player) > 64) {
                it.remove();
            }
        }
    }
    
    public boolean interactWithNewLearner(PlayerEntity learnerPlayer) {
        if (newLearners.contains(learnerPlayer)) {
            if (!learnerPlayer.level.isClientSide()) {
                HamonUtil.startLearningHamon(learnerPlayer.level, learnerPlayer, 
                        INonStandPower.getPlayerNonStandPower(learnerPlayer), power.getUser(), this);
                LivingEntity user = power.getUser();
                PacketManager.sendToClientsTracking(
                        new TrHamonSyncPlayerLearnerPacket(user.getId(), learnerPlayer.getId(), false), user);
            }
            newLearners.remove(learnerPlayer);
            return true;
        }
        return false;
    }
    
    public void removeNewLearner(PlayerEntity player) {
        newLearners.remove(player);
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
        if (!user.level.isClientSide() || ClientUtil.getClientPlayer() == user) {
            HamonAuraColor auraColor = getThisTickAuraColor(user);
            if (auraColor != this.auraColor) {
                this.auraColor = auraColor;
                if (!user.level.isClientSide()) {
                    PacketManager.sendToClientsTracking(new TrHamonAuraColorPacket(user.getId(), auraColor), user);
                }
            }
        }
        if (user.level.isClientSide()) {
            float energy = power.getEnergy();
            Action<?> heldAction = power.getHeldAction();
            if (heldAction instanceof HamonSunlightYellowOverdrive) {
                if (!power.isUserCreative()) {
                    energy += ((HamonSunlightYellowOverdrive) heldAction).getSpentEnergy(power);
                }
                energy *= 2;
            }
            float particlesPerTick = energy / getMaxBreathStability() * getHamonDamageMultiplier();
            boolean isUserTheCameraEntity = user == ClientUtil.getCameraEntity();
            IParticleData particleType = PARTICLE_TYPE.get(auraColor).get();
            
            GeneralUtil.doFractionTimes(() -> {
                CustomParticlesHelper.createHamonAuraParticle(particleType, user, 
                        user.getX() + (random.nextDouble() - 0.5) * (user.getBbWidth() + 0.5F), 
                        user.getY() + random.nextDouble() * (user.getBbHeight() * 0.5F), 
                        user.getZ() + (random.nextDouble() - 0.5) * (user.getBbWidth() + 0.5F));
            }, particlesPerTick);
            if (isUserTheCameraEntity) {
                CustomParticlesHelper.summonHamonAuraParticlesFirstPerson(particleType, user, particlesPerTick / 5);
            }
        }
    }
    
    private HamonAuraColor getThisTickAuraColor(LivingEntity user) {
        if (power.getHeldAction() == ModHamonActions.HAMON_SUNLIGHT_YELLOW_OVERDRIVE.get()
                || power.getHeldAction() == ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get()) {
            return HamonAuraColor.YELLOW;
        }
        if (ContinuousActionInstance.getCurrentAction(user)
                .map(action -> action.getAction() == ModHamonActions.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE.get())
                .orElse(false)) {
            return HamonAuraColor.YELLOW;
        }
        if (power.getHeldAction() == ModHamonActions.JONATHAN_SCARLET_OVERDRIVE.get()) {
            return HamonAuraColor.RED;
        }
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
            if (lastUsedAction == ModHamonActions.JONATHAN_METAL_SILVER_OVERDRIVE.get()
                    || lastUsedAction == ModHamonActions.JONATHAN_METAL_SILVER_OVERDRIVE_WEAPON.get()) {
                return HamonAuraColor.SILVER;
            }
        }
        
        if (isSkillLearned(ModHamonSkills.METAL_SILVER_OVERDRIVE.get()) && MCUtil.isItemWeapon(user.getMainHandItem())) {
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
    
    
    
    private boolean isBeingSuffocated;
    private void tickAirSupply(LivingEntity user) {
        if (!isBeingSuffocated) {
            int air = user.getAirSupply();
            if (air < user.getMaxAirSupply() - 1 && air > 0) {
                int airRegainChancePerc = (int) (getBreathingLevel() * getBreathStability() / getMaxBreathStability()) - 1;
                if (user.tickCount % 100 < airRegainChancePerc) {
                    user.setAirSupply(air + 1);
                }
            }
        }
        isBeingSuffocated = false;
        
        if (user.getAirSupply() <= -19) {
            reduceBreathStability(0);
        }
    }
    
    public void suffocateTick(float suffocationSpeed) {
        reduceBreathStability(Math.max(getBreathStability() - getMaxBreathStability() * suffocationSpeed, 1));
        this.isBeingSuffocated = true;
    }
    
    public void tcsa(boolean tcsa) {
        this.tcsa = tcsa;
    }
    
    
    
    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("StrengthPoints", hamonStrengthPoints);
        nbt.putInt("ControlPoints", hamonControlPoints);
        nbt.putFloat("PointsIncFrac", pointsIncFrac);
        nbt.putFloat("BreathingTechnique", breathingTrainingLevel);
        nbt.put("Skills", hamonSkills.toNBT());
        CompoundNBT exercises = new CompoundNBT();
        for (Exercise exercise : Exercise.values()) {
            exercises.putInt(exercise.toString(), Math.min(exerciseTicks.get(exercise), exercise.getMaxTicks(this)));
        }
        nbt.put("Exercises", exercises);
        nbt.putFloat("TrainingBonus", breathingTrainingDayBonus);
        nbt.putFloat("CanSkipDays", canSkipTrainingDays);
        nbt.putFloat("BreathStability", breathStability);
        nbt.putInt("EnergyTicks", noEnergyDecayTicks);
        nbt.putInt("MaskNoBreathTicks", ticksMaskWithNoHamonBreath);
        nbt.putInt("NoBreathIncTicks", ticksNoBreathStabilityInc);
        nbt.putBoolean("TCSA", tcsa);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        hamonStrengthPoints = nbt.getInt("StrengthPoints");
        hamonStrengthLevel = levelFromPoints(hamonStrengthPoints);
        hamonControlPoints = nbt.getInt("ControlPoints");
        hamonControlLevel = levelFromPoints(hamonControlPoints);
        pointsIncFrac = nbt.getFloat("PointsIncFrac");
        breathingTrainingLevel = nbt.getFloat("BreathingTechnique");
        recalcHamonDamage();
        hamonSkills.fromNbt(nbt.getCompound("Skills"));
        CompoundNBT exercises = nbt.getCompound("Exercises");
        int[] exercisesNbt = new int[Exercise.values().length];
        for (Exercise exercise : Exercise.values()) {
            exercisesNbt[exercise.ordinal()] = exercises.getInt(exercise.toString());
        }
        setExerciseTicks(exercisesNbt, false);
        breathingTrainingDayBonus = nbt.getFloat("TrainingBonus");
        canSkipTrainingDays = nbt.getInt("CanSkipDays");
        breathStability = nbt.contains("BreathStability") ? nbt.getFloat("BreathStability") : getMaxBreathStability();
        prevBreathStability = breathStability;
        noEnergyDecayTicks = nbt.getInt("EnergyTicks");
        ticksMaskWithNoHamonBreath = nbt.getInt("MaskNoBreathTicks");
        ticksNoBreathStabilityInc = nbt.getInt("NoBreathIncTicks");
        tcsa = nbt.getBoolean("TCSA");
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        giveBreathingTrainingBuffs(user);
        updateExerciseAttributes(user);
        hamonSkills.syncWithUser(user, this);
        PacketManager.sendToClient(HamonExercisesPacket.allData(this), user);
        PacketManager.sendToClient(new HamonSyncOnLoadPacket(ticksMaskWithNoHamonBreath), user);
        ModCriteriaTriggers.HAMON_STATS.get().trigger(user, hamonStrengthLevel, hamonControlLevel, breathingTrainingLevel);
    }
    
    public void handleSyncPacket(HamonSyncOnLoadPacket packet) {
        this.ticksMaskWithNoHamonBreath = packet.ticksMaskWithNoHamonBreath;
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrHamonStatsPacket(
                user.getId(), false, getHamonStrengthPoints(), getHamonControlPoints(), getBreathingLevel()), entity);
        PacketManager.sendToClient(new TrHamonBreathStabilityPacket(user.getId(), getBreathStability(), ticksNoBreathStabilityInc), entity);
        PacketManager.sendToClient(new TrHamonEnergyTicksPacket(user.getId(), noEnergyDecayTicks), entity);
        hamonSkills.syncWithTrackingOrUser(user, entity, this);
        PacketManager.sendToClient(new TrHamonAuraColorPacket(user.getId(), auraColor), entity);
        PacketManager.sendToClient(new TrHamonProtectionPacket(user.getId(), this), entity);
        PacketManager.sendToClient(new TrHamonMeditationPacket(user.getId(), isMeditating()), entity);
    }
    
    public enum Exercise {
        MINING(75),
        RUNNING(67.5f),
        SWIMMING(60),
        MEDITATION(37.5f),
        PLACEHOLDER_1(60),
        PLACEHOLDER_2(60);
        
        public static final boolean TMP_HAS_PLACEHOLDERS = true;
        
        private final float maxTicks;
        
        private Exercise(float seconds) {
            this.maxTicks = seconds * 20F;
        }
        
        public int getMaxTicks(@Nullable HamonData hamon) {
            float multiplier = hamon != null ? (MAX_BREATHING_LEVEL - hamon.getBreathingLevel()) / MAX_BREATHING_LEVEL * 0.75F + 0.25F : 1;
            return MathHelper.floor(maxTicks * multiplier);
        }
        
        public double getBuffPercentage() {
            switch (this) {
            case MINING:
                return HamonData.MINING_COMPLETED.getAmount() * 100;
            case RUNNING:
                return HamonData.RUNNING_COMPLETED.getAmount() * 100;
            case SWIMMING:
                return (HamonData.SWIMMING_COMPLETED_MAX_ENERGY_MULTIPLIER - 1) * 100; 
            case MEDITATION:
                return HamonData.MEDITATION_COMPLETED_ENERGY_REGEN_TIME_REDUCTION / 20;
            default:
                return 0;
            }
        }
        
        private ITextComponent tlName;
        public ITextComponent getName() {
            if (tlName == null) {
                tlName = new TranslationTextComponent("hamon." + name().toLowerCase() + "_exercise");
            }
            return tlName;
        }
    }
    
    public boolean toggleHamonProtection() {
        setHamonProtection(!hamonProtection);
        return hamonProtection;
    }
    
    public void setHamonProtection(boolean isEnabled) {
        if (this.hamonProtection != isEnabled) {
            this.hamonProtection = isEnabled;
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrHamonProtectionPacket(user.getId(), this), user);
            }
        }
    }
    
    public boolean isProtectionEnabled() {
        return hamonProtection;
    }
   
    private void tickHamonProtection() {
        LivingEntity user = power.getUser();
        if (hamonProtection) {
            HamonSparksLoopSound.playSparkSound(user, user.getBoundingBox().getCenter(), 1.0F, 1);
            CustomParticlesHelper.createHamonSparkParticles(user, 
                    user.getRandomX(0.5), user.getRandomY(), user.getRandomZ(0.5), 
                    (int) (MathUtil.fractionRandomInc(1) * 2));
        }
    }
    
    
    public void setWaterWalkingThisTick() {
        waterWalkingThisTick = true;
    }
    
    public void postTickWaterWalking(LivingEntity user) {
        if (!user.level.isClientSide()) {
            if (waterWalkingPrevTick ^ waterWalkingThisTick) {
                PacketManager.sendToClientsTracking(new TrHamonLiquidWalkingPacket(user.getId(), waterWalkingThisTick), user);
            }
        }
        else {
            if (!waterWalkingPrevTick && waterWalkingThisTick || clLargeSpark) {
                HamonUtil.emitHamonSparkParticles(user.level, ClientUtil.getClientPlayer(), user.position(), 0.05F);
                CustomParticlesHelper.createHamonSparkParticles(null, user.position(), 10);
                clTickSpark = false;
                clLargeSpark = false;
            }
        }
        waterWalkingPrevTick = waterWalkingThisTick;
        
        if (user.level.isClientSide() && (trWaterWalking || waterWalkingThisTick) && clTickSpark) {
            HamonSparksLoopSound.playSparkSound(user, user.position(), 1.0F);
            CustomParticlesHelper.createHamonSparkParticles(user, 
                    user.getRandomX(0.5), user.getY(Math.random() * 0.1), user.getRandomZ(0.5), 
                    1);
        }
        clTickSpark = true;
    }
    
    public void trSetWaterWalking(boolean waterWalking, LivingEntity user) {
        this.trWaterWalking = waterWalking;
        if (waterWalking) {
            clLargeSpark = true;
        }
    }
    
    public boolean isWaterWalking() {
        return trWaterWalking || waterWalkingPrevTick;
    }
    
    public float waterWalkingTickCost() {
        return waterWalkingPrevTick ? 1 : 50;
    }
    
}
