package com.github.standobyte.jojo.client.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class WalkmanTrackSound extends ResolvedLocationTickingSound {
    private int ticks = 0;
    private final int distortionLevel;

    public WalkmanTrackSound(Sound sound, SoundCategory source, ITextComponent subtitle, int distortionLevel) {
        super(sound, source, subtitle);
        this.distortionLevel = distortionLevel;
        switch (distortionLevel) {
        case 1:
            pitch = 0.9875F;
            break;
        case 2:
            pitch = 0.975F;
            break;
        case 3:
            pitch = 0.95F;
            break;
        }
        
        attenuation = ISound.AttenuationType.NONE;
        relative = true;
    }

    @Override
    public void tick() {
        ticks++;
        if (distortionLevel >= 4) {
            // i'm not a sadist
            // , but...
            pitch = 0.8F + (MathHelper.sin((float) ticks * 0.05F) + 1) * 0.05F;
        }
        
//         /*Nightcore remixes be like:*/ pitch = 1.35F;
    }
    
    public void setVolume(float volume) {
        this.volume = volume;
    }
    
    @Override
    public boolean canStartSilent() {
        return true;
    }
}
