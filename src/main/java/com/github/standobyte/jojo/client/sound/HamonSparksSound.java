package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;

public class HamonSparksSound extends TickableSound {
    private final Entity entity;

    public HamonSparksSound(Entity entity, float volume, float pitch) {
        super(ModSounds.HAMON_SPARKS_LONG.get(), SoundCategory.AMBIENT);
        this.volume = volume;
        this.pitch = pitch;
        this.entity = entity;
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
    }

    public void tick() {
        if (entity.isAlive()) {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
        } 
    }
}
