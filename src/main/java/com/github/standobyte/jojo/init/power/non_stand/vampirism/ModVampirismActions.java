package com.github.standobyte.jojo.init.power.non_stand.vampirism;

import static com.github.standobyte.jojo.init.power.ModCommonRegistries.ACTIONS;
import static com.github.standobyte.jojo.init.power.ModCommonRegistries.NON_STAND_POWERS;

import com.github.standobyte.jojo.action.non_stand.NonStandAction;
import com.github.standobyte.jojo.action.non_stand.VampirismAction;
import com.github.standobyte.jojo.action.non_stand.VampirismBloodDrain;
import com.github.standobyte.jojo.action.non_stand.VampirismBloodGift;
import com.github.standobyte.jojo.action.non_stand.VampirismDarkAura;
import com.github.standobyte.jojo.action.non_stand.VampirismFreeze;
import com.github.standobyte.jojo.action.non_stand.VampirismHamonSuicide;
import com.github.standobyte.jojo.action.non_stand.VampirismSpaceRipperStingyEyes;
import com.github.standobyte.jojo.action.non_stand.VampirismZombieSummon;
import com.github.standobyte.jojo.power.nonstand.type.VampirismPowerType;

import net.minecraftforge.fml.RegistryObject;

public class ModVampirismActions {
    
    public static void loadRegistryObjects() {}
    
    public static final RegistryObject<VampirismAction> VAMPIRISM_BLOOD_DRAIN = ACTIONS.registerEntry("vampirism_blood_drain", 
            () -> new VampirismBloodDrain(new NonStandAction.Builder().emptyMainHand()));

    public static final RegistryObject<VampirismAction> VAMPIRISM_FREEZE = ACTIONS.registerEntry("vampirism_freeze", 
            () -> new VampirismFreeze(new NonStandAction.Builder().holdEnergyCost(0.5F).heldWalkSpeed(0.75F).emptyMainHand()));

    public static final RegistryObject<VampirismAction> VAMPIRISM_SPACE_RIPPER_STINGY_EYES = ACTIONS.registerEntry("vampirism_space_ripper_stingy_eyes", 
            () -> new VampirismSpaceRipperStingyEyes(new NonStandAction.Builder().holdType(20)
                    .holdEnergyCost(20F).cooldown(50).heldWalkSpeed(0.3F).ignoresPerformerStun()));

    public static final RegistryObject<VampirismAction> VAMPIRISM_BLOOD_GIFT = ACTIONS.registerEntry("vampirism_blood_gift", 
            () -> new VampirismBloodGift(new NonStandAction.Builder().holdToFire(60, false)
                    .holdEnergyCost(5F).heldWalkSpeed(0.3F).emptyMainHand()));

    public static final RegistryObject<VampirismAction> VAMPIRISM_ZOMBIE_SUMMON = ACTIONS.registerEntry("vampirism_zombie_summon", 
            () -> new VampirismZombieSummon(new NonStandAction.Builder().energyCost(100F).cooldown(100)));

    public static final RegistryObject<VampirismAction> VAMPIRISM_DARK_AURA = ACTIONS.registerEntry("vampirism_dark_aura", 
            () -> new VampirismDarkAura(new NonStandAction.Builder().energyCost(25F).cooldown(300).ignoresPerformerStun()));

    public static final RegistryObject<VampirismAction> VAMPIRISM_HAMON_SUICIDE = ACTIONS.registerEntry("vampirism_hamon_suicide", 
            () -> new VampirismHamonSuicide(new NonStandAction.Builder().holdToFire(100, false).ignoresPerformerStun()));



    public static final RegistryObject<VampirismPowerType> VAMPIRISM = NON_STAND_POWERS.registerEntry("vampirism", 
            () -> new VampirismPowerType(
                    0xFF0000, 
                    new VampirismAction[] {
                            VAMPIRISM_BLOOD_DRAIN.get(), 
                            VAMPIRISM_FREEZE.get(),
                            VAMPIRISM_SPACE_RIPPER_STINGY_EYES.get()}, 
                    new VampirismAction[] {
                            VAMPIRISM_BLOOD_GIFT.get(), 
                            VAMPIRISM_ZOMBIE_SUMMON.get(), 
                            VAMPIRISM_DARK_AURA.get()}
                    ));

}
