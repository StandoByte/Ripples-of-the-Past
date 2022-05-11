package com.github.standobyte.jojo.action.actions;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;

public class TheWorldBarrage extends StandEntityMeleeBarrage {
    private final Supplier<SoundEvent> wryyyyyyyyyyy;

    public TheWorldBarrage(Builder builder, Supplier<SoundEvent> greatestHighShout) {
        super(builder);
        this.wryyyyyyyyyyy = greatestHighShout == null ? () -> null : greatestHighShout;
    }

    // FIXME (!!!!) DIO and TW muda not overlapping (especially for tracking)
    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        return wasActive && TimeStop.vampireTimeStopDuration(user) ? wryyyyyyyyyyy.get() : super.getShout(user, power, target, wasActive);
//        return null;
    }
    
    @Override
    protected SoundEvent getSound(StandEntity standEntity, IStandPower standPower, Phase phase) {
        return null;
//        return super.getSound(standEntity, standPower, phase);
    }
}
