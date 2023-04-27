package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.client.WalkmanSoundHandler;
import com.github.standobyte.jojo.client.WalkmanSoundHandler.Playlist;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;

public class WalkmanRewindSound extends TickableSound {

    public WalkmanRewindSound() {
        super(ModSounds.WALKMAN_REWIND.get(), SoundCategory.MASTER);
        looping = true;
        x = 0;
        y = 0;
        z = 0;
        attenuation = ISound.AttenuationType.NONE;
        relative = true;
    }

    @Override
    public void tick() {
        Playlist walkmanPlaylist = WalkmanSoundHandler.getCurrentPlaylist();
        if (walkmanPlaylist == null || walkmanPlaylist.getRewindSoundTicks() <= 0) {
            stop();
        }
    }

}
