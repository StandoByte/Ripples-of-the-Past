package com.github.standobyte.jojo.init.power.non_stand.pillarman;

import static com.github.standobyte.jojo.init.power.ModCommonRegisters.ACTIONS;
import static com.github.standobyte.jojo.init.power.ModCommonRegisters.NON_STAND_POWERS;

import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.action.non_stand.PillarmanAbsorption;
import com.github.standobyte.jojo.action.non_stand.PillarmanAction;
import com.github.standobyte.jojo.action.non_stand.PillarmanAtmosphericRift;
import com.github.standobyte.jojo.action.non_stand.PillarmanBladeBarrage;
import com.github.standobyte.jojo.action.non_stand.PillarmanBladeDashAttack;
import com.github.standobyte.jojo.action.non_stand.PillarmanDivineSandstorm;
import com.github.standobyte.jojo.action.non_stand.PillarmanEnhancedSenses;
import com.github.standobyte.jojo.action.non_stand.PillarmanErraticBlazeKing;
import com.github.standobyte.jojo.action.non_stand.PillarmanEvasion;
import com.github.standobyte.jojo.action.non_stand.PillarmanGiantCarthwheelPrison;
import com.github.standobyte.jojo.action.non_stand.PillarmanHeavyPunch;
import com.github.standobyte.jojo.action.non_stand.PillarmanHideInEntity;
import com.github.standobyte.jojo.action.non_stand.PillarmanHornAttack;
import com.github.standobyte.jojo.action.non_stand.PillarmanLightFlash;
import com.github.standobyte.jojo.action.non_stand.PillarmanRegeneration;
import com.github.standobyte.jojo.action.non_stand.PillarmanRibsBlades;
import com.github.standobyte.jojo.action.non_stand.PillarmanSelfDetonation;
import com.github.standobyte.jojo.action.non_stand.PillarmanSmallSandstorm;
import com.github.standobyte.jojo.action.non_stand.PillarmanStoneForm;
import com.github.standobyte.jojo.action.non_stand.PillarmanUnnaturalAgility;
import com.github.standobyte.jojo.action.non_stand.PillarmanWindCloak;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanPowerType;

import net.minecraftforge.fml.RegistryObject;

public class ModPillarmanActions {

    public static void loadRegistryObjects() {}

