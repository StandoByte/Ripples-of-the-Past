package com.github.standobyte.jojo.client.sound;

import java.util.function.Predicate;

import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;

public class HamonSparksLoopSound<T extends Entity> extends StoppableEntityTickableSound<T> {

    public HamonSparksLoopSound(T entity, Predicate<T> stopCondition, float volume, float pitch) {
        super(ModSounds.HAMON_SPARKS_LOOP.get(), SoundCategory.AMBIENT, volume, pitch, true, entity, stopCondition);
    }
}
