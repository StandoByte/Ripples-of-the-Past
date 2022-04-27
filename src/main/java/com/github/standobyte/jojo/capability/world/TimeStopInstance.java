package com.github.standobyte.jojo.capability.world;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class TimeStopInstance {
    private final World world;
    private int ticks;
    final ChunkPos centerPos;
    final int chunkRange;
    @Nullable
    final LivingEntity user;
    @Nullable
    private SoundEvent timeResumeSound;
    @Nullable
    private SoundEvent timeResumeVoiceLine;
    
    public TimeStopInstance(World world, int ticks, ChunkPos pos, int chunkRange, 
            LivingEntity user, SoundEvent timeResumeSound, SoundEvent timeResumeVoiceLine) {
        this.world = world;
        this.ticks = ticks;
        this.centerPos = pos;
        this.chunkRange = chunkRange;
        this.user = user;
        this.timeResumeSound = timeResumeSound;
        this.timeResumeVoiceLine = timeResumeVoiceLine;
    }
    
    public static TimeStopInstance withoutSounds(World world, int ticks, ChunkPos pos, int chunkRange) {
        return new TimeStopInstance(world, ticks, pos, chunkRange, null, null, null);
    }
    
    public boolean tick() {
        if (user != null) {
            if (!user.isAlive()) {
                return true;
            }
            tickSounds();
        }
        return --ticks <= 0;
    }

    int getTicksLeft() {
        return ticks;
    }
    
    public boolean isTimeStopped(ChunkPos pos) {
        return ticks > 0 || inRange(pos);
    }
    
    public boolean inRange(ChunkPos pos) {
        if (chunkRange <= 0) {
            return true;
        }
        return Math.abs(centerPos.x - pos.x) < chunkRange && Math.abs(centerPos.z - pos.z) < chunkRange;
    }
    
    private static final int TIME_RESUME_SOUND_TICKS = 10;
    private static final int TIME_RESUME_VOICELINE_TICKS = 30;
    public void tickSounds() {
        if (!world.isClientSide() && (ticks == TIME_RESUME_SOUND_TICKS || ticks == TIME_RESUME_VOICELINE_TICKS)) {
            if (ticks == TIME_RESUME_SOUND_TICKS && timeResumeSound != null) {
                PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeResumeSound, SoundCategory.AMBIENT, user.blockPosition(), 5.0F, 1.0F), 
                        world.dimension(), player -> (inRange(new ChunkPos(player.blockPosition()))) && TimeUtil.canPlayerSeeInStoppedTime(player));
            }
            else if (timeResumeVoiceLine != null) {
                JojoModUtil.sayVoiceLine(user, timeResumeVoiceLine);
            }
        }
    }
    
    public void removeSoundsIfCrosses(TimeStopInstance newInstance) {
        if (this.ticks < newInstance.ticks && newInstance.inRange(this.centerPos)) {
            timeResumeSound = null;
            timeResumeVoiceLine = null;
        }
    }
}
