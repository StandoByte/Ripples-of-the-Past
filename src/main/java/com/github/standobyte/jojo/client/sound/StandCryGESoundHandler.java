package com.github.standobyte.jojo.client.sound;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class StandCryGESoundHandler<T extends Entity> extends StandCrySoundHandler<T> {

    protected StandCryGESoundHandler(SoundCategory category, float volume, float pitch, boolean looping, 
            T entity, Predicate<T> playWhile, SoundsResolved soundsResolved, SoundEvent[] sounds) {
        super(category, volume, pitch, looping, entity, playWhile, soundsResolved, ArrayUtils.add(sounds, ModSounds.GOLD_EXPERIENCE_WRY.get()));
    }
    
    @Override
    protected void resolve() {
        super.resolve();
        if (sound != SoundHandler.EMPTY_SOUND) {
            soundsInfo.allSounds = soundsInfo.soundsPerEvent.entrySet().stream()
                    .filter(entry -> entry.getKey() != ModSounds.GOLD_EXPERIENCE_WRY.get())
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(Collectors.toList());
            soundsPlayed = 0;
            super.resolve();
        }
    }
    
    @Override
    protected Sound pickNextSound(List<Sound> pickFrom) {
        if (soundsPlayed % 7 == 4) {
            List<Sound> wrySound = soundsInfo.soundsPerEvent.get(ModSounds.GOLD_EXPERIENCE_WRY.get());
            if (wrySound != null && !wrySound.isEmpty()) {
                return super.pickNextSound(wrySound);
            }
        }
        
        return super.pickNextSound(pickFrom);
    }

}
