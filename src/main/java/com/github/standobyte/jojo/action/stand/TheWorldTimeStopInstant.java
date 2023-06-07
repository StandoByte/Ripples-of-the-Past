package com.github.standobyte.jojo.action.stand;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

public class TheWorldTimeStopInstant extends TimeStopInstant {

    public TheWorldTimeStopInstant(StandAction.Builder builder, 
            @Nonnull Supplier<TimeStop> baseTimeStopAction, @Nonnull Supplier<SoundEvent> blinkSound) {
        super(builder, baseTimeStopAction, blinkSound);
    }
    
    @Override
    protected Vector3d getEntityTargetTeleportPos(Entity user, Entity target) {
        return target.position().subtract(target.getLookAngle().scale(target.getBbWidth() + user.getBbWidth()));
    }
}
