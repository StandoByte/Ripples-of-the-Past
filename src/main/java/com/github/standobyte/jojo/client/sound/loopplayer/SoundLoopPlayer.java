package com.github.standobyte.jojo.client.sound.loopplayer;

import java.util.Random;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Deprecated
public abstract class SoundLoopPlayer {
    protected static final Random RANDOM = new Random();
    protected final World world;
    private SoundEvent sound;
    private SoundCategory soundCategory;
    private float volume;
    private float pitch;
    private int tickCount = 0;
    private int nextSoundIn = 0;
    protected boolean playedSoundThisTick;
    private boolean stopped = false;
    
    public SoundLoopPlayer(World world, SoundEvent sound, SoundCategory soundCategory, float volume, float pitch) {
        this.world = world;
        this.sound = sound;
        this.soundCategory = soundCategory;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public void tick() {
        if (!continuePlaying()) {
            stopped = true;
            return;
        }
        
        if (tickCount++ >= nextSoundIn) {
            Vector3d pos = soundPos();
            world.playLocalSound(pos.x, pos.y, pos.z, sound, soundCategory, volume, pitch, true);
            nextSoundIn = tickCount + soundDelayTicks();
            playedSoundThisTick = true;
        }
        else {
            playedSoundThisTick = false;
        }
    }
    
    protected abstract int soundDelayTicks();
    protected abstract boolean continuePlaying();
    protected abstract Vector3d soundPos();
    
    public boolean isStopped() {
        return stopped;
    }
}
