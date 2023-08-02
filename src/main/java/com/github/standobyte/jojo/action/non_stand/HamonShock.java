package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.util.DamageSource;

public class HamonShock extends HamonAction {
    
    public HamonShock(HamonAction.Builder builder) {
        super(builder);
    }
    
    
    @Override
    public boolean cancelHeldOnGettingAttacked(INonStandPower power, DamageSource dmgSource, float dmgAmount) {
        return true;
    }
}
