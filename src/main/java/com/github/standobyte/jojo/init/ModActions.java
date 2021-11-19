package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.actions.HamonAction;
import com.github.standobyte.jojo.action.actions.HamonBubbleBarrier;
import com.github.standobyte.jojo.action.actions.HamonBubbleCutter;
import com.github.standobyte.jojo.action.actions.HamonBubbleLauncher;
import com.github.standobyte.jojo.action.actions.HamonCutter;
import com.github.standobyte.jojo.action.actions.HamonDetector;
import com.github.standobyte.jojo.action.actions.HamonHealing;
import com.github.standobyte.jojo.action.actions.HamonLifeMagnetism;
import com.github.standobyte.jojo.action.actions.HamonOrganismInfusion;
import com.github.standobyte.jojo.action.actions.HamonOverdriveBarrage;
import com.github.standobyte.jojo.action.actions.HamonProjectileShield;
import com.github.standobyte.jojo.action.actions.HamonRepellingOverdrive;
import com.github.standobyte.jojo.action.actions.HamonScarletOverdrive;
import com.github.standobyte.jojo.action.actions.HamonSendoOverdrive;
import com.github.standobyte.jojo.action.actions.HamonSpeedBoost;
import com.github.standobyte.jojo.action.actions.HamonTornadoOverdrive;
import com.github.standobyte.jojo.action.actions.HamonWallClimbing;
import com.github.standobyte.jojo.action.actions.HamonZoomPunch;
import com.github.standobyte.jojo.action.actions.HierophantGreenBarrier;
import com.github.standobyte.jojo.action.actions.HierophantGreenGrapple;
import com.github.standobyte.jojo.action.actions.HierophantGreenStringAttack;
import com.github.standobyte.jojo.action.actions.MagiciansRedCrossfireHurricane;
import com.github.standobyte.jojo.action.actions.MagiciansRedDetector;
import com.github.standobyte.jojo.action.actions.MagiciansRedFireball;
import com.github.standobyte.jojo.action.actions.MagiciansRedRedBind;
import com.github.standobyte.jojo.action.actions.SilverChariotMeleeBarrage;
import com.github.standobyte.jojo.action.actions.SilverChariotRapierLaunch;
import com.github.standobyte.jojo.action.actions.SilverChariotTakeOffArmor;
import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.action.actions.StandEntityBlock;
import com.github.standobyte.jojo.action.actions.StandEntityHeldRangedAttack;
import com.github.standobyte.jojo.action.actions.StandEntityMeleeAttack;
import com.github.standobyte.jojo.action.actions.StandEntityMeleeBarrage;
import com.github.standobyte.jojo.action.actions.StandEntityRangedAttack;
import com.github.standobyte.jojo.action.actions.StarPlatinumZoom;
import com.github.standobyte.jojo.action.actions.TheWorldRoadRoller;
import com.github.standobyte.jojo.action.actions.TimeStop;
import com.github.standobyte.jojo.action.actions.TimeStopInstant;
import com.github.standobyte.jojo.action.actions.VampirismBloodDrain;
import com.github.standobyte.jojo.action.actions.VampirismBloodGift;
import com.github.standobyte.jojo.action.actions.VampirismDarkAura;
import com.github.standobyte.jojo.action.actions.VampirismFreeze;
import com.github.standobyte.jojo.action.actions.VampirismHamonSuicide;
import com.github.standobyte.jojo.action.actions.VampirismSpaceRipperStingyEyes;
import com.github.standobyte.jojo.action.actions.VampirismZombieSummon;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;
import com.github.standobyte.jojo.util.TimeHandler;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModActions {
    public static final DeferredRegister<Action> ACTIONS = DeferredRegister.create(Action.class, JojoMod.MOD_ID);
    
    public static final RegistryObject<HamonAction> HAMON_SENDO_OVERDRIVE = ACTIONS.register("hamon_sendo_overdrive", 
            () -> new HamonSendoOverdrive(new HamonAction.Builder().manaCost(1000).swingHand().needsBlockTarget()
                    .shout(Technique.JONATHAN, ModSounds.JONATHAN_SENDO_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_ZOOM_PUNCH = ACTIONS.register("hamon_zoom_punch", 
            () -> new HamonZoomPunch(new HamonAction.Builder().manaCost(800).cooldown(14).shout(Technique.JONATHAN, ModSounds.JONATHAN_ZOOM_PUNCH)
                    .shout(Technique.ZEPPELI, ModSounds.ZEPPELI_ZOOM_PUNCH).shout(Technique.JOSEPH, ModSounds.JOSEPH_ZOOM_PUNCH)));

    public static final RegistryObject<HamonAction> HAMON_SPEED_BOOST = ACTIONS.register("hamon_speed_boost", 
            () -> new HamonSpeedBoost(new HamonAction.Builder().manaCost(600)));
    
    public static final RegistryObject<HamonAction> HAMON_PLANT_INFUSION = ACTIONS.register("hamon_plant_infusion", 
            () -> new HamonOrganismInfusion(new HamonAction.Builder().manaCost(200).swingHand().needsBlockTarget()));
    
    public static final RegistryObject<HamonAction> HAMON_ORGANISM_INFUSION = ACTIONS.register("hamon_organism_infusion", 
            () -> new HamonOrganismInfusion(new HamonAction.Builder().manaCost(200).swingHand().needsBlockTarget().needsEntityTarget()));
    
    public static final RegistryObject<HamonAction> HAMON_HEALING = ACTIONS.register("hamon_healing", 
            () -> new HamonHealing(new HamonAction.Builder().manaCost(670).swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_WALL_CLIMBING = ACTIONS.register("hamon_wall_climbing", 
            () -> new HamonWallClimbing(new HamonAction.Builder().holdType(15)));
    
    public static final RegistryObject<HamonAction> HAMON_DETECTOR = ACTIONS.register("hamon_detector", 
            () -> new HamonDetector(new HamonAction.Builder().holdType(7.5F).heldSlowDownFactor(0.5F)));
    
    public static final RegistryObject<HamonAction> HAMON_LIFE_MAGNETISM = ACTIONS.register("hamon_life_magnetism", 
            () -> new HamonLifeMagnetism(new HamonAction.Builder().manaCost(200).shout(Technique.ZEPPELI, ModSounds.ZEPPELI_LIFE_MAGNETISM_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_PROJECTILE_SHIELD = ACTIONS.register("hamon_projectile_shield", 
            () -> new HamonProjectileShield(new HamonAction.Builder().holdType(30).heldSlowDownFactor(0.5F).shout(Technique.JOSEPH, ModSounds.JOSEPH_BARRIER)));
    
    public static final RegistryObject<HamonAction> HAMON_REPELLING_OVERDRIVE = ACTIONS.register("hamon_repelling_overdrive", 
            () -> new HamonRepellingOverdrive(new HamonAction.Builder().manaCost(1000)));

    public static final RegistryObject<HamonAction> JONATHAN_OVERDRIVE_BARRAGE = ACTIONS.register("jonathan_overdrive_barrage", 
            () -> new HamonOverdriveBarrage(new HamonAction.Builder().holdType(75).heldSlowDownFactor(0.5F).shout(ModSounds.JONATHAN_OVERDRIVE_BARRAGE)));

    public static final RegistryObject<HamonAction> JONATHAN_SCARLET_OVERDRIVE = ACTIONS.register("jonathan_scarlet_overdrive", 
            () -> new HamonScarletOverdrive(new HamonAction.Builder().manaCost(150)
                    .shout(ModSounds.JONATHAN_SCARLET_OVERDRIVE).swingHand().doNotCancelClick()));
    
    public static final RegistryObject<HamonAction> ZEPPELI_HAMON_CUTTER = ACTIONS.register("zeppeli_hamon_cutter", 
            () -> new HamonCutter(new HamonAction.Builder().manaCost(400).shout(ModSounds.ZEPPELI_HAMON_CUTTER)));
    
    public static final RegistryObject<HamonAction> ZEPPELI_TORNADO_OVERDRIVE = ACTIONS.register("zeppeli_tornado_overdrive", 
            () -> new HamonTornadoOverdrive(new HamonAction.Builder().shout(ModSounds.ZEPPELI_TORNADO_OVERDRIVE).holdType(75)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_LAUNCHER = ACTIONS.register("caesar_bubble_launcher", 
            () -> new HamonBubbleLauncher(new HamonAction.Builder().holdType(50).heldSlowDownFactor(0.3F).shout(ModSounds.CAESAR_BUBBLE_LAUNCHER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_BARRIER = ACTIONS.register("caesar_bubble_barrier", 
            () -> new HamonBubbleBarrier(new HamonAction.Builder().holdToFire(20, false, 50).heldSlowDownFactor(0.3F)
                    .shout(ModSounds.CAESAR_BUBBLE_BARRIER).shiftVariationOf(CAESAR_BUBBLE_LAUNCHER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_CUTTER = ACTIONS.register("caesar_bubble_cutter", 
            () -> new HamonBubbleCutter(new HamonAction.Builder().manaCost(500).swingHand().shout(ModSounds.CAESAR_BUBBLE_CUTTER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_CUTTER_GLIDING = ACTIONS.register("caesar_bubble_cutter_gliding", 
            () -> new HamonBubbleCutter(new HamonAction.Builder().manaCost(600).swingHand()
                    .shout(ModSounds.CAESAR_BUBBLE_CUTTER_GLIDING).shiftVariationOf(CAESAR_BUBBLE_CUTTER)));
    

    public static final RegistryObject<Action> VAMPIRISM_BLOOD_DRAIN = ACTIONS.register("vampirism_blood_drain", 
            () -> new VampirismBloodDrain(new Action.Builder().needsEntityTarget().maxRangeEntityTarget(2.0D).holdType(0).heldSlowDownFactor(0.5F)));
    
    public static final RegistryObject<Action> VAMPIRISM_FREEZE = ACTIONS.register("vampirism_freeze", 
            () -> new VampirismFreeze(new Action.Builder().needsEntityTarget().maxRangeEntityTarget(2.0D).holdType(0).heldSlowDownFactor(0.5F)));
    
    public static final RegistryObject<Action> VAMPIRISM_SPACE_RIPPER_STINGY_EYES = ACTIONS.register("vampirism_space_ripper_stingy_eyes", 
            () -> new VampirismSpaceRipperStingyEyes(new Action.Builder().ignoresPerformerStun().holdType(20, 40).heldSlowDownFactor(0.3F)));
    
    public static final RegistryObject<Action> VAMPIRISM_BLOOD_GIFT = ACTIONS.register("vampirism_blood_gift", 
            () -> new VampirismBloodGift(new Action.Builder().needsEntityTarget()
                    .maxRangeEntityTarget(1.0D).holdToFire(60, false, 10).heldSlowDownFactor(0.3F)));
    
    public static final RegistryObject<Action> VAMPIRISM_ZOMBIE_SUMMON = ACTIONS.register("vampirism_zombie_summon", 
            () -> new VampirismZombieSummon(new Action.Builder().manaCost(100)));
    
    public static final RegistryObject<Action> VAMPIRISM_DARK_AURA = ACTIONS.register("vampirism_dark_aura", 
            () -> new VampirismDarkAura(new Action.Builder().ignoresPerformerStun().manaCost(50).cooldown(100)));
    
    public static final RegistryObject<Action> VAMPIRISM_HAMON_SUICIDE = ACTIONS.register("vampirism_hamon_suicide", 
            () -> new VampirismHamonSuicide(new Action.Builder().ignoresPerformerStun().holdToFire(100, false, 0)));

    
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_PUNCH = ACTIONS.register("star_platinum_punch", 
            () -> new StandEntityMeleeAttack(new StandEntityAction.Builder().manaCost(50).cooldown(5)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_BARRAGE = ACTIONS.register("star_platinum_barrage", 
            () -> new StandEntityMeleeBarrage(new StandEntityMeleeBarrage.Builder().holdType(2, 100).cooldown(5).shiftVariationOf(STAR_PLATINUM_PUNCH)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_STAR_FINGER = ACTIONS.register("star_platinum_star_finger", 
            () -> new StandEntityRangedAttack(new StandEntityAction.Builder().manaCost(300)
                    .cooldown(20).ignoresPerformerStun().expRequirement(300).shout(ModSounds.JOTARO_STAR_FINGER)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_BLOCK = ACTIONS.register("star_platinum_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_ZOOM = ACTIONS.register("star_platinum_zoom", 
            () -> new StarPlatinumZoom(new StandEntityAction.Builder().holdType(0)));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_TIME_STOP = ACTIONS.register("star_platinum_time_stop", 
            () -> new TimeStop(new StandEntityAction.Builder().manaCost(800).holdToFire(30, false, 0)
                    .expRequirement(950).ignoresPerformerStun().shout(ModSounds.JOTARO_STAR_PLATINUM_THE_WORLD))
            .voiceLineWithStandSummoned(ModSounds.JOTARO_THE_WORLD).timeStopSound(ModSounds.STAR_PLATINUM_TIME_STOP)
            .timeResumeVoiceLine(ModSounds.JOTARO_TIME_RESUMES).timeResumeSound(ModSounds.STAR_PLATINUM_TIME_RESUME));
    
    public static final RegistryObject<StandEntityAction> STAR_PLATINUM_TIME_STOP_BLINK = ACTIONS.register("star_platinum_ts_blink", 
            () -> new TimeStopInstant(new StandEntityAction.Builder().manaCost(800)
                    .expRequirement(950).ignoresPerformerStun().shiftVariationOf(STAR_PLATINUM_TIME_STOP)));
    

    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_STRING_ATTACK = ACTIONS.register("hierophant_green_attack", 
            () -> new HierophantGreenStringAttack(new StandEntityAction.Builder().manaCost(60).cooldown(10)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_STRING_BIND = ACTIONS.register("hierophant_green_attack_binding", 
            () -> new HierophantGreenStringAttack(new StandEntityAction.Builder().manaCost(75).cooldown(25).expRequirement(200)
                    .shiftVariationOf(HIEROPHANT_GREEN_STRING_ATTACK)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_EMERALD_SPLASH = ACTIONS.register("hierophant_green_emerald_splash", 
            () -> new StandEntityRangedAttack(new StandEntityAction.Builder().manaCost(55).cooldown(30).expRequirement(50)
                    .shout(ModSounds.KAKYOIN_EMERALD_SPLASH)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_EMERALD_SPLASH_CONCENTRATED = ACTIONS.register("hierophant_green_es_concentrated", 
            () -> new StandEntityRangedAttack(new StandEntityAction.Builder().manaCost(35).cooldown(5)
                    .expRequirement(400).shout(ModSounds.KAKYOIN_EMERALD_SPLASH).shiftVariationOf(HIEROPHANT_GREEN_EMERALD_SPLASH)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_BLOCK = ACTIONS.register("hierophant_green_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_GRAPPLE = ACTIONS.register("hierophant_green_grapple", 
            () -> new HierophantGreenGrapple(new StandEntityAction.Builder().expRequirement(100).holdType(0.5F)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_GRAPPLE_ENTITY = ACTIONS.register("hierophant_green_grapple_entity", 
            () -> new HierophantGreenGrapple(new StandEntityAction.Builder().expRequirement(100).holdType(0.5F).shiftVariationOf(HIEROPHANT_GREEN_GRAPPLE)));
    
    public static final RegistryObject<StandEntityAction> HIEROPHANT_GREEN_BARRIER = ACTIONS.register("hierophant_green_barrier", 
            () -> new HierophantGreenBarrier(new StandEntityAction.Builder().needsBlockTarget().expRequirement(700)));
    

    public static final RegistryObject<StandEntityAction> THE_WORLD_PUNCH = ACTIONS.register("the_world_punch", 
            () -> new StandEntityMeleeAttack(new StandEntityAction.Builder().manaCost(45).cooldown(5)));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_BARRAGE = ACTIONS.register("the_world_barrage", 
            () -> new StandEntityMeleeBarrage(new StandEntityMeleeBarrage.Builder().holdType(2.2F, 100).shiftVariationOf(THE_WORLD_PUNCH)));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_BLOCK = ACTIONS.register("the_world_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_TIME_STOP = ACTIONS.register("the_world_time_stop", 
            () -> new TimeStop(new StandEntityAction.Builder().manaCost(800).expRequirement(500).holdToFire(30, false, 0)
                    .ignoresPerformerStun().doNotAutoSummonStand().shout(ModSounds.DIO_THE_WORLD))
            .voiceLineWithStandSummoned(ModSounds.DIO_TIME_STOP).timeStopSound(ModSounds.THE_WORLD_TIME_STOP)
            .timeResumeVoiceLine(ModSounds.DIO_TIME_RESUMES).timeResumeSound(ModSounds.THE_WORLD_TIME_RESUME));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_TIME_STOP_BLINK = ACTIONS.register("the_world_ts_blink", 
            () -> new TimeStopInstant(new StandEntityAction.Builder().manaCost(800).expRequirement(500)
                    .ignoresPerformerStun().shiftVariationOf(THE_WORLD_TIME_STOP)));
    
    public static final RegistryObject<StandEntityAction> THE_WORLD_ROAD_ROLLER = ACTIONS.register("the_world_road_roller", 
            () -> new TheWorldRoadRoller(new StandEntityAction.Builder().cooldown(12000).expRequirement(1000)
                    .doNotAutoSummonStand().shout(ModSounds.DIO_ROAD_ROLLER)));

    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_ATTACK = ACTIONS.register("silver_chariot_attack", 
            () -> new StandEntityMeleeAttack(new StandEntityAction.Builder().manaCost(60).cooldown(5)));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_BARRAGE = ACTIONS.register("silver_chariot_barrage", 
            () -> new SilverChariotMeleeBarrage(new StandEntityMeleeBarrage.Builder().holdType(1.5F, 80)
                    .cooldown(6).shout(ModSounds.POLNAREFF_HORA_HORA_HORA).shiftVariationOf(SILVER_CHARIOT_ATTACK)));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_RAPIER_LAUNCH = ACTIONS.register("silver_chariot_rapier_launch", 
            () -> new SilverChariotRapierLaunch(new StandEntityAction.Builder().manaCost(0).cooldown(400).expRequirement(200)));
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_BLOCK = ACTIONS.register("silver_chariot_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> SILVER_CHARIOT_TAKE_OFF_ARMOR = ACTIONS.register("silver_chariot_take_off_armor", 
            () -> new SilverChariotTakeOffArmor(new StandEntityAction.Builder().expRequirement(600)));
    

    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_PUNCH = ACTIONS.register("magicians_red_punch", 
            () -> new StandEntityMeleeAttack(new StandEntityAction.Builder().manaCost(70).cooldown(5)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_FLAME_BURST = ACTIONS.register("magicians_red_flame_burst", 
            () -> new StandEntityHeldRangedAttack(new StandEntityHeldRangedAttack.Builder().holdType(2).expRequirement(50).shiftVariationOf(MAGICIANS_RED_PUNCH)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_FIREBALL = ACTIONS.register("magicians_red_fireball", 
            () -> new MagiciansRedFireball(new StandEntityAction.Builder().manaCost(50).expRequirement(150)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_CROSSFIRE_HURRICANE = ACTIONS.register("magicians_red_crossfire_hurricane", 
            () -> new MagiciansRedCrossfireHurricane(new StandEntityAction.Builder().manaCost(400)
                    .expRequirement(700).holdToFire(40, false, 0).shout(ModSounds.AVDOL_CROSSFIRE_HURRICANE)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_CROSSFIRE_HURRICANE_SPECIAL = ACTIONS.register("magicians_red_ch_special", 
            () -> new MagiciansRedCrossfireHurricane(new StandEntityAction.Builder().manaCost(450).expRequirement(1000)
                    .holdToFire(60, false, 0).shout(ModSounds.AVDOL_CROSSFIRE_HURRICANE_SPECIAL).shiftVariationOf(MAGICIANS_RED_CROSSFIRE_HURRICANE)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_BLOCK = ACTIONS.register("magicians_red_block", 
            () -> new StandEntityBlock());
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_RED_BIND = ACTIONS.register("magicians_red_red_bind", 
            () -> new MagiciansRedRedBind(new StandEntityAction.Builder().expRequirement(450).holdType(1)
                    .heldSlowDownFactor(0.3F).shout(ModSounds.AVDOL_RED_BIND)));
    
    public static final RegistryObject<StandEntityAction> MAGICIANS_RED_DETECTOR = ACTIONS.register("magicians_red_detector", 
            () -> new MagiciansRedDetector(new StandEntityAction.Builder().manaCost(0).expRequirement(500)));
    
    

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void afterActionsInit(RegistryEvent.Register<Action> event) {
        Action.initShiftVariations();
        addTimeStopAbilities(event);
    }
    
    public static void addTimeStopAbilities(RegistryEvent.Register<Action> event) {
        TimeHandler.addAllowMovingInStoppedTime(THE_WORLD_TIME_STOP.get());
        TimeHandler.addAllowMovingInStoppedTime(STAR_PLATINUM_TIME_STOP.get());
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void initTimeManipulatingActions(RegistryEvent.Register<Action> event) {
        TimeHandler.initTimeManipulatingActions();
    }
    
    
    
    public static class Registry {
        private static Supplier<IForgeRegistry<Action>> REGISTRY_SUPPLIER = null;
        
        public static void initRegistry() {
            if (REGISTRY_SUPPLIER == null) {
                REGISTRY_SUPPLIER = ModActions.ACTIONS.makeRegistry("action", () -> new RegistryBuilder<>());
            }
        }
        
        public static IForgeRegistry<Action> getRegistry() {
            return REGISTRY_SUPPLIER.get();
        }
    }

}
