package com.github.standobyte.jojo.init.power.non_stand;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.pillarman.ModPillarmanActions;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.init.power.non_stand.zombie.ModZombieActions;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.zombie.ZombiePowerType;

public class ModPowers {

    public static final Supplier<HamonPowerType> HAMON = ModHamonActions.HAMON;
    public static final Supplier<VampirismPowerType> VAMPIRISM = ModVampirismActions.VAMPIRISM;
    public static final Supplier<ZombiePowerType> ZOMBIE = ModZombieActions.ZOMBIE;
    public static final Supplier<PillarmanPowerType> PILLAR_MAN = ModPillarmanActions.PILLAR_MAN;
}
