package com.github.standobyte.jojo.init.power.non_stand.hamon;

import static com.github.standobyte.jojo.init.power.ModCommonRegisters.ACTIONS;
import static com.github.standobyte.jojo.init.power.ModCommonRegisters.NON_STAND_POWERS;

import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.action.non_stand.HamonBreath;
import com.github.standobyte.jojo.action.non_stand.HamonBubbleBarrier;
import com.github.standobyte.jojo.action.non_stand.HamonBubbleCutter;
import com.github.standobyte.jojo.action.non_stand.HamonBubbleLauncher;
import com.github.standobyte.jojo.action.non_stand.HamonCutter;
import com.github.standobyte.jojo.action.non_stand.HamonDetector;
import com.github.standobyte.jojo.action.non_stand.HamonHealing;
import com.github.standobyte.jojo.action.non_stand.HamonHypnosis;
import com.github.standobyte.jojo.action.non_stand.HamonLifeMagnetism;
import com.github.standobyte.jojo.action.non_stand.HamonMetalSilverOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonMetalSilverOverdriveWeapon;
import com.github.standobyte.jojo.action.non_stand.HamonOrganismInfusion;
import com.github.standobyte.jojo.action.non_stand.HamonOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonOverdriveBarrage;
import com.github.standobyte.jojo.action.non_stand.HamonPlantInfusion;
import com.github.standobyte.jojo.action.non_stand.HamonProjectileShield;
import com.github.standobyte.jojo.action.non_stand.HamonProtection;
import com.github.standobyte.jojo.action.non_stand.HamonRebuffOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonScarletOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonSendoOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonSendoWaveKick;
import com.github.standobyte.jojo.action.non_stand.HamonShock;
import com.github.standobyte.jojo.action.non_stand.HamonSpeedBoost;
import com.github.standobyte.jojo.action.non_stand.HamonSunlightYellowOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonSunlightYellowOverdriveBarrage;
import com.github.standobyte.jojo.action.non_stand.HamonTornadoOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonTurquoiseBlueOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;
import com.github.standobyte.jojo.action.non_stand.HamonZoomPunch;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;

import net.minecraftforge.fml.RegistryObject;

public class ModHamonActions {
    
    public static void loadRegistryObjects() {}

