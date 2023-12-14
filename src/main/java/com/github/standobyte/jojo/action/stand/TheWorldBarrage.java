package com.github.standobyte.jojo.action.stand;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;

public class TheWorldBarrage extends StandEntityMeleeBarrage {
    private final Supplier<SoundEvent> wryyyyyyyyyyy;

    public TheWorldBarrage(Builder builder, Supplier<SoundEvent> greatestHighShout) {
        super(builder);
        this.wryyyyyyyyyyy = greatestHighShout == null ? () -> null : greatestHighShout;
    }

    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        return wasActive && TimeStop.vampireTimeStopDuration(user) ? wryyyyyyyyyyy.get() : super.getShout(user, power, target, wasActive);
    }
    
    @Override
    public Stream<SoundEvent> getSounds(StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task) {
        if (Optional.ofNullable(standPower.getUser()).map(
                user -> user.getCapability(ClientPlayerUtilCapProvider.CAPABILITY).map(
                        cap -> cap.lastVoiceLineTriggered).orElse(false))
                .orElse(false)) {
            return null;
        }
        return super.getSounds(standEntity, standPower, phase, task);
    }
}
