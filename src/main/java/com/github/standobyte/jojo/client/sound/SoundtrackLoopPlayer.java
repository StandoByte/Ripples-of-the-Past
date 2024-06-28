package com.github.standobyte.jojo.client.sound;

import java.util.OptionalInt;

import org.lwjgl.openal.AL10;

import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.AudioStreamManager;
import net.minecraft.client.audio.ChannelManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.sound.PlayStreamingSourceEvent;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;

public class SoundtrackLoopPlayer {
    protected final LivingEntity bossEntity;
    protected final SoundEvent start;
    protected final SoundEvent main;
    protected final SoundEvent finish;
    protected SoundCategory category;
    protected float volume;
    protected float pitch;

    boolean queuedMain = false;
    boolean setLooped = false;
    boolean finished = false;

    protected ISound startingSound;
    protected ChannelManager.Entry channelEntry;
    protected OptionalInt soundSourceID = OptionalInt.empty();
    protected OptionalInt startingSoundBuffer = OptionalInt.empty();

    public SoundtrackLoopPlayer(LivingEntity entity, SoundEvent start, SoundEvent main, SoundEvent finish) {
        this(entity, start, main, finish, SoundCategory.RECORDS, 0.4f, 1);
    }

    public SoundtrackLoopPlayer(LivingEntity entity, SoundEvent start, SoundEvent main, SoundEvent finish, 
            SoundCategory category, float volume, float pitch) {
        this.start = start;
        this.main = main;
        this.finish = finish;
        this.bossEntity = entity;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    protected void start() {
        SoundHandler soundManager = Minecraft.getInstance().getSoundManager();
        startingSound = new BackgroundSound(start.getLocation(), category, volume, pitch, false, 0, ISound.AttenuationType.NONE, 0, 0, 0, false);
        soundManager.play(startingSound);
    }
    
    protected void onSoundSourceEvent(SoundSourceEvent event) {
        if (!queuedMain && startingSound != null && event.getSound() == startingSound) {
            SoundEngine soundEngine = event.getManager();
            SoundSource startSoundSource = event.getSource();

            ISound iMainSound = new SimpleSound(main.getLocation(), startingSound.getSource(), 
                    startingSound.getVolume(), startingSound.getPitch(), true, 0, startingSound.getAttenuation(), 
                    startingSound.getX(), startingSound.getY(), startingSound.getZ(), startingSound.isRelative());
            SoundEventAccessor mainSoundAccessor = iMainSound.resolve(soundEngine.soundManager);
            if (mainSoundAccessor != null) {
                Sound mainSound = iMainSound.getSound();
                if (mainSound != SoundHandler.EMPTY_SOUND) {
                    ResourceLocation mainSoundPath = mainSound.getPath();
//                    boolean isStartSoundStream = event instanceof PlayStreamingSourceEvent;
//                    boolean isMainSoundStream = mainSound.shouldStream();
                    AudioStreamManager soundBuffers = ClientReflection.getSoundBuffers(soundEngine); // TODO cache this
                    
//                    if (!isMainSoundStream) {
                        soundBuffers.getCompleteBuffer(startingSound.getSound().getPath()).thenAccept(startingAudioStream -> 
                        soundBuffers.getCompleteBuffer(mainSoundPath).thenAccept(mainAudioStream -> {
                            ClientReflection.getAlBuffer(startingAudioStream).ifPresent(buffer1 -> 
                            ClientReflection.getAlBuffer(mainAudioStream).ifPresent(buffer2 -> {
                                startingSoundBuffer = OptionalInt.of(buffer1);
                                soundSourceID = OptionalInt.of(ClientReflection.getSourceId(startSoundSource));
                                startSoundSource.stop();
                                AL10.alSourcei(soundSourceID.getAsInt(), AL10.AL_BUFFER, 0);
                                AL10.alSourceQueueBuffers(soundSourceID.getAsInt(), buffer1);
                                AL10.alSourceQueueBuffers(soundSourceID.getAsInt(), buffer2);
                                startSoundSource.play();
                            }));
                        }));
//                    } else {
                        // TODO ???
//                    }
                }
            }
            
            queuedMain = true;
        }
    }
    
    @SuppressWarnings("deprecation")
    public void tick() {
        if (bossEntity != null) {
            if (bossEntity.isDeadOrDying()) {
                finish();
                return;
            }
            else if (bossEntity.removed) {
                forceStop();
                return;
            }
        }
        
        
        if (!setLooped) {
            soundSourceID.ifPresent(soundSource -> {
                startingSoundBuffer.ifPresent(buffer -> {
                    int curBuffer = AL10.alGetSourcei(soundSource, AL10.AL_BUFFERS_PROCESSED);
                    boolean startingSoundStopped = curBuffer == 1;
                    if (startingSoundStopped) {
                        AL10.alSourceUnqueueBuffers(soundSource, new int[] { buffer });
                        AL10.alSourcei(soundSource, AL10.AL_LOOPING, 1);
                        setLooped = true;
                    }
                });
            });
        }
    }
    
    public void finish() {
        SoundHandler soundManager = Minecraft.getInstance().getSoundManager();
        if (!finished) {
            ISound sound = new SimpleSound(finish.getLocation(), category, volume, pitch, false, 0, ISound.AttenuationType.NONE, 0, 0, 0, false);
            soundManager.play(sound);
        }
        forceStop();
    }
    
    public void forceStop() {
        SoundHandler soundManager = Minecraft.getInstance().getSoundManager();
        if (startingSound != null) {
            soundManager.stop(startingSound);
            startingSound = null;
        }
        finished = true;
    }
    
    public boolean hasFinished() {
        return finished;
    }
    
    
    
    public static class BackgroundSound extends SimpleSound {

        public BackgroundSound(ResourceLocation location, SoundCategory source, float volume,
                float pitch, boolean looping, int delay, AttenuationType attenuation,
                double x, double y, double z, boolean isRelative) {
            super(location, source, volume, pitch, looping, delay, attenuation, x,
                    y, z, isRelative);
        }
        
        @Override
        public boolean canStartSilent() {
            return true;
        }
        
    }
    
}
