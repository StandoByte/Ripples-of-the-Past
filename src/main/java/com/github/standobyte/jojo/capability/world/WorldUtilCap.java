package com.github.standobyte.jojo.capability.world;

import java.lang.ref.WeakReference;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class WorldUtilCap {
    private final World world;
    int timeStopTicks;
    public boolean gameruleDayLightCycle;
    public boolean gameruleWeatherCycle;
    
    private WeakReference<LivingEntity> entityToResumeTime;
    private SoundEvent timeResumeVoiceLine;
    private SoundEvent timeResumeSound;
    
    public WorldUtilCap(World world) {
        this.world = world;
    }
    
    public boolean setTimeStopTicks(int ticks) {
        boolean timeWasStoppedAlready = timeStopTicks > 0;
        this.timeStopTicks = Math.max(timeStopTicks, ticks);
        return !timeWasStoppedAlready && ticks > 0;
    }
    
    public void resetTimeStopTicks() {
        timeStopTicks = 0;
    }
    
    public boolean isTimeStopped() {
        return timeStopTicks > 0;
    }
    
    public void decTimeStopTicks() {
        if (timeStopTicks > 0) {
            handleTimeResumeSounds();
            timeStopTicks--;
        }
    }
    
    public int getTimeStopTicks() {
        return timeStopTicks;
    }
    
    public void setLastToResumeTime(LivingEntity entity, SoundEvent sound, SoundEvent voiceLine) {
        this.entityToResumeTime = new WeakReference<>(entity);
        this.timeResumeSound = sound;
        this.timeResumeVoiceLine = voiceLine;
    }
    
    private static final int TIME_RESUME_SOUND_TICKS = 10;
    private static final int TIME_RESUME_VOICELINE_TICKS = 30;
    private void handleTimeResumeSounds() {
        if (!world.isClientSide() && (timeStopTicks == TIME_RESUME_SOUND_TICKS || timeStopTicks == TIME_RESUME_VOICELINE_TICKS)) {
            if (entityToResumeTime != null) {
                LivingEntity entity = entityToResumeTime.get();
                if (entity != null) {
                    if (timeStopTicks == TIME_RESUME_SOUND_TICKS && timeResumeSound != null) {
                        PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeResumeSound, SoundCategory.AMBIENT, entity.blockPosition(), 5.0F, 1.0F), 
                                world.dimension(), TimeHandler::canPlayerSeeInStoppedTime);
                    }
                    else if (timeResumeVoiceLine != null) {
                        JojoModUtil.sayVoiceLine(entity, timeResumeVoiceLine);
                    }
                }
            }
        }
    }
}
