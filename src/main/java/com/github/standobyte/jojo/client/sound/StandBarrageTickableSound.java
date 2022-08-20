package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundEvent;

public class StandBarrageTickableSound extends TickableSound {
    private final StandEntity stand;

    public StandBarrageTickableSound(StandEntity stand, SoundEvent sound) {
        super(sound, stand.getSoundSource());
        this.stand = stand;
        this.looping = true;
        this.delay = 0;
        this.volume = 0;
        this.x = stand.getX();
        this.y = stand.getY();
        this.z = stand.getZ();
    }

    @Override
    public boolean canPlaySound() {
        return !stand.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (!stand.isAlive()) {
            stop();
        }
        else {
            BarrageSoundsHandler sounds = stand.getBarrageSoundsHandler();
            if (sounds.getCurrentSound() == this) {
                JojoModUtil.ifPresentOrElse(sounds.getCurrentSoundPos(), pos -> {
                    x = pos.x;
                    y = pos.y;
                    z = pos.z;
                    volume = 1;
                }, () -> {
                    volume = 0;
                });
            }
            else {
                volume = 0;
            }
        }
    }

}
