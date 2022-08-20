package com.github.standobyte.jojo.client.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

public class BarrageSoundsHandler {
    private final StandEntity stand;
    private final Map<SoundEvent, StandBarrageTickableSound> sounds = new HashMap<>();
    @Nullable
    private StandBarrageTickableSound currentSound;
    private Optional<Vector3d> currentSoundPos = Optional.empty();
    
    public BarrageSoundsHandler(StandEntity stand) {
        this.stand = stand;
    }
    
    public void setSound(@Nonnull SoundEvent sound) {
        currentSound = sounds.computeIfAbsent(sound, soundEvent -> {
            StandBarrageTickableSound soundInstance = new StandBarrageTickableSound(stand, soundEvent);
            Minecraft.getInstance().getSoundManager().play(soundInstance);
            return soundInstance;
        });
    }
    
    public void stopSound() {
        currentSound = null;
    }
    
    public void setCurrentSoundPos(Vector3d pos) {
        currentSoundPos = Optional.ofNullable(pos);
    }
    
    @Nullable
    public StandBarrageTickableSound getCurrentSound() {
        return currentSound;
    }
    
    public Optional<Vector3d> getCurrentSoundPos() {
        return currentSoundPos;
    }

}
