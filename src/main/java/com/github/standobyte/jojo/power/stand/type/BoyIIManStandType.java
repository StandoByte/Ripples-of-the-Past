package com.github.standobyte.jojo.power.stand.type;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.stats.StandStats;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;

public class BoyIIManStandType<T extends StandStats> extends StandType<T> {
    
    public BoyIIManStandType(int color, ITextComponent partName, 
            StandAction[] attacks, StandAction[] abilities, 
            Class<T> statsClass, T defaultStats,  @Nullable StandTypeOptionals additions) {
        super(color, partName, attacks, abilities, null, statsClass, defaultStats, additions);
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
