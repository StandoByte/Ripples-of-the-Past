package com.github.standobyte.jojo.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;

public class ResolvedLocationTickingSound extends ResolvedLocationSimpleSound implements ITickableSound {
    private boolean stopped;

    public ResolvedLocationTickingSound(Sound sound, SoundCategory source, ITextComponent subtitle) {
        super(sound, source, subtitle);
    }

    public ResolvedLocationTickingSound(Sound sound, SoundCategory source) {
        super(sound, source);
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void tick() {}
    
    public void stop() {
        stopped = true;
        looping = false;
    }

}
