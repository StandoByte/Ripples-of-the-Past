package com.github.standobyte.jojo.power.stand.type;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.stats.StandStats;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;

public class NoManifestationStandType<T extends StandStats> extends StandType<T> {

    public NoManifestationStandType(int color, ITextComponent partName, StandAction[] attacks, StandAction[] abilities,
            Class<T> statsClass, T defaultStats, StandTypeOptionals additions) {
        super(color, partName, attacks, abilities, abilities.length > 0 ? abilities[0] : null, statsClass, defaultStats, additions);
    }

    @Override
    public boolean summon(LivingEntity user, IStandPower standPower, boolean withoutNameVoiceLine) {
        return false;
    }

    @Override
    public void unsummon(LivingEntity user, IStandPower standPower) {}

    @Override
    public void forceUnsummon(LivingEntity user, IStandPower standPower) {}

}
