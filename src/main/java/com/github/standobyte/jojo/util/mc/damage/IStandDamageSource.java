package com.github.standobyte.jojo.util.mc.damage;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

public interface IStandDamageSource {
    IStandPower getStandPower();
    int getStandInvulTicks();
    
    IStandDamageSource setStandCanHitSelf();
    boolean standCanHitSelf();
}
