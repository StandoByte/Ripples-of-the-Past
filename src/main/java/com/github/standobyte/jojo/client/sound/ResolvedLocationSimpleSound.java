package com.github.standobyte.jojo.client.sound;

import javax.annotation.Nullable;

import net.minecraft.client.audio.LocatableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;

public class ResolvedLocationSimpleSound extends LocatableSound {
    protected final ITextComponent subtitle;

    public ResolvedLocationSimpleSound(Sound sound, SoundCategory source) {
        this(sound, source, null);
    }

    public ResolvedLocationSimpleSound(Sound sound, SoundCategory source, @Nullable ITextComponent subtitle) {
        super((sound != null ? sound : SoundHandler.EMPTY_SOUND).getLocation(), source);
        this.sound = sound != null ? sound : SoundHandler.EMPTY_SOUND;
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

}
