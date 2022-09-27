package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.Technique;
import com.github.standobyte.jojo.power.stand.IStandPower;

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
                otherPower.getTypeSpecificData(ModNonStandPowers.HAMON.get()).map(hamon -> 
                hamon.getTechnique() == Technique.JONATHAN).orElse(false)).orElse(false)) {
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
        return shortedHoldDuration(power, super.getHoldDurationToFire(power));
    }
    
    private int shortedHoldDuration(IStandPower power, int ticks) {
        return ticks > 0 && power.getResolveLevel() >= 4 ? 20 : ticks;
    }
    
    @Override
    public boolean cancelHeldOnGettingAttacked(IStandPower power, DamageSource dmgSource, float dmgAmount) {
        return true;
    }
}
