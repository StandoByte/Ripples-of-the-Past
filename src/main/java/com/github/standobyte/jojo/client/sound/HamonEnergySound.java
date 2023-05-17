package com.github.standobyte.jojo.client.sound;

import java.util.Optional;

import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.LivingEntity;

public class HamonEnergySound extends TickableSound {
    private final LivingEntity entity;
    private Optional<INonStandPower> power = Optional.empty();
    private boolean stoppedBreath = false;

    public HamonEnergySound(LivingEntity entity, float volume, float pitch) {
        super(ModSounds.HAMON_CONCENTRATION.get(), entity.getSoundSource());
        this.entity = entity;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void tick() {
        if (entity.isAlive()) {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
            if (!stoppedBreath && getEntityStand()
                    .map(power -> power.getHeldAction() != ModHamonActions.HAMON_BREATH.get())
                    .orElse(true)) {
                stoppedBreath = true;
            }
            if (stoppedBreath) {
                volume = Math.max(volume - 0.1F, 0);
                if (volume == 0) stop();
            }
            else {
                volume = getEntityStand().map(power -> power.getEnergy() / power.getMaxEnergy()).orElse(1F);
            }
        }
        else {
            stop();
        }
    }
    
    private Optional<INonStandPower> getEntityStand() {
        if (!power.isPresent()) {
            power = INonStandPower.getNonStandPowerOptional(entity).resolve();
        }
        return power;
    }
}
