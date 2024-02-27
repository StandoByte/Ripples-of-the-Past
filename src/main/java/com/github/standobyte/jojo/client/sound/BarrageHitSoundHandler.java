package com.github.standobyte.jojo.client.sound;

import java.util.Random;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;

public class BarrageHitSoundHandler {
    private Random random = new Random();
    
    private SoundEvent sound;
    private Vector3d soundPos;
    private boolean pauseAfterNext;
    private boolean isBarraging = false;
    
    private double soundTickLast;
    private double nextSoundGap;
    
    public void hit(SoundEvent sound, Vector3d punchPos) {
        this.sound = sound;
        this.soundPos = punchPos;
        this.pauseAfterNext = false;
    }
    
    public void hitMissed() {
        this.pauseAfterNext = true;
    }
    
    public void setIsBarraging(boolean isBarraging) {
        this.isBarraging = isBarraging;
        if (!isBarraging) {
            sound = null;
            soundPos = null;
        }
    }

    public void playSound(StandEntity entity, float ticks) {
        if (isBarraging && sound != null && soundPos != null && soundTickLast + nextSoundGap <= ticks) {
            soundTickLast = ticks;
            nextSoundGap = getSoundGap(entity);
            entity.playSound(sound, 0.5F + random.nextFloat() * 0.25F, 0.85F + random.nextFloat() * 0.3F, ClientUtil.getClientPlayer(), soundPos);
            if (pauseAfterNext) {
                sound = null;
                soundPos = null;
                pauseAfterNext = false;
            }
        }
    }
    
    protected float getSoundGap(StandEntity entity) {
        float mean = 250.0F / (float) Math.max(40, StandStatFormulas
                .getBarrageHitsPerSecond(20 * entity.getStandEfficiency()));
        return (float) random.nextGaussian() + mean;
    }
}