    public static final RegistryObject<PillarmanAction> PILLARMAN_HEAVY_PUNCH = ACTIONS.register("pillarman_heavy_punch", 
            () -> new PillarmanHeavyPunch(new NonStandAction.Builder().needsFreeMainHand().energyCost(10F)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_ABSORPTION = ACTIONS.register("pillarman_absorption", 
            () -> new PillarmanAbsorption(new NonStandAction.Builder().needsFreeMainHand().ignoresPerformerStun()));

    public static final RegistryObject<PillarmanAction> PILLARMAN_HORN_ATTACK = ACTIONS.register("pillarman_horn_attack", 
            () -> new PillarmanHornAttack(new NonStandAction.Builder().energyCost(15F).cooldown(60).ignoresPerformerStun()));

    public static final RegistryObject<PillarmanAction> PILLARMAN_RIBS_BLADES = ACTIONS.register("pillarman_ribs_blades", 
            () -> new PillarmanRibsBlades(new NonStandAction.Builder().energyCost(60F).cooldown(80, 0)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_HIDE_IN_ENTITY = ACTIONS.register("pillarman_hide_in_entity", 
        () -> new PillarmanHideInEntity(new NonStandAction.Builder().holdToFire(20, false).heldWalkSpeed(0.5F)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_STONE_FORM = ACTIONS.register("pillarman_stone_form", 
            () -> new PillarmanStoneForm(new NonStandAction.Builder().holdToFire(40, false).heldWalkSpeed(0.5F).ignoresPerformerStun()));

    public static final RegistryObject<PillarmanAction> PILLARMAN_REGENERATION = ACTIONS.register("pillarman_regeneration", 
            () -> new PillarmanRegeneration(new NonStandAction.Builder().energyCost(40F).cooldown(20).ignoresPerformerStun()));

    public static final RegistryObject<PillarmanAction> PILLARMAN_ENHANCED_SENSES = ACTIONS.register("pillarman_enhanced_senses", 
            () -> new PillarmanEnhancedSenses(new NonStandAction.Builder().holdEnergyCost(0.05F).heldWalkSpeed(0.5F).ignoresPerformerStun()));

    public static final RegistryObject<PillarmanAction> PILLARMAN_EVASION = ACTIONS.register("pillarman_evasion", 
            () -> new PillarmanEvasion(new NonStandAction.Builder().holdType(50).holdEnergyCost(1.5F).heldWalkSpeed(5.0F).cooldown(50)));
    
    public static final RegistryObject<PillarmanAction> PILLARMAN_UNNATURAL_AGILITY = ACTIONS.register("pillarman_unnatural_agility", 
            () -> new PillarmanUnnaturalAgility(new NonStandAction.Builder().holdEnergyCost(1F).heldWalkSpeed(0).shiftVariationOf(PILLARMAN_EVASION)));

    // NEXT IS MODE TECHNIQUES---------------------

    public static final RegistryObject<PillarmanAction> PILLARMAN_SMALL_SANDSTORM = ACTIONS.register("pillarman_small_sandstorm", 
            () -> new PillarmanSmallSandstorm(new NonStandAction.Builder().energyCost(20F).cooldown(10).swingHand()));

    public static final RegistryObject<PillarmanAction> PILLARMAN_DIVINE_SANDSTORM = ACTIONS.register("pillarman_divine_sandstorm", 
            () -> new PillarmanDivineSandstorm(new NonStandAction.Builder().holdToFire(40, true).heldWalkSpeed(0.2F)
                    .shiftVariationOf(PILLARMAN_SMALL_SANDSTORM)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_WIND_CLOAK = ACTIONS.register("pillarman_wind_cloak", 
            () -> new PillarmanWindCloak(new NonStandAction.Builder().holdType(200).holdEnergyCost(2F).cooldown(100)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_ATMOSPHERIC_RIFT = ACTIONS.register("pillarman_atmospheric_rift", 
            () -> new PillarmanAtmosphericRift(new NonStandAction.Builder().holdToFire(60, true).heldWalkSpeed(0.1F)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_ERRATIC_BLAZE_KING = ACTIONS.register("pillarman_erratic_blaze_king", 
            () -> new PillarmanErraticBlazeKing(new NonStandAction.Builder().energyCost(20F).heldWalkSpeed(0.1F)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_GIANT_CARTHWHEEL_PRISON = ACTIONS.register("pillarman_giant_carthwheel_prison", 
            () -> new PillarmanGiantCarthwheelPrison(new NonStandAction.Builder().energyCost(125F).holdToFire(30, false).heldWalkSpeed(0).cooldown(100)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_SELF_DETONATION = ACTIONS.register("pillarman_self_detonation", 
            () -> new PillarmanSelfDetonation(new NonStandAction.Builder().energyCost(150F).holdToFire(60, false).heldWalkSpeed(0)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_LIGHT_FLASH = ACTIONS.register("pillarman_light_flash", 
            () -> new PillarmanLightFlash(new NonStandAction.Builder().energyCost(25F).cooldown(80).holdToFire(60, false)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_BLADE_DASH_ATTACK = ACTIONS.register("pillarman_blade_dash_attack", 
            () -> new PillarmanBladeDashAttack(new NonStandAction.Builder().energyCost(40F).cooldown(10)));

    public static final RegistryObject<PillarmanAction> PILLARMAN_BLADE_BARRAGE = ACTIONS.register("pillarman_blade_barrage", 
            () -> new PillarmanBladeBarrage(new PillarmanAction.Builder().holdEnergyCost(2.5F).heldWalkSpeed(0.5F)));



    public static final RegistryObject<PillarmanPowerType> PILLAR_MAN = NON_STAND_POWERS.register("pillarman", 
            () -> new PillarmanPowerType(
                    new PillarmanAction[] {
                            PILLARMAN_HEAVY_PUNCH.get()
                    }, 
                    new PillarmanAction[] {
                            PILLARMAN_STONE_FORM.get()
                    },
                    PILLARMAN_ABSORPTION.get()
                    ).withColor(PillarmanPowerType.COLOR));
}
