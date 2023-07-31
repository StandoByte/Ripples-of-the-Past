package com.github.standobyte.jojo.capability.entity;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.BarrageSwingsHolder;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
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
    
    private boolean isWalkingOnLiquid;
    private boolean tickSpark;
    
    private final BarrageSwingsHolder<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> barrageSwings;
    private boolean isBarraging;
    
    @Nullable private EntityType<?> vehicleType;
    
    private OptionalFloat lockedYRot = OptionalFloat.empty();
    private OptionalFloat lockedXRot = OptionalFloat.empty();
    
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
    
    
    public void setWaterWalking(boolean waterWalking) {
        if (!this.isWalkingOnLiquid && waterWalking) {
            HamonUtil.emitHamonSparkParticles(player.level, ClientUtil.getClientPlayer(), player.position(), 0.05F);
            CustomParticlesHelper.createHamonSparkParticles(null, player.position(), 10);
            this.tickSpark = false;
        }
        this.isWalkingOnLiquid = waterWalking;
    }
    
    public boolean isWaterWalking() {
        return isWalkingOnLiquid;
    }
    
    public void tickWaterWalking() {
        if (isWalkingOnLiquid && tickSpark) {
            HamonSparksLoopSound.playSparkSound(player, player.position(), 1.0F);
            CustomParticlesHelper.createHamonSparkParticles(player, 
                    player.getRandomX(0.5), player.getY(Math.random() * 0.1), player.getRandomZ(0.5), 
                    1);
        }
        tickSpark = true;
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
