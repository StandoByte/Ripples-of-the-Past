package com.github.standobyte.jojo.client.sound;

import net.minecraft.client.audio.EntityTickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class LoopingEntityTickableSound extends EntityTickableSound {

    public LoopingEntityTickableSound(SoundEvent soundEvent, SoundCategory soundCategory, 
            float volume, float pitch, boolean looping, Entity entity) {
        super(soundEvent, soundCategory, volume, pitch, entity);
        this.looping = looping;
    }

}
