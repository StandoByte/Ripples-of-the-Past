package com.github.standobyte.jojo.action.stand;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.util.SoundEvent;

@Deprecated
/**
 * @deprecated use the TimeStopInstant(StandAction.Builder, Supplier<TimeStop>, Supplier<SoundEvent>, true) constructor
 */
public class TheWorldTimeStopInstant extends TimeStopInstant {

    public TheWorldTimeStopInstant(StandAction.Builder builder, 
            @Nonnull Supplier<TimeStop> baseTimeStopAction, @Nonnull Supplier<SoundEvent> blinkSound) {
        super(builder, baseTimeStopAction, blinkSound, true);
    }
}
