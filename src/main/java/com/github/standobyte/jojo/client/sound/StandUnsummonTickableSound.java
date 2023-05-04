package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;

import net.minecraft.client.audio.EntityTickableSound;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class StandUnsummonTickableSound extends EntityTickableSound {
    private StandEntity stand;
    
    public StandUnsummonTickableSound(SoundEvent sound, SoundCategory category, 
            float volume, float pitch, LivingEntity standUser, StandEntity stand) {
        super(sound, category, volume, pitch, standUser);
        this.stand = stand;
    }

    @Override
    public void tick() {
        if (stand != null && !stand.isAlive()) {
            stand = null;
        }
        if (stand != null && stand.getCurrentTaskAction() != ModStandsInit.UNSUMMON_STAND_ENTITY.get()) {
            stop();
        }
        else {
            super.tick();
        }
    }
}
