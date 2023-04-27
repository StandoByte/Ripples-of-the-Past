package com.github.standobyte.jojo.init.power.non_stand;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.VampirismPowerType;

public class ModPowers {

    public static final Supplier<HamonPowerType> HAMON = ModHamonActions.HAMON;
    public static final Supplier<VampirismPowerType> VAMPIRISM = ModVampirismActions.VAMPIRISM;
}
