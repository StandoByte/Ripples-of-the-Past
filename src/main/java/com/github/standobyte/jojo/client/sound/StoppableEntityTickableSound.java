package com.github.standobyte.jojo.client.sound;

import java.util.function.Predicate;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class StoppableEntityTickableSound<T extends Entity> extends TickableSound {
    protected final T entity;
    protected final Predicate<T> playWhile;
    
    private int fadeOutTimer;
    private boolean isFadingOut = false;
    private float volumeReduction;
    
    public StoppableEntityTickableSound(SoundEvent sound, SoundCategory category, T entity, 
            Predicate<T> playWhile) {
        this(sound, category, 1.0F, 1.0F, false, entity, playWhile);
    }
    
    public StoppableEntityTickableSound(SoundEvent sound, SoundCategory category, 
            float volume, float pitch, boolean looping, T entity, Predicate<T> playWhile) {
        super(sound, category);
        this.entity = entity;
        this.playWhile = playWhile;
        this.volume = volume;
        this.pitch = pitch;
        this.looping = looping;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }
    
    public StoppableEntityTickableSound<T> withFadeOut(int ticks) {
        this.fadeOutTimer = ticks;
        return this;
    }

    @Override
    public boolean canPlaySound() {
        return !getEntity().isSilent();
    }
    
    public T getEntity() {
        return entity;
    }
    
    @Override
    public void tick() {
        T entity = getEntity();
        if (isFadingOut) {
            if (--fadeOutTimer <= 0) {
                stop();
            }
            else {
                volume -= volumeReduction;
            }
        }
        else if (!(entity.isAlive() && playWhile.test(entity))) {
            fadeOut();
        }
        else {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
        }
    }
    
    private void fadeOut() {
        if (fadeOutTimer > 0) {
            isFadingOut = true;
            volumeReduction = volume / fadeOutTimer;
        }
        else {
            stop();
        }
    }
}
