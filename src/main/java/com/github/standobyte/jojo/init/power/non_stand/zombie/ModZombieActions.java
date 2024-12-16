package com.github.standobyte.jojo.init.power.non_stand.zombie;

import static com.github.standobyte.jojo.init.power.ModCommonRegisters.ACTIONS;
import static com.github.standobyte.jojo.init.power.ModCommonRegisters.NON_STAND_POWERS;

import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.action.non_stand.ZombieAction;
import com.github.standobyte.jojo.action.non_stand.ZombieClawLacerate;
import com.github.standobyte.jojo.action.non_stand.ZombieDevour;
import com.github.standobyte.jojo.action.non_stand.ZombieDisguise;
import com.github.standobyte.jojo.power.impl.nonstand.type.zombie.ZombiePowerType;

import net.minecraftforge.fml.RegistryObject;

public class ModZombieActions {
    
    public static void loadRegistryObjects() {}
    
    public static final RegistryObject<ZombieAction> ZOMBIE_CLAW_LACERATE = ACTIONS.register("zombie_claw_lacerate", 
            () -> new ZombieClawLacerate(new NonStandAction.Builder().needsFreeMainHand().energyCost(60F)));
    
    public static final RegistryObject<ZombieAction> ZOMBIE_DEVOUR = ACTIONS.register("zombie_devour", 
            () -> new ZombieDevour(new NonStandAction.Builder().needsFreeMainHand()));
    
    public static final RegistryObject<ZombieAction> ZOMBIE_DISGUISE = ACTIONS.register("zombie_disguise", 
            () -> new ZombieDisguise(new NonStandAction.Builder().holdToFire(60, false)));




    public static final RegistryObject<ZombiePowerType> ZOMBIE = NON_STAND_POWERS.register("zombie", 
            () -> new ZombiePowerType(
                    new ZombieAction[] {
                            ZOMBIE_CLAW_LACERATE.get(),
                            ZOMBIE_DEVOUR.get()}, 
                    new ZombieAction[] {
                            ZOMBIE_DISGUISE.get()
                    },
                    ZOMBIE_DEVOUR.get()
                    ).withColor(ZombiePowerType.COLOR));

}