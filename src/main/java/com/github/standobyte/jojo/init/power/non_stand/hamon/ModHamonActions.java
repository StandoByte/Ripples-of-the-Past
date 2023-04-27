package com.github.standobyte.jojo.init.power.non_stand.hamon;

import static com.github.standobyte.jojo.init.power.ModCommonRegistries.ACTIONS;
import static com.github.standobyte.jojo.init.power.ModCommonRegistries.NON_STAND_POWERS;

import com.github.standobyte.jojo.action.non_stand.HamonAction;
import com.github.standobyte.jojo.action.non_stand.HamonBubbleBarrier;
import com.github.standobyte.jojo.action.non_stand.HamonBubbleCutter;
import com.github.standobyte.jojo.action.non_stand.HamonBubbleLauncher;
import com.github.standobyte.jojo.action.non_stand.HamonCutter;
import com.github.standobyte.jojo.action.non_stand.HamonDetector;
import com.github.standobyte.jojo.action.non_stand.HamonHealing;
import com.github.standobyte.jojo.action.non_stand.HamonLifeMagnetism;
import com.github.standobyte.jojo.action.non_stand.HamonOrganismInfusion;
import com.github.standobyte.jojo.action.non_stand.HamonOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonOverdriveBarrage;
import com.github.standobyte.jojo.action.non_stand.HamonPlantInfusion;
import com.github.standobyte.jojo.action.non_stand.HamonProjectileShield;
import com.github.standobyte.jojo.action.non_stand.HamonRepellingOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonScarletOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonSendoOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonSpeedBoost;
import com.github.standobyte.jojo.action.non_stand.HamonSunlightYellowOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonTornadoOverdrive;
import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing;
import com.github.standobyte.jojo.action.non_stand.HamonZoomPunch;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.Technique;

import net.minecraftforge.fml.RegistryObject;

public class ModHamonActions {
    
    public static void loadRegistryObjects() {}

    public static final RegistryObject<HamonAction> HAMON_OVERDRIVE = ACTIONS.registerEntry("hamon_overdrive", 
            () -> new HamonOverdrive(new HamonAction.Builder().energyCost(750F)));
     
