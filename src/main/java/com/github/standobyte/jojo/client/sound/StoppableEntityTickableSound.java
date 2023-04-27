package com.github.standobyte.jojo.client.sound;

import java.util.function.Predicate;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class StoppableEntityTickableSound<T extends Entity> extends TickableSound {
    protected final T entity;
    protected final Predicate<T> stopCondition;

    // FIXME invert the predicate args (rename stopCondition to playWhile)
    public StoppableEntityTickableSound(SoundEvent sound, SoundCategory category, T entity, 
            Predicate<T> stopCondition) {
        this(sound, category, 1.0F, 1.0F, false, entity, stopCondition);
    }

    public StoppableEntityTickableSound(SoundEvent sound, SoundCategory category, 
            float volume, float pitch, boolean looping, T entity, Predicate<T> stopCondition) {
        super(sound, category);
        this.entity = entity;
        this.stopCondition = stopCondition;
        this.volume = volume;
        this.pitch = pitch;
        this.looping = looping;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
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
        if (!entity.isAlive() || stopCondition.test(entity)) {
            stop();
        } else {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
        }
    }
}
