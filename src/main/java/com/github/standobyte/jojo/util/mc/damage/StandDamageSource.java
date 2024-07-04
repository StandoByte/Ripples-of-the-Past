package com.github.standobyte.jojo.util.mc.damage;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.util.DamageSource;

public class StandDamageSource extends DamageSource implements IStandDamageSource {
    protected final IStandPower stand;
    private boolean standCanHitSelf = false;
    
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
    
    @Override
    public StandDamageSource setStandCanHitSelf() {
        standCanHitSelf = true;
        return this;
    }

    @Override
    public boolean standCanHitSelf() {
        return standCanHitSelf;
    }
}
