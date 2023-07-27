package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.math.MathHelper;

public class GliderFlightSound extends TickableSound {
    private final LeavesGliderEntity glider;
    private int time;
    
    public GliderFlightSound(LeavesGliderEntity glider) {
        super(
                ModSounds.GLIDER_FLIGHT.get(),  // FIXME !!!!!!!!!!!!!!! is the fucking elytra flight sound in particular bugged? why doesn't the volume decrease with distance??? aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa kill me kill me kill me kill me
                glider.getSoundSource());
        this.glider = glider;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.05F;
    }

    private static final float VOLUME_HIGHER_PITCH = 0.8F;
    public void tick() {
        ++time;
        if (glider.isAlive() && (time <= 20 || glider.isFlying())) {
            x = glider.getX();
            y = glider.getY();
            z = glider.getZ();
            double movementSqr = glider.getDeltaMovement().lengthSqr();
            if (movementSqr >= 1.0E-7D) {
                volume = MathHelper.clamp((float) movementSqr * 1.5F, 0.0F, 1.0F);
            } 
            else {
                volume = 0;
            }

            if (time < 20) {
                volume = 0;
            } 
            else if (time < 40) {
                volume *= (float) (time - 20) / 20.0F;
            }

            if (volume > VOLUME_HIGHER_PITCH) {
                pitch = 1.0F + (volume - VOLUME_HIGHER_PITCH);
            } 
            else {
                pitch = 1.0F;
            }
        } 
        else {
            stop();
        }
    }

}
