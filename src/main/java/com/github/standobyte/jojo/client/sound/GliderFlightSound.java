package com.github.standobyte.jojo.client.sound;

import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.init.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class GliderFlightSound extends TickableSound {
    private final LeavesGliderEntity glider;
    private int time;
    private float trueVolume;
    
    public GliderFlightSound(LeavesGliderEntity glider) {
        super(ModSounds.GLIDER_FLIGHT.get(), SoundCategory.AMBIENT);
        this.glider = glider;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.05F;
    }
    
    private static final float VOLUME_HIGHER_PITCH = 0.8F;
    public void tick() {
        ++time;
        if (glider.isAlive() && (time <= 20 || glider.isFlying())) {
            volume = trueVolume;
            
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
            
            trueVolume = volume;
            manualAttenuation();
        } 
        else {
            stop();
        }
    }
    
    // looping sounds only change position when the sound plays over, the elytra loop sound is too long for that
    private void manualAttenuation() {
        if (attenuation == ISound.AttenuationType.LINEAR) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player.getVehicle() != glider) {
                Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();
                Vector3d vecTo = new Vector3d(x, y, z).subtract(cameraPos);
                double maxDist = getSound().getAttenuationDistance();
                volume *= Math.max(1 - vecTo.length() / maxDist, 0);
            }
        }
    }

}
