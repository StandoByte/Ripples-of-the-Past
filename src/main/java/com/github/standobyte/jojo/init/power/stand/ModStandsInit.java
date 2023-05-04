package com.github.standobyte.jojo.init.power.stand;

import static com.github.standobyte.jojo.init.ModEntityTypes.ENTITIES;
import static com.github.standobyte.jojo.init.power.ModCommonRegisters.ACTIONS;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockBullet;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMake;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockCheckpointMove;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBloodCutter;
import com.github.standobyte.jojo.action.stand.CrazyDiamondHeal;
import com.github.standobyte.jojo.action.stand.CrazyDiamondHeavyPunch;
import com.github.standobyte.jojo.action.stand.CrazyDiamondLeaveObject;
import com.github.standobyte.jojo.action.stand.CrazyDiamondMisshapeBodyPart;
import com.github.standobyte.jojo.action.stand.CrazyDiamondMisshapingPunch;
import com.github.standobyte.jojo.action.stand.CrazyDiamondPreviousState;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRepairItem;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.github.standobyte.jojo.action.stand.HierophantGreenBarrier;
import com.github.standobyte.jojo.action.stand.HierophantGreenEmeraldSplash;
import com.github.standobyte.jojo.action.stand.HierophantGreenGrapple;
import com.github.standobyte.jojo.action.stand.HierophantGreenStringAttack;
import com.github.standobyte.jojo.action.stand.MagiciansRedCrossfireHurricane;
import com.github.standobyte.jojo.action.stand.MagiciansRedDetector;
import com.github.standobyte.jojo.action.stand.MagiciansRedFireball;
import com.github.standobyte.jojo.action.stand.MagiciansRedFlameBurst;
import com.github.standobyte.jojo.action.stand.MagiciansRedKick;
import com.github.standobyte.jojo.action.stand.MagiciansRedRedBind;
import com.github.standobyte.jojo.action.stand.SilverChariotDashAttack;
import com.github.standobyte.jojo.action.stand.SilverChariotLightAttack;
import com.github.standobyte.jojo.action.stand.SilverChariotMeleeBarrage;
import com.github.standobyte.jojo.action.stand.SilverChariotRapierLaunch;
import com.github.standobyte.jojo.action.stand.SilverChariotSweepingAttack;
import com.github.standobyte.jojo.action.stand.SilverChariotTakeOffArmor;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.StandEntityAction.AutoSummonMode;
import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.action.stand.StandEntityActionModifier;
import com.github.standobyte.jojo.action.stand.StandEntityBlock;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.action.stand.StandEntityLightAttack;
import com.github.standobyte.jojo.action.stand.StandEntityMeleeBarrage;
import com.github.standobyte.jojo.action.stand.StandEntityUnsummon;
import com.github.standobyte.jojo.action.stand.StarPlatinumInhale;
import com.github.standobyte.jojo.action.stand.StarPlatinumStarFinger;
import com.github.standobyte.jojo.action.stand.StarPlatinumUppercut;
import com.github.standobyte.jojo.action.stand.StarPlatinumZoom;
import com.github.standobyte.jojo.action.stand.TheWorldBarrage;
import com.github.standobyte.jojo.action.stand.TheWorldHeavyPunch;
import com.github.standobyte.jojo.action.stand.TheWorldKick;
import com.github.standobyte.jojo.action.stand.TheWorldTSHeavyAttack;
import com.github.standobyte.jojo.action.stand.TheWorldTimeStop;
import com.github.standobyte.jojo.action.stand.TheWorldTimeStopInstant;
import com.github.standobyte.jojo.action.stand.TimeResume;
import com.github.standobyte.jojo.action.stand.TimeStop;
import com.github.standobyte.jojo.action.stand.TimeStopInstant;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;
import com.github.standobyte.jojo.entity.stand.stands.CrazyDiamondEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.entity.stand.stands.StarPlatinumEntity;
import com.github.standobyte.jojo.entity.stand.stands.TheWorldEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.power.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.power.stand.type.BoyIIManStandType;
import com.github.standobyte.jojo.power.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Jump to the start of a Stand's intialization:
 *             {@link ModStandsInit#STAR_PLATINUM_PUNCH}  Star Platinum
 *                 {@link ModStandsInit#THE_WORLD_PUNCH}  The World
 *  {@link ModStandsInit#HIEROPHANT_GREEN_STRING_ATTACK}  Hierophant Green
 * {@link ModStandsInit#SILVER_CHARIOT_NO_RAPIER_ATTACK}  Silver Chariot
 *             {@link ModStandsInit#MAGICIANS_RED_PUNCH}  Magician's Red
 *             {@link ModStandsInit#CRAZY_DIAMOND_PUNCH}  Crazy Diamond
 *
 */
public class ModStandsInit {
    public static final ITextComponent PART_3_NAME = new TranslationTextComponent("jojo.story_part.3").withStyle(TextFormatting.DARK_PURPLE);
    public static final ITextComponent PART_4_NAME = new TranslationTextComponent("jojo.story_part.4").withStyle(TextFormatting.RED);
    public static final ITextComponent PART_5_NAME = new TranslationTextComponent("jojo.story_part.5").withStyle(TextFormatting.GOLD);
    public static final ITextComponent PART_6_NAME = new TranslationTextComponent("jojo.story_part.6").withStyle(TextFormatting.AQUA);
    
    public static final ITextComponent PART_7_NAME = new TranslationTextComponent("jojo.story_part.7").withStyle(TextFormatting.LIGHT_PURPLE);
    public static final ITextComponent PART_8_NAME = new TranslationTextComponent("jojo.story_part.8").withStyle(TextFormatting.WHITE);
    public static final ITextComponent PART_9_NAME = new TranslationTextComponent("jojo.story_part.9").withStyle(TextFormatting.BLUE);

    public static final DeferredRegister<StandType<?>> STAND_TYPES = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), JojoMod.MOD_ID);
    
    public static final RegistryObject<StandEntityAction> UNSUMMON_STAND_ENTITY = ACTIONS.register("stand_entity_unsummon", 
            () -> new StandEntityUnsummon());

    public static final RegistryObject<StandEntityAction> BLOCK_STAND_ENTITY = ACTIONS.register("stand_entity_block", 
            () -> new StandEntityBlock() {
                @Override
                public StandRelativeOffset getOffsetFromUser(IStandPower standPower, StandEntity standEntity, StandEntityTask task) {
                    return null;
                }
            });
    
    
    
// ======================================== Star Platinum ========================================
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_PUNCH = ACTIONS.register("star_platinum_punch", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder()
                    .punchSound(ModSounds.STAR_PLATINUM_PUNCH_LIGHT)
                    .standSound(Phase.WINDUP, ModSounds.STAR_PLATINUM_ORA)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_BARRAGE = ACTIONS.register("star_platinum_barrage", 
            () -> new StandEntityMeleeBarrage(new StandEntityMeleeBarrage.Builder()
                    .barrageHitSound(ModSounds.STAR_PLATINUM_PUNCH_BARRAGE)
                    .standSound(ModSounds.STAR_PLATINUM_ORA_ORA_ORA)));
    
    public static final RegistryObject<StandEntityHeavyAttack> STAR_PLATINUM_UPPERCUT = ACTIONS.register("star_platinum_uppercut", 
            () -> new StarPlatinumUppercut(new StandEntityHeavyAttack.Builder()
                    .punchSound(ModSounds.STAR_PLATINUM_PUNCH_HEAVY)
                    .standSound(Phase.WINDUP, ModSounds.STAR_PLATINUM_ORA_LONG)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_HEAVY_PUNCH = ACTIONS.register("star_platinum_heavy_punch", 
            () -> new StandEntityHeavyAttack(new StandEntityHeavyAttack.Builder()
                    .punchSound(ModSounds.STAR_PLATINUM_PUNCH_HEAVY)
                    .standSound(Phase.WINDUP, ModSounds.STAR_PLATINUM_ORA_LONG)
                    .partsRequired(StandPart.ARMS)
                    .setFinisherVariation(STAR_PLATINUM_UPPERCUT)
                    .shiftVariationOf(STAR_PLATINUM_PUNCH).shiftVariationOf(STAR_PLATINUM_BARRAGE)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_STAR_FINGER = ACTIONS.register("star_platinum_star_finger", 
            () -> new StarPlatinumStarFinger(new StandEntityAction.Builder().staminaCost(375).standPerformDuration(20).cooldown(20, 60)
                    .ignoresPerformerStun().resolveLevelToUnlock(3)
                    .standOffsetFront().standPose(StandPose.RANGED_ATTACK)
                    .shout(ModSounds.JOTARO_STAR_FINGER).standSound(Phase.PERFORM, ModSounds.STAR_PLATINUM_STAR_FINGER)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_BLOCK = ACTIONS.register("star_platinum_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_ZOOM = ACTIONS.register("star_platinum_zoom", 
            () -> new StarPlatinumZoom(new StandEntityAction.Builder()
                    .ignoresPerformerStun()
                    .standOffsetFromUser(-0.25, -0.25, -0.3)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_INHALE = ACTIONS.register("star_platinum_inhale", 
            () -> new StarPlatinumInhale(new StandEntityAction.Builder().holdType(80).staminaCostTick(2F).cooldown(120)
                    .ignoresPerformerStun().resolveLevelToUnlock(2)
                    .standOffsetFromUser(0, -0.25).standSound(ModSounds.STAR_PLATINUM_INHALE)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    public static final RegistryObject<TimeStop> STAR_PLATINUM_TIME_STOP = ACTIONS.register("star_platinum_time_stop", 
            () -> new TimeStop(new StandAction.Builder().holdToFire(40, false).staminaCost(250).staminaCostTick(7.5F)
                    .isTrained().resolveLevelToUnlock(4)
                    .ignoresPerformerStun().autoSummonStand()
                    .shout(ModSounds.JOTARO_STAR_PLATINUM_THE_WORLD)
                    .partsRequired(StandPart.MAIN_BODY))
            .timeStopSound(ModSounds.STAR_PLATINUM_TIME_STOP)
            .addTimeResumeVoiceLine(ModSounds.JOTARO_TIME_RESUMES).timeResumeSound(ModSounds.STAR_PLATINUM_TIME_RESUME));
    
    public static final RegistryObject<TimeResume> TIME_RESUME = ACTIONS.register("time_resume", 
            () -> new TimeResume(new StandAction.Builder()));
    
    public static final RegistryObject<StandAction> STAR_PLATINUM_TIME_STOP_BLINK = ACTIONS.register("star_platinum_ts_blink", 
            () -> new TimeStopInstant(new StandAction.Builder()
                    .resolveLevelToUnlock(4).isTrained()
                    .ignoresPerformerStun()
                    .partsRequired(StandPart.MAIN_BODY)
                    .shiftVariationOf(STAR_PLATINUM_TIME_STOP), 
                    STAR_PLATINUM_TIME_STOP, ModSounds.STAR_PLATINUM_TIME_STOP_BLINK));
    
    
    public static final EntityStandRegistryObject<EntityStandType<TimeStopperStandStats>, StandEntityType<StarPlatinumEntity>> STAND_STAR_PLATINUM = 
            new EntityStandRegistryObject<>("star_platinum", 
                    STAND_TYPES, 
                    () -> new EntityStandType<>(
                            0x8E45FF, PART_3_NAME,

                            new StandAction[] {
                                    STAR_PLATINUM_PUNCH.get(), 
                                    STAR_PLATINUM_BARRAGE.get(), 
                                    STAR_PLATINUM_STAR_FINGER.get()},
                            new StandAction[] {
                                    STAR_PLATINUM_BLOCK.get(), 
                                    STAR_PLATINUM_ZOOM.get(), 
                                    STAR_PLATINUM_INHALE.get(), 
                                    STAR_PLATINUM_TIME_STOP.get()},

                            TimeStopperStandStats.class, new TimeStopperStandStats.Builder()
                            .tier(6)
                            .power(16.0)
                            .speed(16.0)
                            .range(2.0, 10.0)
                            .durability(16.0)
                            .precision(16.0)
                            .timeStopMaxTicks(100, 180)
                            .timeStopLearningPerTick(0.25F)
                            .timeStopDecayPerDay(0F)
                            .timeStopCooldownPerTick(3F)
                            .build("Star Platinum"), 

                            new StandType.StandTypeOptionals()
                            .addSummonShout(ModSounds.JOTARO_STAR_PLATINUM)
                            .addOst(ModSounds.STAR_PLATINUM_OST)), 

                    ENTITIES, 
                    () -> new StandEntityType<StarPlatinumEntity>(StarPlatinumEntity::new, 0.7F, 2.1F)
                    .summonSound(ModSounds.STAR_PLATINUM_SUMMON)
                    .unsummonSound(ModSounds.STAR_PLATINUM_UNSUMMON))
            .withDefaultStandAttributes();
    
    
    
// ======================================== The World ========================================
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_PUNCH = ACTIONS.register("the_world_punch", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder()
                    .punchSound(ModSounds.THE_WORLD_PUNCH_LIGHT)
                    .standSound(Phase.WINDUP, ModSounds.DIO_MUDA)));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_BARRAGE = ACTIONS.register("the_world_barrage", 
            () -> new TheWorldBarrage(new StandEntityMeleeBarrage.Builder()
                    .barrageHitSound(ModSounds.THE_WORLD_PUNCH_BARRAGE)
                    .standSound(ModSounds.THE_WORLD_MUDA_MUDA_MUDA).shout(ModSounds.DIO_MUDA_MUDA), ModSounds.DIO_WRY));

    public static final RegistryObject<StandEntityHeavyAttack> THE_WORLD_KICK = ACTIONS.register("the_world_kick", 
            () -> new TheWorldKick(new StandEntityHeavyAttack.Builder()
                    .punchSound(ModSounds.THE_WORLD_KICK_HEAVY)
                    .shout(ModSounds.DIO_DIE)
                    .partsRequired(StandPart.LEGS)));

    public static final RegistryObject<StandEntityHeavyAttack> THE_WORLD_HEAVY_PUNCH = ACTIONS.register("the_world_heavy_punch", 
            () -> new TheWorldHeavyPunch(new StandEntityHeavyAttack.Builder()
                    .punchSound(ModSounds.THE_WORLD_PUNCH_HEAVY)
                    .shout(ModSounds.DIO_DIE)
                    .partsRequired(StandPart.ARMS)
                    .setFinisherVariation(THE_WORLD_KICK)
                    .shiftVariationOf(THE_WORLD_PUNCH).shiftVariationOf(THE_WORLD_BARRAGE)));

    public static final RegistryObject<StandEntityAction> THE_WORLD_BLOCK = ACTIONS.register("the_world_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<TimeStop> THE_WORLD_TIME_STOP = ACTIONS.register("the_world_time_stop", 
            () -> new TheWorldTimeStop(new StandAction.Builder().holdToFire(30, false).staminaCost(250).staminaCostTick(7.5F)
                    .resolveLevelToUnlock(2).isTrained()
                    .ignoresPerformerStun()
                    .shout(ModSounds.DIO_THE_WORLD)
                    .partsRequired(StandPart.MAIN_BODY))
            .voiceLineWithStandSummoned(ModSounds.DIO_TIME_STOP).timeStopSound(ModSounds.THE_WORLD_TIME_STOP)
            .addTimeResumeVoiceLine(ModSounds.DIO_TIME_RESUMES, true).addTimeResumeVoiceLine(ModSounds.DIO_TIMES_UP, false)
            .timeResumeSound(ModSounds.THE_WORLD_TIME_RESUME));
    
    public static final RegistryObject<TimeStopInstant> THE_WORLD_TIME_STOP_BLINK = ACTIONS.register("the_world_ts_blink", 
            () -> new TheWorldTimeStopInstant(new StandAction.Builder()
                    .resolveLevelToUnlock(2).isTrained()
                    .ignoresPerformerStun()
                    .partsRequired(StandPart.MAIN_BODY)
                    .shiftVariationOf(THE_WORLD_TIME_STOP), 
                    THE_WORLD_TIME_STOP, ModSounds.THE_WORLD_TIME_STOP_BLINK));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_TS_PUNCH = ACTIONS.register("the_world_ts_punch", 
            () -> new TheWorldTSHeavyAttack(new StandEntityAction.Builder().resolveLevelToUnlock(3).standUserWalkSpeed(1.0F)
                    .standPose(TheWorldTSHeavyAttack.TS_PUNCH_POSE).standWindupDuration(5).cooldown(50)
                    .partsRequired(StandPart.MAIN_BODY, StandPart.ARMS), THE_WORLD_HEAVY_PUNCH, THE_WORLD_TIME_STOP_BLINK));
    
    
    public static final EntityStandRegistryObject<EntityStandType<TimeStopperStandStats>, StandEntityType<TheWorldEntity>> STAND_THE_WORLD = 
            new EntityStandRegistryObject<>("the_world", 
                    STAND_TYPES,
                    () -> new EntityStandType<>(
                            0xFFD800, PART_3_NAME,

                            new StandAction[] {
                                    THE_WORLD_PUNCH.get(), 
                                    THE_WORLD_BARRAGE.get(), 
                                    THE_WORLD_TS_PUNCH.get()},
                            new StandAction[] {
                                    THE_WORLD_BLOCK.get(), 
                                    THE_WORLD_TIME_STOP.get()},

                            TimeStopperStandStats.class, new TimeStopperStandStats.Builder()
                            .tier(6)
                            .power(16.0)
                            .speed(16.0)
                            .range(2.0, 10.0)
                            .durability(16.0)
                            .precision(12.0)
                            .timeStopMaxTicks(100, 180)
                            .timeStopLearningPerTick(0.1F)
                            .timeStopDecayPerDay(0F)
                            .timeStopCooldownPerTick(3F)
                            .build("The World"), 

                            new StandType.StandTypeOptionals()
                            .addSummonShout(ModSounds.DIO_THE_WORLD)
                            .addOst(ModSounds.THE_WORLD_OST)
                            /*.addItemOnResolveLevel(4, new ItemStack(ModItems.ROAD_ROLLER.get()))*/), 

                    ENTITIES, 
                    () -> new StandEntityType<TheWorldEntity>(TheWorldEntity::new, 0.7F, 2.1F)
                    .summonSound(ModSounds.THE_WORLD_SUMMON))
            .withDefaultStandAttributes();
    
    
    
// ======================================== Hierophant Green ========================================
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_STRING_ATTACK = ACTIONS.register("hierophant_green_attack", 
            () -> new HierophantGreenStringAttack(new StandEntityAction.Builder().staminaCost(75).standPerformDuration(16)
                    .standSound(ModSounds.HIEROPHANT_GREEN_TENTACLES)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_STRING_BIND = ACTIONS.register("hierophant_green_attack_binding", 
            () -> new HierophantGreenStringAttack(new StandEntityAction.Builder().staminaCost(75).standPerformDuration(24).cooldown(25, 100, 0.5F)
                    .standSound(ModSounds.HIEROPHANT_GREEN_TENTACLES)
                    .partsRequired(StandPart.MAIN_BODY)
                    .shiftVariationOf(HIEROPHANT_GREEN_STRING_ATTACK)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_EMERALD_SPLASH = ACTIONS.register("hierophant_green_emerald_splash", 
            () -> new HierophantGreenEmeraldSplash(new StandEntityAction.Builder().standPerformDuration(30).standRecoveryTicks(20).staminaCostTick(3)
                    .resolveLevelToUnlock(1).isTrained()
                    .standOffsetFront().standPose(StandPose.RANGED_ATTACK).shout(ModSounds.KAKYOIN_EMERALD_SPLASH).standSound(ModSounds.HIEROPHANT_GREEN_EMERALD_SPLASH)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED = ACTIONS.register("hierophant_green_es_concentrated", 
            () -> new HierophantGreenEmeraldSplash(new StandEntityAction.Builder().staminaCostTick(6).standPerformDuration(5).standRecoveryTicks(20).cooldown(5, 60)
                    .noResolveUnlock()
                    .standOffsetFront().standPose(StandPose.RANGED_ATTACK).shout(ModSounds.KAKYOIN_EMERALD_SPLASH).standSound(ModSounds.HIEROPHANT_GREEN_EMERALD_SPLASH)
                    .partsRequired(StandPart.ARMS)
                    .shiftVariationOf(HIEROPHANT_GREEN_EMERALD_SPLASH)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_BLOCK = ACTIONS.register("hierophant_green_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_GRAPPLE = ACTIONS.register("hierophant_green_grapple", 
            () -> new HierophantGreenGrapple(new StandEntityAction.Builder().staminaCostTick(1).holdType().standUserWalkSpeed(1.0F)
                    .resolveLevelToUnlock(2)
                    .standPose(HierophantGreenGrapple.GRAPPLE_POSE).standOffsetFromUser(-0.5, 0.25)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_GRAPPLE_ENTITY = ACTIONS.register("hierophant_green_grapple_entity", 
            () -> new HierophantGreenGrapple(new StandEntityAction.Builder().staminaCostTick(1).holdType().standUserWalkSpeed(1.0F)
                    .resolveLevelToUnlock(2)
                    .standPose(HierophantGreenGrapple.GRAPPLE_POSE)
                    .partsRequired(StandPart.ARMS)
                    .shiftVariationOf(HIEROPHANT_GREEN_GRAPPLE)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_BARRIER = ACTIONS.register("hierophant_green_barrier", 
            () -> new HierophantGreenBarrier(new StandEntityAction.Builder()
                    .resolveLevelToUnlock(3)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    
    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<HierophantGreenEntity>> STAND_HIEROPHANT_GREEN = 
            new EntityStandRegistryObject<>("hierophant_green", 
                    STAND_TYPES, 
                    () -> new EntityStandType<>(
                            0x00B319, PART_3_NAME,

                            new StandAction[] {
                                    HIEROPHANT_GREEN_STRING_ATTACK.get(), 
                                    HIEROPHANT_GREEN_EMERALD_SPLASH.get()},
                            new StandAction[] {
                                    HIEROPHANT_GREEN_BLOCK.get(), 
                                    HIEROPHANT_GREEN_GRAPPLE.get(), 
                                    HIEROPHANT_GREEN_BARRIER.get()},

                            StandStats.class, new StandStats.Builder()
                            .tier(5)
                            .power(8.0)
                            .speed(12.0)
                            .range(50.0, 100.0)
                            .durability(10.0)
                            .precision(8.0)
                            .build("Hierophant Green"), 

                            new StandType.StandTypeOptionals()
                            .addSummonShout(ModSounds.KAKYOIN_HIEROPHANT_GREEN)
                            .addOst(ModSounds.HIEROPHANT_GREEN_OST)), 

                    ENTITIES, 
                    () -> new StandEntityType<HierophantGreenEntity>(HierophantGreenEntity::new, 0.6F, 1.9F)
                    .summonSound(ModSounds.HIEROPHANT_GREEN_SUMMON))
            .withDefaultStandAttributes();
    
    
    
// ======================================== Silver Chariot ========================================
    
    public static final RegistryObject<StandEntityLightAttack> SILVER_CHARIOT_NO_RAPIER_ATTACK = ACTIONS.register("silver_chariot_no_rapier_attack", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder()));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_ATTACK = ACTIONS.register("silver_chariot_attack", 
            () -> new SilverChariotLightAttack(new StandEntityLightAttack.Builder()
                    .punchSound(() -> null).standSound(ModSounds.SILVER_CHARIOT_SWEEP_LIGHT), 
                    SILVER_CHARIOT_NO_RAPIER_ATTACK));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_RAPIER_BARRAGE = ACTIONS.register("silver_chariot_barrage", 
            () -> new SilverChariotMeleeBarrage(new StandEntityMeleeBarrage.Builder()
                    .shout(ModSounds.POLNAREFF_HORA_HORA_HORA).barrageHitSound(ModSounds.SILVER_CHARIOT_BARRAGE)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityHeavyAttack> SILVER_CHARIOT_SWEEPING_ATTACK = ACTIONS.register("silver_chariot_sweeping_attack", 
            () -> new SilverChariotSweepingAttack(new StandEntityHeavyAttack.Builder().standPerformDuration(3)
                    .punchSound(() -> null)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_DASH_ATTACK = ACTIONS.register("silver_chariot_dash_attack", 
            () -> new SilverChariotDashAttack(new StandEntityHeavyAttack.Builder()
                    .punchSound(() -> null).standSound(ModSounds.SILVER_CHARIOT_DASH)
                    .partsRequired(StandPart.MAIN_BODY, StandPart.ARMS)
                    .setFinisherVariation(SILVER_CHARIOT_SWEEPING_ATTACK)
                    .shiftVariationOf(SILVER_CHARIOT_ATTACK).shiftVariationOf(SILVER_CHARIOT_RAPIER_BARRAGE)));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_RAPIER_LAUNCH = ACTIONS.register("silver_chariot_rapier_launch", 
            () -> new SilverChariotRapierLaunch(new StandEntityAction.Builder().cooldown(100)
                    .ignoresPerformerStun()
                    .resolveLevelToUnlock(2)
                    .standOffsetFromUser(0, 0.25).standPose(StandPose.RANGED_ATTACK).standSound(ModSounds.SILVER_CHARIOT_RAPIER_SHOT)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_BLOCK = ACTIONS.register("silver_chariot_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_TAKE_OFF_ARMOR = ACTIONS.register("silver_chariot_take_off_armor", 
            () -> new SilverChariotTakeOffArmor(new StandEntityAction.Builder()
                    .resolveLevelToUnlock(3)
                    .standSound(ModSounds.SILVER_CHARIOT_ARMOR_OFF)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    
    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<SilverChariotEntity>> STAND_SILVER_CHARIOT = 
            new EntityStandRegistryObject<>("silver_chariot", 
                    STAND_TYPES, 
                    () -> new EntityStandType<>(
                            0xBEC8D6, PART_3_NAME,
                            
                            new StandAction[] {
                                    SILVER_CHARIOT_ATTACK.get(), 
                                    SILVER_CHARIOT_RAPIER_BARRAGE.get(), 
                                    SILVER_CHARIOT_RAPIER_LAUNCH.get()},
                            new StandAction[] {
                                    SILVER_CHARIOT_BLOCK.get(), 
                                    SILVER_CHARIOT_TAKE_OFF_ARMOR.get()},
                            
                            StandStats.class, new StandStats.Builder()
                            .tier(5)
                            .power(9.0)
                            .speed(14.0)
                            .range(10.0)
                            .durability(12.0)
                            .precision(16.0)
                            .build("Silver Chariot"), 

                            new StandType.StandTypeOptionals()
                            .addSummonShout(ModSounds.POLNAREFF_SILVER_CHARIOT)
                            .addOst(ModSounds.SILVER_CHARIOT_OST)), 

                    ENTITIES, 
                    () -> new StandEntityType<SilverChariotEntity>(SilverChariotEntity::new, 0.6F, 1.95F)
                    .summonSound(ModSounds.SILVER_CHARIOT_SUMMON)
                    .unsummonSound(ModSounds.SILVER_CHARIOT_UNSUMMON))
            .withDefaultStandAttributes();
    
    
    
// ======================================== Magician's Red ========================================
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_PUNCH = ACTIONS.register("magicians_red_punch", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder()
                    .punchSound(ModSounds.MAGICIANS_RED_PUNCH_LIGHT)));

    public static final RegistryObject<StandEntityHeavyAttack> MAGICIANS_RED_KICK = ACTIONS.register("magicians_red_kick", 
            () -> new MagiciansRedKick(new StandEntityHeavyAttack.Builder()
                    .punchSound(ModSounds.MAGICIANS_RED_KICK_HEAVY)
                    .partsRequired(StandPart.LEGS)));

    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_HEAVY_PUNCH = ACTIONS.register("magicians_red_heavy_punch", 
            () -> new StandEntityHeavyAttack(new StandEntityHeavyAttack.Builder()
                    .punchSound(ModSounds.MAGICIANS_RED_PUNCH_HEAVY)
                    .partsRequired(StandPart.ARMS)
                    .setFinisherVariation(MAGICIANS_RED_KICK)
                    .shiftVariationOf(MAGICIANS_RED_PUNCH)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_FLAME_BURST = ACTIONS.register("magicians_red_flame_burst", 
            () -> new MagiciansRedFlameBurst(new StandEntityAction.Builder().holdType()
                    .staminaCostTick(3)
                    .standOffsetFront().standPose(MagiciansRedFlameBurst.FLAME_BURST_POSE)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_FIREBALL = ACTIONS.register("magicians_red_fireball", 
            () -> new MagiciansRedFireball(new StandEntityAction.Builder().staminaCost(75).standPerformDuration(3)
                    .resolveLevelToUnlock(2)
                    .standPose(MagiciansRedFlameBurst.FLAME_BURST_POSE).standSound(ModSounds.MAGICIANS_RED_FIREBALL)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_CROSSFIRE_HURRICANE = ACTIONS.register("magicians_red_crossfire_hurricane", 
            () -> new MagiciansRedCrossfireHurricane(new StandEntityAction.Builder().holdToFire(20, false).staminaCost(500)
                    .resolveLevelToUnlock(4).isTrained()
                    .standPose(MagiciansRedFlameBurst.FLAME_BURST_POSE).shout(ModSounds.AVDOL_CROSSFIRE_HURRICANE).standSound(ModSounds.MAGICIANS_RED_FIRE_BLAST)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_CROSSFIRE_HURRICANE_SPECIAL = ACTIONS.register("magicians_red_ch_special", 
            () -> new MagiciansRedCrossfireHurricane(new StandEntityAction.Builder().holdToFire(20, false).staminaCost(500)
                    .noResolveUnlock()
                    .standPose(MagiciansRedFlameBurst.FLAME_BURST_POSE).shout(ModSounds.AVDOL_CROSSFIRE_HURRICANE_SPECIAL).standSound(ModSounds.MAGICIANS_RED_FIRE_BLAST)
                    .partsRequired(StandPart.MAIN_BODY)
                    .shiftVariationOf(MAGICIANS_RED_CROSSFIRE_HURRICANE)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_BLOCK = ACTIONS.register("magicians_red_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_RED_BIND = ACTIONS.register("magicians_red_red_bind", 
            () -> new MagiciansRedRedBind(new StandEntityAction.Builder().staminaCostTick(1).holdType().heldWalkSpeed(0.3F)
                    .resolveLevelToUnlock(1)
                    .standOffsetFront().standPose(MagiciansRedRedBind.RED_BIND_POSE)
                    .shout(ModSounds.AVDOL_RED_BIND).standSound(ModSounds.MAGICIANS_RED_FIRE_BLAST)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandAction> MAGICIANS_RED_DETECTOR = ACTIONS.register("magicians_red_detector", 
            () -> new MagiciansRedDetector(new StandAction.Builder().autoSummonStand()
                    .resolveLevelToUnlock(3)
                    .partsRequired(StandPart.MAIN_BODY)));
    
    
    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<MagiciansRedEntity>> STAND_MAGICIANS_RED = 
            new EntityStandRegistryObject<>("magicians_red", 
                    STAND_TYPES, 
                    () -> new EntityStandType<>(
                            0xDE203A, PART_3_NAME,
                            
                            new StandAction[] {
                                    MAGICIANS_RED_PUNCH.get(), 
                                    MAGICIANS_RED_FLAME_BURST.get(), 
                                    MAGICIANS_RED_FIREBALL.get(), 
                                    MAGICIANS_RED_CROSSFIRE_HURRICANE.get()},
                            new StandAction[] {
                                    MAGICIANS_RED_BLOCK.get(), 
                                    MAGICIANS_RED_RED_BIND.get(), 
                                    MAGICIANS_RED_DETECTOR.get()},
                            
                            StandStats.class, new StandStats.Builder()
                            .tier(5)
                            .power(12.0)
                            .speed(12.0)
                            .range(5.0, 10.0)
                            .durability(12.0)
                            .precision(8.0)
                            .build("Magician's Red"), 
                            
                            new StandType.StandTypeOptionals()
                            .addSummonShout(ModSounds.AVDOL_MAGICIANS_RED)
                            .addOst(ModSounds.MAGICIANS_RED_OST)), 
                    
                    ENTITIES, 
                    () -> new StandEntityType<MagiciansRedEntity>(MagiciansRedEntity::new, 0.65F, 1.95F)
                    .summonSound(ModSounds.MAGICIANS_RED_SUMMON))
            .withDefaultStandAttributes();
    
    
    
// ======================================== Crazy Diamond ========================================
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_PUNCH = ACTIONS.register("crazy_diamond_punch", 
            () -> new StandEntityLightAttack(new StandEntityLightAttack.Builder()
                    .punchSound(ModSounds.CRAZY_DIAMOND_PUNCH_LIGHT)
                    .standSound(Phase.WINDUP, ModSounds.CRAZY_DIAMOND_DORA)
                    ));
    
    public static final RegistryObject<StandEntityMeleeBarrage> CRAZY_DIAMOND_BARRAGE = ACTIONS.register("crazy_diamond_barrage", 
            () -> new StandEntityMeleeBarrage(new StandEntityMeleeBarrage.Builder()
                    .barrageHitSound(ModSounds.CRAZY_DIAMOND_PUNCH_BARRAGE)
                    .standSound(ModSounds.CRAZY_DIAMOND_DORARARA)));
    
    public static final RegistryObject<StandEntityHeavyAttack> CRAZY_DIAMOND_COMBO_PUNCH = ACTIONS.register("crazy_diamond_misshaping_punch", 
            () -> new CrazyDiamondMisshapingPunch(new StandEntityHeavyAttack.Builder()
                    .punchSound(ModSounds.CRAZY_DIAMOND_PUNCH_HEAVY)
                    .standSound(Phase.WINDUP, ModSounds.CRAZY_DIAMOND_DORA_LONG)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityActionModifier> CRAZY_DIAMOND_MISSHAPE_FACE = ACTIONS.register("crazy_diamond_misshape_face", 
            () -> new CrazyDiamondMisshapeBodyPart(new StandAction.Builder().staminaCost(50)));
    
    public static final RegistryObject<StandEntityActionModifier> CRAZY_DIAMOND_MISSHAPE_ARMS = ACTIONS.register("crazy_diamond_misshape_arms", 
            () -> new CrazyDiamondMisshapeBodyPart(new StandAction.Builder().staminaCost(50)));
    
    public static final RegistryObject<StandEntityActionModifier> CRAZY_DIAMOND_MISSHAPE_LEGS = ACTIONS.register("crazy_diamond_misshape_legs", 
            () -> new CrazyDiamondMisshapeBodyPart(new StandAction.Builder().staminaCost(50)));
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_HEAVY_PUNCH = ACTIONS.register("crazy_diamond_heavy_punch", 
            () -> new CrazyDiamondHeavyPunch(new StandEntityHeavyAttack.Builder()
                    .punchSound(ModSounds.CRAZY_DIAMOND_PUNCH_HEAVY)
                    .standSound(Phase.WINDUP, ModSounds.CRAZY_DIAMOND_DORA_LONG)
                    .partsRequired(StandPart.ARMS)
                    .setFinisherVariation(CRAZY_DIAMOND_COMBO_PUNCH)
                    .shiftVariationOf(CRAZY_DIAMOND_PUNCH).shiftVariationOf(CRAZY_DIAMOND_BARRAGE)));
    
    public static final RegistryObject<StandEntityActionModifier> CRAZY_DIAMOND_LEAVE_OBJECT = ACTIONS.register("crazy_diamond_leave_object", 
            () -> new CrazyDiamondLeaveObject(new StandAction.Builder().staminaCost(50)));
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_BLOCK_BULLET = ACTIONS.register("crazy_diamond_block_bullet", 
            () -> new CrazyDiamondBlockBullet(new StandEntityAction.Builder().standWindupDuration(15).staminaCost(40).staminaCostTick(2F)
                    .resolveLevelToUnlock(4)
                    .standPose(CrazyDiamondBlockBullet.BLOCK_BULLET_SHOT_POSE)
                    .standSound(Phase.WINDUP, ModSounds.CRAZY_DIAMOND_FIX_STARTED).standSound(Phase.PERFORM, ModSounds.CRAZY_DIAMOND_BULLET_SHOT)
                    .standOffsetFromUser(0.25, -0.5, 0)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_BLOOD_CUTTER = ACTIONS.register("crazy_diamond_blood_cutter", 
            () -> new CrazyDiamondBloodCutter(new StandEntityAction.Builder().standWindupDuration(5).standRecoveryTicks(5).staminaCost(25).cooldown(300)
                    .resolveLevelToUnlock(4)
                    .standPose(CrazyDiamondBloodCutter.BLOOD_CUTTER_SHOT_POSE)
                    .standSound(Phase.WINDUP, ModSounds.CRAZY_DIAMOND_FIX_STARTED, ModSounds.CRAZY_DIAMOND_DORA).standSound(Phase.PERFORM, ModSounds.CRAZY_DIAMOND_BLOOD_CUTTER_SHOT)
                    .standOffsetFromUser(-0.1, -0.5)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_BLOCK = ACTIONS.register("crazy_diamond_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<CrazyDiamondRepairItem> CRAZY_DIAMOND_REPAIR = ACTIONS.register("crazy_diamond_repair", 
            () -> new CrazyDiamondRepairItem(new StandEntityAction.Builder().holdType().staminaCostTick(0.2F)
                    .resolveLevelToUnlock(0).isTrained()
                    .standOffsetFromUser(0.667, 0.2, 0).standPose(CrazyDiamondRepairItem.ITEM_FIX_POSE)
                    .standSound(Phase.PERFORM, ModSounds.CRAZY_DIAMOND_FIX_STARTED)
                    .standAutoSummonMode(AutoSummonMode.OFF_ARM)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_PREVIOUS_STATE = ACTIONS.register("crazy_diamond_previous_state", 
            () -> new CrazyDiamondPreviousState(new StandEntityAction.Builder().holdType().staminaCostTick(0.2F)
                    .noResolveUnlock()
                    .standOffsetFromUser(0.667, 0.2, 0).standPose(CrazyDiamondRepairItem.ITEM_FIX_POSE)
                    .standSound(Phase.PERFORM, ModSounds.CRAZY_DIAMOND_FIX_STARTED).barrageVisuals(CRAZY_DIAMOND_BARRAGE)
                    .standAutoSummonMode(AutoSummonMode.OFF_ARM)
                    .partsRequired(StandPart.ARMS)
                    .shiftVariationOf(CRAZY_DIAMOND_REPAIR)));
    
    public static final RegistryObject<CrazyDiamondHeal> CRAZY_DIAMOND_HEAL = ACTIONS.register("crazy_diamond_heal", 
            () -> new CrazyDiamondHeal(new StandEntityAction.Builder().holdType().staminaCostTick(1)
                    .resolveLevelToUnlock(1)
                    .standSound(Phase.PERFORM, ModSounds.CRAZY_DIAMOND_FIX_STARTED).barrageVisuals(CRAZY_DIAMOND_BARRAGE)
                    .standAutoSummonMode(AutoSummonMode.MAIN_ARM)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_RESTORE_TERRAIN = ACTIONS.register("crazy_diamond_restore_terrain", 
            () -> new CrazyDiamondRestoreTerrain(new StandEntityAction.Builder().holdType().staminaCostTick(2) // cost per block rather than per tick
                    .resolveLevelToUnlock(2)
                    .shout(ModSounds.JOSUKE_FIX).standSound(Phase.PERFORM, ModSounds.CRAZY_DIAMOND_FIX_STARTED)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE = ACTIONS.register("crazy_diamond_anchor_move", 
            () -> new CrazyDiamondBlockCheckpointMove(new StandEntityAction.Builder().holdType().staminaCostTick(1)
                    .resolveLevelToUnlock(3)
                    .standSound(Phase.PERFORM, ModSounds.CRAZY_DIAMOND_FIX_STARTED)
                    .standAutoSummonMode(AutoSummonMode.OFF_ARM)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> CRAZY_DIAMOND_BLOCK_ANCHOR_MAKE = ACTIONS.register("crazy_diamond_anchor_make", 
            () -> new CrazyDiamondBlockCheckpointMake(new StandEntityAction.Builder().standWindupDuration(10).standRecoveryTicks(5).staminaCost(25)
                    .resolveLevelToUnlock(3)
                    .standPose(StandPose.HEAVY_ATTACK_COMBO)
                    .partsRequired(StandPart.ARMS)
                    .shiftVariationOf(CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE)));
    
    
    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<CrazyDiamondEntity>> STAND_CRAZY_DIAMOND = 
            new EntityStandRegistryObject<>("crazy_diamond", 
                    STAND_TYPES, 
                    () -> new EntityStandType<>(
                            0xEA6E7C, PART_4_NAME,
                            
                            new StandAction[] {
                                    CRAZY_DIAMOND_PUNCH.get(), 
                                    CRAZY_DIAMOND_BARRAGE.get(), 
                                    CRAZY_DIAMOND_BLOCK_BULLET.get(), 
                                    CRAZY_DIAMOND_BLOOD_CUTTER.get()},
                            new StandAction[] {
                                    CRAZY_DIAMOND_BLOCK.get(), 
                                    CRAZY_DIAMOND_REPAIR.get(), 
                                    CRAZY_DIAMOND_HEAL.get(), 
                                    CRAZY_DIAMOND_RESTORE_TERRAIN.get(), 
                                    CRAZY_DIAMOND_BLOCK_ANCHOR_MOVE.get()},
                            
                            StandStats.class, new StandStats.Builder()
                            .tier(5)
                            .power(14.0)
                            .speed(14.0)
                            .range(2.0, 4.0)
                            .durability(12.0)
                            .precision(12.0)
                            .build("Crazy Diamond"), 
                            
                            new StandType.StandTypeOptionals()
                            .addSummonShout(ModSounds.JOSUKE_CRAZY_DIAMOND)
                            .addOst(ModSounds.CRAZY_DIAMOND_OST)),
                    
                    ENTITIES,
                    () -> new StandEntityType<CrazyDiamondEntity>(CrazyDiamondEntity::new, 0.65F, 1.95F)
                    .summonSound(ModSounds.CRAZY_DIAMOND_SUMMON)
                    .unsummonSound(ModSounds.CRAZY_DIAMOND_UNSUMMON))
            .withDefaultStandAttributes();
    
    
    
    public static final RegistryObject<StandType<StandStats>> BOY_II_MAN = STAND_TYPES.register("boy_ii_man", 
            () -> {
//                StandArrowEntity.EntityPierce.addBehavior(
//                        () -> RockPaperScissorsKidEntity::canTurnFromArrow, 
//                        () -> RockPaperScissorsKidEntity::turnFromArrow);
                return new BoyIIManStandType<>(
                        0x749FA5, PART_4_NAME,
                        
                        new StandAction[] {},
                        new StandAction[] {},
                        StandStats.class, new StandStats.Builder()
                        .tier(0)
                        .power(0)
                        .speed(0)
                        .range(0)
                        .durability(0)
                        .precision(0)
                        .build("Boy II Man"), 
                        
                        new StandType.StandTypeOptionals()
                        .setPlayerAccess(false));
            });
    
    
    
    public static final Supplier<EntityStandType<StandStats>> KILLER_QUEEN = () -> null;
    
}
