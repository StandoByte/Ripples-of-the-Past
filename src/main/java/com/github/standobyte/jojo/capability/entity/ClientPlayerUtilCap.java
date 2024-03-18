package com.github.standobyte.jojo.capability.entity;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.EnergyRippleLayer.HamonEnergyRippleHandler;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.util.general.OptionalFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.EntityType;

public class ClientPlayerUtilCap {
    private final AbstractClientPlayerEntity player;
    private final SoundHandler soundManager;
    private ISound currentVoiceLine;
    public boolean lastVoiceLineTriggered;
    
    private final BarrageSwingsHolder<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> barrageSwings;
    private boolean isBarraging;
    private final HamonEnergyRippleHandler hamonSparkWaves;
    
    @Nullable private EntityType<?> vehicleType;
    
    private OptionalFloat lockedYRot = OptionalFloat.empty();
    private OptionalFloat lockedXRot = OptionalFloat.empty();
    
    private Action<?> heldWithAnim;
    
    public ClientPlayerUtilCap(AbstractClientPlayerEntity player) {
        this.player = player;
        this.soundManager = Minecraft.getInstance().getSoundManager();
        this.barrageSwings = new BarrageSwingsHolder<>();
        this.hamonSparkWaves = new HamonEnergyRippleHandler(player);
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
    
    public HamonEnergyRippleHandler getHamonSparkWaves() {
        return hamonSparkWaves;
    }
    
    
    public void setHeldActionWithAnim(Action<?> action) {
        this.heldWithAnim = action;
    }
    
    public Optional<Action<?>> getHeldActionWithAnim() {
        return Optional.ofNullable(heldWithAnim);
    }
    
    
    public void setVehicleType(@Nullable EntityType<?> vehicleType) {
        this.vehicleType = vehicleType;
        PlayerAnimationHandler.getPlayerAnimator().onVehicleMount(player, vehicleType);
    }
    
    
    public void lockYRot() {
        lockedYRot = OptionalFloat.of(player.yRot);
    }
    
    public void clearLockedYRot() {
        lockedYRot = OptionalFloat.empty();
    }
    
    public OptionalFloat getLockedYRot() {
        return lockedYRot;
    }
    
    public void lockXRot() {
        lockedXRot = OptionalFloat.of(player.xRot);
    }
    
    public void clearLockedXRot() {
        lockedXRot = OptionalFloat.empty();
    }
    
    public OptionalFloat getLockedXRot() {
        return lockedXRot;
    }
    
    public void applyLockedRotation() {
        lockedYRot.ifPresent(yRot -> {
            player.yRot = yRot;
            player.yRotO = yRot;
        });
        lockedXRot.ifPresent(xRot -> {
            player.xRot = xRot;
            player.xRotO = xRot;
        });
    }
}