    public static final RegistryObject<HamonAction> HAMON_SUNLIGHT_YELLOW_OVERDRIVE = ACTIONS.registerEntry("hamon_sunlight_yellow_overdrive", 
            () -> new HamonSunlightYellowOverdrive(new HamonAction.Builder().energyCost(1500F)
                    .shout(Technique.JONATHAN, ModSounds.JONATHAN_SUNLIGHT_YELLOW_OVERDRIVE)
                    .shout(Technique.ZEPPELI, ModSounds.ZEPPELI_SUNLIGHT_YELLOW_OVERDRIVE)
                    .shout(Technique.JOSEPH, ModSounds.JOSEPH_SUNLIGHT_YELLOW_OVERDRIVE)
                    .shout(Technique.CAESAR, ModSounds.CAESAR_SUNLIGHT_YELLOW_OVERDRIVE)
                    .shiftVariationOf(HAMON_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_SENDO_OVERDRIVE = ACTIONS.registerEntry("hamon_sendo_overdrive", 
            () -> new HamonSendoOverdrive(new HamonAction.Builder().energyCost(1000F)
                    .emptyMainHand().swingHand().shout(Technique.JONATHAN, ModSounds.JONATHAN_SENDO_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_ZOOM_PUNCH = ACTIONS.registerEntry("hamon_zoom_punch", 
            () -> new HamonZoomPunch(new HamonAction.Builder().energyCost(800F).cooldown(14, 0)
                    .shout(Technique.JONATHAN, ModSounds.JONATHAN_ZOOM_PUNCH)
                    .shout(Technique.ZEPPELI, ModSounds.ZEPPELI_ZOOM_PUNCH)
                    .shout(Technique.JOSEPH, ModSounds.JOSEPH_ZOOM_PUNCH)));

    public static final RegistryObject<HamonAction> HAMON_SPEED_BOOST = ACTIONS.registerEntry("hamon_speed_boost", 
            () -> new HamonSpeedBoost(new HamonAction.Builder().energyCost(600F)));
    
    public static final RegistryObject<HamonAction> HAMON_PLANT_INFUSION = ACTIONS.registerEntry("hamon_plant_infusion", 
            () -> new HamonPlantInfusion(new HamonAction.Builder().energyCost(200F)
                    .emptyMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_ORGANISM_INFUSION = ACTIONS.registerEntry("hamon_organism_infusion", 
            () -> new HamonOrganismInfusion(new HamonAction.Builder().energyCost(200F)
                    .emptyMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_HEALING = ACTIONS.registerEntry("hamon_healing", 
            () -> new HamonHealing(new HamonAction.Builder().energyCost(670F)
                    .emptyMainHand().swingHand()));
    
    public static final RegistryObject<HamonAction> HAMON_WALL_CLIMBING = ACTIONS.registerEntry("hamon_wall_climbing", 
            () -> new HamonWallClimbing(new HamonAction.Builder().holdEnergyCost(15F)));
    
    public static final RegistryObject<HamonAction> HAMON_DETECTOR = ACTIONS.registerEntry("hamon_detector", 
            () -> new HamonDetector(new HamonAction.Builder().holdEnergyCost(7.5F).heldWalkSpeed(0.5F)));
    
    public static final RegistryObject<HamonAction> HAMON_LIFE_MAGNETISM = ACTIONS.registerEntry("hamon_life_magnetism", 
            () -> new HamonLifeMagnetism(new HamonAction.Builder().energyCost(200F).shout(Technique.ZEPPELI, ModSounds.ZEPPELI_LIFE_MAGNETISM_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> HAMON_PROJECTILE_SHIELD = ACTIONS.registerEntry("hamon_projectile_shield", 
            () -> new HamonProjectileShield(new HamonAction.Builder().holdEnergyCost(30F).heldWalkSpeed(0.5F).shout(Technique.JOSEPH, ModSounds.JOSEPH_BARRIER)));
    
    public static final RegistryObject<HamonAction> HAMON_REPELLING_OVERDRIVE = ACTIONS.registerEntry("hamon_repelling_overdrive", 
            () -> new HamonRepellingOverdrive(new HamonAction.Builder().energyCost(1000F)));
    
    

    public static final RegistryObject<HamonPowerType> HAMON = NON_STAND_POWERS.registerEntry("hamon", 
            () -> new HamonPowerType(
                    0xFFFF00, 
                    new HamonAction[] {
                            HAMON_SENDO_OVERDRIVE.get(), 
                            HAMON_PLANT_INFUSION.get(), 
                            HAMON_ZOOM_PUNCH.get()}, 
                    new HamonAction[] {
                            HAMON_HEALING.get(), 
                            HAMON_SPEED_BOOST.get(), 
                            HAMON_WALL_CLIMBING.get(), 
                            HAMON_DETECTOR.get(), 
                            HAMON_LIFE_MAGNETISM.get(), 
                            HAMON_PROJECTILE_SHIELD.get(), 
                            HAMON_REPELLING_OVERDRIVE.get()}
                    ));
    
    

    public static final RegistryObject<HamonAction> JONATHAN_OVERDRIVE_BARRAGE = ACTIONS.registerEntry("jonathan_overdrive_barrage", 
            () -> new HamonOverdriveBarrage(new HamonAction.Builder().holdEnergyCost(70F).heldWalkSpeed(0.5F).shout(ModSounds.JONATHAN_OVERDRIVE_BARRAGE)));

    public static final RegistryObject<HamonAction> JONATHAN_SCARLET_OVERDRIVE = ACTIONS.registerEntry("jonathan_scarlet_overdrive", 
            () -> new HamonScarletOverdrive(new HamonAction.Builder().energyCost(150F)
                    .emptyMainHand().swingHand().shout(ModSounds.JONATHAN_SCARLET_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> ZEPPELI_HAMON_CUTTER = ACTIONS.registerEntry("zeppeli_hamon_cutter", 
            () -> new HamonCutter(new HamonAction.Builder().energyCost(400F).shout(ModSounds.ZEPPELI_HAMON_CUTTER)));
    
    public static final RegistryObject<HamonAction> ZEPPELI_TORNADO_OVERDRIVE = ACTIONS.registerEntry("zeppeli_tornado_overdrive", 
            () -> new HamonTornadoOverdrive(new HamonAction.Builder().holdEnergyCost(75F).shout(ModSounds.ZEPPELI_TORNADO_OVERDRIVE)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_LAUNCHER = ACTIONS.registerEntry("caesar_bubble_launcher", 
            () -> new HamonBubbleLauncher(new HamonAction.Builder().holdEnergyCost(50F).heldWalkSpeed(0.3F).shout(ModSounds.CAESAR_BUBBLE_LAUNCHER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_BARRIER = ACTIONS.registerEntry("caesar_bubble_barrier", 
            () -> new HamonBubbleBarrier(new HamonAction.Builder().holdToFire(20, false).holdEnergyCost(50F).heldWalkSpeed(0.3F)
                    .shout(ModSounds.CAESAR_BUBBLE_BARRIER).shiftVariationOf(CAESAR_BUBBLE_LAUNCHER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_CUTTER = ACTIONS.registerEntry("caesar_bubble_cutter", 
            () -> new HamonBubbleCutter(new HamonAction.Builder().energyCost(500F).swingHand().shout(ModSounds.CAESAR_BUBBLE_CUTTER)));
    
    public static final RegistryObject<HamonAction> CAESAR_BUBBLE_CUTTER_GLIDING = ACTIONS.registerEntry("caesar_bubble_cutter_gliding", 
            () -> new HamonBubbleCutter(new HamonAction.Builder().energyCost(600F).cooldown(40).swingHand()
                    .shout(ModSounds.CAESAR_BUBBLE_CUTTER_GLIDING).shiftVariationOf(CAESAR_BUBBLE_CUTTER)));
    
}
