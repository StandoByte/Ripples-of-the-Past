package com.github.standobyte.jojo.util.damage;

import com.github.standobyte.jojo.power.stand.IStandPower;

public interface IStandDamageSource {
    IStandPower getStandPower();
    int getStandInvulTicks();
}
