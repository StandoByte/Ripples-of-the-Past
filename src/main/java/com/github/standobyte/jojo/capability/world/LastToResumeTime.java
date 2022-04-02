package com.github.standobyte.jojo.capability.world;

import java.lang.ref.WeakReference;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class LastToResumeTime {
    private final WeakReference<LivingEntity> entityToResumeTime;
    private final SoundEvent timeResumeSound;
    private final SoundEvent timeResumeVoiceLine;
    
    public LastToResumeTime(LivingEntity user, SoundEvent timeResumeSound, SoundEvent timeResumeVoiceLine) {
        this.entityToResumeTime = new WeakReference<>(user);
        this.timeResumeSound = timeResumeSound;
        this.timeResumeVoiceLine = timeResumeVoiceLine;
    }

    private static final int TIME_RESUME_SOUND_TICKS = 10;
    private static final int TIME_RESUME_VOICELINE_TICKS = 30;
    public void playSounds(int timeStopTicksLeft, ChunkPos chunkPos, World world) {
        if (!world.isClientSide() && (timeStopTicksLeft == TIME_RESUME_SOUND_TICKS || timeStopTicksLeft == TIME_RESUME_VOICELINE_TICKS)) {
            if (entityToResumeTime != null) {
                LivingEntity entity = entityToResumeTime.get();
                if (entity != null) {
                    if (timeStopTicksLeft == TIME_RESUME_SOUND_TICKS && timeResumeSound != null) {
                        PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeResumeSound, SoundCategory.AMBIENT, entity.blockPosition(), 5.0F, 1.0F), 
                                world.dimension(), player -> (JojoModConfig.getCommonConfigInstance().inTimeStopRange(
                                        chunkPos, new ChunkPos(player.blockPosition()))) && TimeHandler.canPlayerSeeInStoppedTime(player));
                    }
                    else if (timeResumeVoiceLine != null) {
                        JojoModUtil.sayVoiceLine(entity, timeResumeVoiceLine);
                    }
                }
            }
        }
    }
}
