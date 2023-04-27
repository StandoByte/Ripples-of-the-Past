package com.github.standobyte.jojo.client.sound;

import javax.annotation.Nullable;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;

public class ResolvedLocationSimpleSound implements ISound {
    protected final Sound sound;
    protected final SoundCategory source;
    protected final ITextComponent subtitle;
    protected float volume = 1.0F;
    protected float pitch = 1.0F;
    protected double x = 0;
    protected double y = 0;
    protected double z = 0;
    protected boolean looping = false;
    protected int delay;
    protected ISound.AttenuationType attenuation = ISound.AttenuationType.NONE;
    protected boolean relative = true;

    public ResolvedLocationSimpleSound(Sound sound, SoundCategory source) {
        this(sound, source, null);
    }

    public ResolvedLocationSimpleSound(Sound sound, SoundCategory source, @Nullable ITextComponent subtitle) {
        this.sound = sound != null ? sound : SoundHandler.EMPTY_SOUND;
        this.source = source;
        this.subtitle = subtitle;
    }

    @Override
    public ResourceLocation getLocation() {
        return sound.getLocation();
    }
    
    @Override
    public SoundEventAccessor resolve(SoundHandler soundManager) {
        return new EventlessSoundAccessor(sound.getLocation(), subtitle, sound);
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public SoundCategory getSource() {
        return source;
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public float getVolume() {
        return volume * sound.getVolume();
    }

    @Override
    public float getPitch() {
        return pitch * sound.getPitch();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public ISound.AttenuationType getAttenuation() {
        return attenuation;
    }

    @Override
    public boolean isRelative() {
        return relative;
    }

    @Override
    public String toString() {
        return "SoundInstance[" + getLocation() + "]";
    }

}
