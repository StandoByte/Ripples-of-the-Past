package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;

public class TheWorldTimeStop extends TimeStop {

    public TheWorldTimeStop(Builder builder) {
        super(builder);
    }

    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        if (user.getRandom().nextFloat() < 0.05F && 
                INonStandPower.getNonStandPowerOptional(user).map(otherPower -> 
                otherPower.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> 
                hamon.characterIs(ModHamonSkills.CHARACTER_JONATHAN.get())).orElse(false)).orElse(false)) {
            return ModSounds.JONATHAN_THE_WORLD.get();
        }
        return super.getShout(user, power, target, wasActive);
    }

    @Override
    protected boolean autoSummonStand(IStandPower power) {
        return super.autoSummonStand(power) || power.getResolveLevel() < 3;
    }
    
    @Override
    public int getHoldDurationToFire(IStandPower power) { 
        return shortenedHoldDuration(power, super.getHoldDurationToFire(power));
    }
    
    private int shortenedHoldDuration(IStandPower power, int ticks) {
        return ticks > 0 && power.getResolveLevel() >= 4 ? 20 : ticks;
    }
    
    @Override
    public boolean cancelHeldOnGettingAttacked(IStandPower power, DamageSource dmgSource, float dmgAmount) {
        return true;
    }
}
