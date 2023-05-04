package com.github.standobyte.jojo.capability.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;

public class ClientPlayerUtilCap {
    @SuppressWarnings("unused")
    private final AbstractClientPlayerEntity player;
    private final SoundHandler soundManager;
    private ISound currentVoiceLine;
    public boolean lastVoiceLineTriggered;
    
    public boolean isWalkingOnLiquid;
    
    public ClientPlayerUtilCap(AbstractClientPlayerEntity player) {
        this.player = player;
        this.soundManager = Minecraft.getInstance().getSoundManager();
    }
    
    public boolean isVoiceLinePlaying() {
        if (!soundManager.isActive(currentVoiceLine)) {
            currentVoiceLine = null;
            return false;
        }
        return currentVoiceLine != null;
    }
    
    public void setCurrentVoiceLine(ISound sound) {
        this.currentVoiceLine = sound;
    }

}
