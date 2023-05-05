package com.github.standobyte.jojo.capability.entity;

import java.util.Optional;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;

public class ClientPlayerUtilCap {
    @SuppressWarnings("unused")
    private final AbstractClientPlayerEntity player;
    private final SoundHandler soundManager;
    private ISound currentVoiceLine;
    public boolean lastVoiceLineTriggered;
    
    public boolean isWalkingOnLiquid;
    
    private final BarrageSwingsHolder<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> barrageSwings;
    private boolean isBarraging;
    
    private Action<?> heldWithAnim;
    
    public ClientPlayerUtilCap(AbstractClientPlayerEntity player) {
        this.player = player;
        this.soundManager = Minecraft.getInstance().getSoundManager();
        this.barrageSwings = new BarrageSwingsHolder<>();
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
    
    
    public BarrageSwingsHolder<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> getBarrageSwings() {
        return barrageSwings;
    }
    
    public boolean isBarraging() {
        return isBarraging;
    }
    
    public void setIsBarraging(boolean isBarraging) {
        this.isBarraging = isBarraging;
    }

    
    public void setHeldActionWithAnim(Action<?> action) {
        this.heldWithAnim = action;
    }
    
    public Optional<Action<?>> getHeldActionWithAnim() {
        return Optional.ofNullable(heldWithAnim);
    }

}