    public static final RegistryObject<HamonAction> HAMON_OVERDRIVE = ACTIONS.register("hamon_overdrive", 
            () -> new HamonOverdrive(new HamonAction.Builder().energyCost(600F)
                    .needsFreeMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_BEAT = ACTIONS.register("hamon_overdrive_beat", 
            () -> new HamonOverdrive(new HamonAction.Builder().energyCost(1500F)
                    .needsFreeMainHand().swingHand().shiftVariationOf(HAMON_OVERDRIVE))
            .setIsStrongVersion());
   
   public static final RegistryObject<HamonAction> HAMON_SENDO_OVERDRIVE = ACTIONS.register("hamon_sendo_overdrive", 
           () -> new HamonSendoOverdrive(new HamonAction.Builder().energyCost(900F)
                   .needsFreeMainHand().swingHand()
                   .shout(ModHamonSkills.CHARACTER_JONATHAN, ModSounds.JONATHAN_SENDO_OVERDRIVE)));

    public static final RegistryObject<HamonAction> HAMON_TURQUOISE_BLUE_OVERDRIVE = ACTIONS.register("hamon_turquoise_blue_overdrive", 
            () -> new HamonTurquoiseBlueOverdrive(new HamonAction.Builder().energyCost(250F).cooldown(30)
                    .needsFreeMainHand().swingHand()));
    
   public static final RegistryObject<HamonAction> HAMON_SUNLIGHT_YELLOW_OVERDRIVE = ACTIONS.register("hamon_sunlight_yellow_overdrive", 
           () -> new HamonSunlightYellowOverdrive(new HamonAction.Builder().holdToFire(80, true).holdType(160).swingHand()
                   .shout(ModHamonSkills.CHARACTER_JONATHAN, ModSounds.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE)
                   .shout(ModHamonSkills.CHARACTER_ZEPPELI, ModSounds.ZEPPELI_SUNLIGHT_YELLOW_OVERDRIVE)
                   .shout(ModHamonSkills.CHARACTER_JOSEPH, ModSounds.JOSEPH_SUNLIGHT_YELLOW_OVERDRIVE)
                   .shout(ModHamonSkills.CHARACTER_CAESAR, ModSounds.CAESAR_SUNLIGHT_YELLOW_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_ZOOM_PUNCH = ACTIONS.register("hamon_zoom_punch", 
            () -> new HamonZoomPunch(new HamonZoomPunch.Builder().energyCost(450F).hitCost(150F).cooldown(14, 0)
                    .shout(ModHamonSkills.CHARACTER_JONATHAN, ModSounds.JONATHAN_ZOOM_PUNCH)
                    .shout(ModHamonSkills.CHARACTER_ZEPPELI, ModSounds.ZEPPELI_ZOOM_PUNCH)
                    .shout(ModHamonSkills.CHARACTER_JOSEPH, ModSounds.JOSEPH_ZOOM_PUNCH)));

    public static final RegistryObject<HamonAction> HAMON_SPEED_BOOST = ACTIONS.register("hamon_speed_boost", 
            () -> new HamonSpeedBoost(new HamonAction.Builder().energyCost(600F)));
    
    public static final RegistryObject<HamonAction> HAMON_PLANT_INFUSION = ACTIONS.register("hamon_plant_infusion", 
            () -> new HamonPlantInfusion(new HamonAction.Builder().energyCost(200F)
                    .needsFreeMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_ORGANISM_INFUSION = ACTIONS.register("hamon_organism_infusion", 
            () -> new HamonOrganismInfusion(new HamonAction.Builder().energyCost(1000F)
                    .needsFreeMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_BREATH = ACTIONS.register("hamon_breath", 
            () -> new HamonBreath(new HamonAction.Builder().holdType().heldWalkSpeed(0.0F)
                    .shout(ModSounds.BREATH_DEFAULT)
                    .shout(ModHamonSkills.CHARACTER_JONATHAN, ModSounds.BREATH_JONATHAN)
                    .shout(ModHamonSkills.CHARACTER_ZEPPELI, ModSounds.BREATH_ZEPPELI)
                    .shout(ModHamonSkills.CHARACTER_JOSEPH, ModSounds.BREATH_JOSEPH)
                    .shout(ModHamonSkills.CHARACTER_CAESAR, ModSounds.BREATH_CAESAR)
                    .shout(ModHamonSkills.CHARACTER_LISA_LISA, ModSounds.BREATH_LISA_LISA)));
    
    public static final RegistryObject<HamonAction> HAMON_HEALING = ACTIONS.register("hamon_healing", 
            () -> new HamonHealing(new HamonAction.Builder().energyCost(750F)
                    .needsFreeMainHand().swingHand()));
    
    public static final RegistryObject<HamonWallClimbing2> HAMON_WALL_CLIMBING = ACTIONS.register("hamon_wall_climbing", 
            () -> new HamonWallClimbing2(new HamonAction.Builder().holdEnergyCost(10F)));
    
    public static final RegistryObject<HamonAction> HAMON_DETECTOR = ACTIONS.register("hamon_detector", 
            () -> new HamonDetector(new HamonAction.Builder().holdEnergyCost(5F).heldWalkSpeed(0.5F)));
    
    public static final RegistryObject<HamonAction> HAMON_LIFE_MAGNETISM = ACTIONS.register("hamon_life_magnetism", 
            () -> new HamonLifeMagnetism(new HamonAction.Builder().energyCost(LeavesGliderEntity.MAX_ENERGY)
                    .shout(ModHamonSkills.CHARACTER_ZEPPELI, ModSounds.ZEPPELI_LIFE_MAGNETISM_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_PROJECTILE_SHIELD = ACTIONS.register("hamon_projectile_shield", 
            () -> new HamonProjectileShield(new HamonAction.Builder().holdEnergyCost(15F).heldWalkSpeed(0.3F)
                    .shout(ModHamonSkills.CHARACTER_JOSEPH, ModSounds.JOSEPH_BARRIER)));
    
    public static final RegistryObject<HamonProtection> HAMON_PROTECTION = ACTIONS.register("hamon_protection", 
            () -> new HamonProtection(new HamonAction.Builder()));
    
    public static final RegistryObject<HamonAction> HAMON_HYPNOSIS = ACTIONS.register("hamon_hypnosis", 
            () -> new HamonHypnosis(new HamonAction.Builder().holdToFire(60, false).holdEnergyCost(15)));
    
    public static final RegistryObject<HamonAction> HAMON_SHOCK = ACTIONS.register("hamon_shock", 
            () -> new HamonShock(new HamonAction.Builder().holdToFire(15, false).holdEnergyCost(180)));
    
    
    
    public static final RegistryObject<HamonPowerType> HAMON = NON_STAND_POWERS.register("hamon", 
            () -> new HamonPowerType(
                    new HamonAction[] {
                            HAMON_OVERDRIVE.get(), 
                            HAMON_SENDO_OVERDRIVE.get(), 
                            HAMON_SUNLIGHT_YELLOW_OVERDRIVE.get(), 
                            HAMON_PLANT_INFUSION.get(), 
                            HAMON_ZOOM_PUNCH.get(), 
                            HAMON_TURQUOISE_BLUE_OVERDRIVE.get()
                            }, 
                    new HamonAction[] {
                            HAMON_BREATH.get(), 
                            HAMON_HEALING.get(), 
                            HAMON_SPEED_BOOST.get(), 
                            HAMON_WALL_CLIMBING.get(), 
                            HAMON_LIFE_MAGNETISM.get(), 
                            HAMON_PROJECTILE_SHIELD.get(), 
                            HAMON_PROTECTION.get(), 
                            HAMON_DETECTOR.get(), 
                            HAMON_HYPNOSIS.get(), 
                            HAMON_SHOCK.get()
                            },
                    HAMON_BREATH.get()
                    ));
    
    
    
    public static final RegistryObject<HamonAction> JONATHAN_SCARLET_OVERDRIVE = ACTIONS.register("jonathan_scarlet_overdrive", 
            () -> new HamonScarletOverdrive(new HamonAction.Builder().needsFreeMainHand()
                    .holdToFire(40, true).holdType(100).swingHand().shout(ModSounds.JONATHAN_SCARLET_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> JONATHAN_METAL_SILVER_OVERDRIVE = ACTIONS.register("jonathan_metal_silver_overdrive", 
            () -> new HamonMetalSilverOverdrive(new HamonAction.Builder().energyCost(1000).swingHand().needsFreeMainHand()
                    .addShiftVariation(HAMON_BEAT)));
    
    public static final RegistryObject<HamonAction> JONATHAN_METAL_SILVER_OVERDRIVE_WEAPON = ACTIONS.register("jonathan_metal_silver_overdrive_weapon", 
            () -> new HamonMetalSilverOverdriveWeapon(new HamonAction.Builder().energyCost(750).swingHand()));
    
    public static final RegistryObject<HamonAction> JONATHAN_OVERDRIVE_BARRAGE = ACTIONS.register("jonathan_overdrive_barrage", 
            () -> new HamonOverdriveBarrage(new HamonAction.Builder().holdEnergyCost(70F).heldWalkSpeed(0.5F).shout(ModSounds.JONATHAN_OVERDRIVE_BARRAGE)));
    
    public static final RegistryObject<HamonSunlightYellowOverdriveBarrage> JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE_BARRAGE = ACTIONS.register("jonathan_syo_barrage", 
            () -> new HamonSunlightYellowOverdriveBarrage(new HamonAction.Builder().holdToFire(60, false).heldWalkSpeed(0)
                    .shout(ModSounds.JONATHAN_SYO_BARRAGE_START).shiftVariationOf(JONATHAN_OVERDRIVE_BARRAGE)));
    
    public static final RegistryObject<HamonAction> ZEPPELI_HAMON_CUTTER = ACTIONS.register("zeppeli_hamon_cutter", 
            () -> new HamonCutter(new HamonAction.Builder().energyCost(400F).swingHand().shout(ModSounds.ZEPPELI_HAMON_CUTTER)));
    
    public static final RegistryObject<HamonSendoWaveKick> ZEPPELI_SENDO_WAVE_KICK = ACTIONS.register("zeppeli_sendo_wave_kick", 
            () -> new HamonSendoWaveKick(new HamonAction.Builder().energyCost(1000F).shout(ModSounds.ZEPPELI_SENDO_WAVE_KICK)));
    
    public static final RegistryObject<HamonAction> ZEPPELI_TORNADO_OVERDRIVE = ACTIONS.register("zeppeli_tornado_overdrive", 
            () -> new HamonTornadoOverdrive(new HamonAction.Builder().holdEnergyCost(75F).shout(ModSounds.ZEPPELI_TORNADO_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> JOSEPH_REBUFF_OVERDRIVE = ACTIONS.register("joseph_rebuff_overdrive", 
            () -> new HamonRebuffOverdrive(new HamonAction.Builder().cooldown(80).shout(ModSounds.JOSEPH_GIGGLE)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_LAUNCHER = ACTIONS.register("caesar_bubble_launcher", 
            () -> new HamonBubbleLauncher(new HamonAction.Builder().holdEnergyCost(50F).heldWalkSpeed(0.3F).shout(ModSounds.CAESAR_BUBBLE_LAUNCHER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_BARRIER = ACTIONS.register("caesar_bubble_barrier", 
            () -> new HamonBubbleBarrier(new HamonAction.Builder().holdToFire(20, false).holdEnergyCost(50F).heldWalkSpeed(0.3F).swingHand()
                    .shout(ModSounds.CAESAR_BUBBLE_BARRIER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_CUTTER = ACTIONS.register("caesar_bubble_cutter", 
            () -> new HamonBubbleCutter(new HamonAction.Builder().energyCost(500F).cooldown(20).swingHand().shout(ModSounds.CAESAR_BUBBLE_CUTTER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_CUTTER_GLIDING = ACTIONS.register("caesar_bubble_cutter_gliding", 
            () -> new HamonBubbleCutter(new HamonAction.Builder().energyCost(600F).cooldown(20).swingHand()
                    .shout(ModSounds.CAESAR_BUBBLE_CUTTER_GLIDING).shiftVariationOf(CAESAR_BUBBLE_CUTTER)));
    
}
