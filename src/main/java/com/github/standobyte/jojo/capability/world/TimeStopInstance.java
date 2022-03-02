package com.github.standobyte.jojo.capability.world;

import java.lang.ref.WeakReference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;

public class TimeStopInstance {
    private int ticks;
    private final WeakReference<LivingEntity> entityToResumeTime;
    private final SoundEvent timeResumeSound;
    private final SoundEvent timeResumeVoiceLine;
    
    public TimeStopInstance(int ticks, LivingEntity user, SoundEvent timeResumeSound, SoundEvent timeResumeVoiceLine) {
        this.ticks = ticks;
        this.entityToResumeTime = new WeakReference<>(user);
        this.timeResumeSound = timeResumeSound;
        this.timeResumeVoiceLine = timeResumeVoiceLine;
    }
    
    public boolean tick() {
        
        return --ticks <= 0;
    }

}
