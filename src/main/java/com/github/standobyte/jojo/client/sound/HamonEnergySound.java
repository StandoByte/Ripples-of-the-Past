package com.github.standobyte.jojo.client.sound;

import java.util.Optional;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.LivingEntity;

public class HamonEnergySound extends TickableSound {
    private final LivingEntity entity;
    private final Action<?> action;
    private Optional<INonStandPower> power = Optional.empty();
    private boolean soundStarted = false;
    private boolean stoppedBreath = false;

    public HamonEnergySound(LivingEntity entity, float volume, float pitch, Action<?> action) {
        super(ModSounds.HAMON_CONCENTRATION.get(), entity.getSoundSource());
        this.entity = entity;
        this.volume = volume;
        this.pitch = pitch;
        this.action = action;
    }

    @Override
    public void tick() {
        if (entity.isAlive()) {
            x = entity.getX();
            y = entity.getY();
            z = entity.getZ();
            if (!stoppedBreath && getEntityStand()
                    .map(power -> power.getHeldAction() != action)
                    .orElse(true)) {
                stoppedBreath = true;
            }
            if (stoppedBreath) {
                if (soundStarted) {
                    volume = Math.max(volume - 0.1F, 0);
                    if (volume == 0) stop();
                }
                else {
                    volume = 0;
                    stop();
                }
            }
            else {
                volume = getEntityStand().map(power -> power.getEnergy() / power.getMaxEnergy()).orElse(1F);
            }
            soundStarted = true;
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
