package com.github.standobyte.jojo.power.impl.stand.type;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;

public class NoManifestationStandType<T extends StandStats> extends StandType<T> {

    public NoManifestationStandType(int color, ITextComponent partName, StandAction[] attacks, StandAction[] abilities,
            Class<T> statsClass, T defaultStats, @Nullable StandTypeOptionals additions) {
        super(color, partName, attacks, abilities, abilities.length > 0 ? abilities[0] : null, statsClass, defaultStats, additions);
    }

    protected NoManifestationStandType(Builder<T> builder) {
        super(builder);
    }

    @Override
    public boolean summon(LivingEntity user, IStandPower standPower, boolean withoutNameVoiceLine) {
        return false;
    }

    @Override
    public void unsummon(LivingEntity user, IStandPower standPower) {}

    @Override
    public void forceUnsummon(LivingEntity user, IStandPower standPower) {}
    
    
    
    public static class Builder<T extends StandStats> extends StandType.AbstractBuilder<Builder<T>, T>{

        @Override
        protected Builder<T> getThis() {
            return this;
        }
        
        @Override
        public NoManifestationStandType<T> build() {
            return new NoManifestationStandType<>(this);
        }
        
    }

}
