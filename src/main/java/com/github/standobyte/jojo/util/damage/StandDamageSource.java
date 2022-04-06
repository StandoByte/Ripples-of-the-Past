package com.github.standobyte.jojo.util.damage;

import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.util.DamageSource;

public class StandDamageSource extends DamageSource implements IStandDamageSource {
    protected final IStandPower stand;
    
    public StandDamageSource(String msgId, IStandPower stand) {
        super(msgId);
        this.stand = stand;
    }
    
    StandDamageSource(DamageSource source, IStandPower stand) {
        this(source.msgId, stand);
    }

    @Override
    public String toString() {
       return "StandDamageSource (" + msgId + ")";
    }
    
    @Override
    public IStandPower getStandPower() {
        return stand;
    }

    @Override
    public int getStandInvulTicks() {
        return 0;
    }
}
