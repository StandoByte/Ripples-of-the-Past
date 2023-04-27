package com.github.standobyte.jojo.util.mc.damage;

import com.github.standobyte.jojo.power.stand.IStandPower;

public interface IStandDamageSource {
    IStandPower getStandPower();
    int getStandInvulTicks();
}
