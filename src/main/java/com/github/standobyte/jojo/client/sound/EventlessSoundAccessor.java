package com.github.standobyte.jojo.client.sound;

import net.minecraft.client.audio.ISoundEventAccessor;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class EventlessSoundAccessor extends SoundEventAccessor {
    private final Sound sound;
    private final ITextComponent subtitle;

    public EventlessSoundAccessor(ResourceLocation location, ITextComponent subtitle, Sound sound) {
        super(location, null);
        this.sound = sound;
        this.subtitle = subtitle;
    }

    @Override
    public int getWeight() {
        return sound.getWeight();
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public void addSound(ISoundEventAccessor<Sound> sound) {}
    
    @Override
    public ITextComponent getSubtitle() {
        return subtitle;
    }

    @Override
    public void preloadIfRequired(SoundEngine soundManager) {
        sound.preloadIfRequired(soundManager);
    }

}
