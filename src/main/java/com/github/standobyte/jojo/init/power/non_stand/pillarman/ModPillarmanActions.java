package com.github.standobyte.jojo.init.power.non_stand.pillarman;

import static com.github.standobyte.jojo.init.power.ModCommonRegisters.ACTIONS;
import static com.github.standobyte.jojo.init.power.ModCommonRegisters.NON_STAND_POWERS;

import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.action.non_stand.PillarmanAbsorption;
import com.github.standobyte.jojo.action.non_stand.PillarmanAction;
import com.github.standobyte.jojo.action.non_stand.PillarmanHeavyPunch;
import com.github.standobyte.jojo.action.non_stand.PillarmanStoneForm;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanPowerType;

import net.minecraftforge.fml.RegistryObject;

public class ModPillarmanActions {

public static void loadRegistryObjects() {}

public static final RegistryObject<PillarmanAction> PILLARMAN_HEAVY_PUNCH = ACTIONS.register("pillarman_heavy_punch", 
        () -> new PillarmanHeavyPunch(new NonStandAction.Builder().needsFreeMainHand().swingHand().energyCost(60F).cooldown(40)));

public static final RegistryObject<PillarmanAction> PILLARMAN_ABSORPTION = ACTIONS.register("pillarman_absorption", 
        () -> new PillarmanAbsorption(new NonStandAction.Builder().needsFreeMainHand()));

public static final RegistryObject<PillarmanAction> PILLARMAN_STONE_FORM = ACTIONS.register("pillarman_stone_form", 
        () -> new PillarmanStoneForm(new NonStandAction.Builder().holdToFire(80, false)));

    public static final RegistryObject<PillarmanPowerType> PILLAR_MAN = NON_STAND_POWERS.register("pillarman", 
            () -> new PillarmanPowerType(
                    new PillarmanAction[] {
                    		PILLARMAN_HEAVY_PUNCH.get(),
                    		PILLARMAN_ABSORPTION.get()
                    }, 
                    new PillarmanAction[] {
                    		PILLARMAN_STONE_FORM.get()
                    },
                    PILLARMAN_ABSORPTION.get()
                    ));

}
