package com.github.standobyte.jojo.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class StandOstSound extends SimpleSound implements ITickableSound {
    private MusicTicker musicManager;
    private int fadeAwayTicks = -1;
    private int fadeAwayInitialTicks = -1;

    public StandOstSound(SoundEvent sound, Minecraft mc) {
        super(sound.getLocation(), SoundCategory.MUSIC, 1.0F, 1.0F, false, 
                0, ISound.AttenuationType.NONE, 0.0D, 0.0D, 0.0D, true);
        this.musicManager = mc.getMusicManager();
        musicManager.stopPlaying();
    }

    @Override
    public boolean isStopped() {
        return volume <= 0;
    }

    @Override
    public void tick() {
        if (!isStopped()) {
            if (fadeAwayInitialTicks > -1) {
                volume = (float) fadeAwayTicks-- / (float) fadeAwayInitialTicks;
            }
        }
    }
    
    public void setFadeAway(int ticks) {
        if (ticks > -1) {
            this.fadeAwayTicks = ticks;
            this.fadeAwayInitialTicks = ticks;
        }
    }
}
