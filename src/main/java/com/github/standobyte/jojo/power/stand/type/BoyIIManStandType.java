package com.github.standobyte.jojo.power.stand.type;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.stats.StandStats;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;

// FIXME !!!!! uncollectible, for villager kid only
public class BoyIIManStandType<T extends StandStats> extends StandType<T> {
    
    public BoyIIManStandType(int color, ITextComponent partName, 
            StandAction[] attacks, StandAction[] abilities, 
            Class<T> statsClass, T defaultStats) {
        super(color, partName, attacks, abilities, statsClass, defaultStats);
    }

    @Override
    public void unsummon(LivingEntity user, IStandPower standPower) {}

    @Override
    public void forceUnsummon(LivingEntity user, IStandPower standPower) {}
}
