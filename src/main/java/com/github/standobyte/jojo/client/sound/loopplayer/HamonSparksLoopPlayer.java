package com.github.standobyte.jojo.client.sound.loopplayer;

import java.util.function.Predicate;

import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

public class HamonSparksLoopPlayer<T extends Entity> extends EntitySoundLoopPlayer<T> {
    
    public HamonSparksLoopPlayer(T entity, Predicate<T> playWhile, float volume, float pitch) {
        super(entity, playWhile, ModSounds.HAMON_SPARK_SHORT.get(), SoundCategory.AMBIENT, volume, pitch);
    }
    
    @Override
    protected int soundDelayTicks() {
        return 9 + RANDOM.nextInt(2);
    }
    
    @Override
    protected Vector3d soundPos() {
        return entity.getBoundingBox().getCenter();
    }
}
