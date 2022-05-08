package com.github.standobyte.jojo.capability.world;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlaySoundAtClientPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncWorldTimeStopPacket;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.TimeUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class TimeStopInstance {
    private static int i = 0;
    private final World world;
    private final int id;
    private int ticks;
    final ChunkPos centerPos;
    final int chunkRange;
    @Nullable
    final LivingEntity user;
    @Nullable
    private SoundEvent timeResumeSound;
    @Nullable
    private SoundEvent timeResumeVoiceLine;
    @Nullable
    private SoundEvent timeManualResumeVoiceLine;
    private boolean ticksManuallySet = false;
    private boolean alwaysSayVoiceLine = false;
    
    public TimeStopInstance(World world, int ticks, ChunkPos pos, int chunkRange, LivingEntity user) {
        this(world, ticks, pos, chunkRange, user, i++);
    }
    
    public TimeStopInstance(World world, int ticks, ChunkPos pos, int chunkRange, LivingEntity user, int id) {
        this.world = world;
        this.ticks = ticks;
        this.centerPos = pos;
        this.chunkRange = chunkRange;
        this.user = user;
        this.id = id;
    }
    
    public TimeStopInstance setSounds(SoundEvent timeResumeSound, SoundEvent timeResumeVoiceLine, SoundEvent timeManualResumeVoiceLine) {
        this.timeResumeSound = timeResumeSound;
        this.timeResumeVoiceLine = timeResumeVoiceLine;
        this.timeManualResumeVoiceLine = timeManualResumeVoiceLine;
        return this;
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
    
    public void setTicksLeft(int ticks) {
        this.ticks = ticks;
        ticksManuallySet = true;
        if (ticks < TIME_RESUME_VOICELINE_TICKS) {
            alwaysSayVoiceLine = true;
        }
    }
    
    public boolean wereTicksManuallySet() {
        return ticksManuallySet;
    }

    public int getTicksLeft() {
        return ticks;
    }
    
    public boolean isTimeStopped(ChunkPos pos) {
        return ticks > 0 && inRange(pos);
    }
    
    public boolean inRange(ChunkPos pos) {
        if (chunkRange <= 0) {
            return true;
        }
        return Math.abs(centerPos.x - pos.x) < chunkRange && Math.abs(centerPos.z - pos.z) < chunkRange;
    }
    
    public static final int TIME_RESUME_SOUND_TICKS = 10;
    public static final int TIME_RESUME_VOICELINE_TICKS = 30;
    public void tickSounds() {
        if (!world.isClientSide() && (ticks == TIME_RESUME_SOUND_TICKS || ticks == TIME_RESUME_VOICELINE_TICKS || alwaysSayVoiceLine)) {
            if (ticks == TIME_RESUME_SOUND_TICKS) {
                if (timeResumeSound != null) {
                    PacketManager.sendGloballyWithCondition(new PlaySoundAtClientPacket(timeResumeSound, SoundCategory.AMBIENT, user.blockPosition(), 5.0F, 1.0F), 
                            world.dimension(), player -> (inRange(new ChunkPos(player.blockPosition()))) && TimeUtil.canPlayerSeeInStoppedTime(player));
                }
            }
            else {
                SoundEvent voiceLine = ticksManuallySet ? timeManualResumeVoiceLine : timeResumeVoiceLine;
                if (voiceLine != null) {
                    JojoModUtil.sayVoiceLine(user, voiceLine);
                }
                alwaysSayVoiceLine = false;
            }
        }
    }
    
    public void removeSoundsIfCrosses(TimeStopInstance newInstance) {
        if (this.ticks < newInstance.ticks && newInstance.inRange(this.centerPos)) {
            timeResumeSound = null;
            timeResumeVoiceLine = null;
            timeManualResumeVoiceLine = null;
        }
    }
    
    public SoundEvent getTimeResumeSound() {
        return timeResumeSound;
    }
    
    public int getId() {
        return id;
    }
    
    public void syncToClient(ServerPlayerEntity player) {
        boolean canMove = TimeUtil.canPlayerMoveInStoppedTime(player, true);
        boolean canSee = TimeUtil.canPlayerSeeInStoppedTime(canMove, TimeUtil.hasTimeStopAbility(player));
        PacketManager.sendToClient(new SyncWorldTimeStopPacket(ticks, centerPos, 
                canSee, canMove, user == null ? -1 : user.getId(), id), player);
    }
}
