package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.interfaces.WallClimbAnim;
import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled.AnimLayerHandler;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.modifier.KosmXHeadRotationModifier;
import com.github.standobyte.jojo.client.render.entity.layerrenderer.EnergyRippleLayer;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KosmXWallClimbLayer extends AnimLayerHandler implements WallClimbAnim {
    // FIXME the AnimLayerHandler object is effectively a singleton, 
    //       while ModifierLayer objects are created for every player (so modifiers are too)
    PlayerAnimStuff animStuff;
    
    public KosmXWallClimbLayer(ResourceLocation id) {
        super(id);
    }

    @Override
    protected ModifierLayer<IAnimation> createAnimLayer(AbstractClientPlayerEntity player) {
        ModifierLayer<IAnimation> anim = new ModifierLayer<>(null);
        anim.addModifierLast(new KosmXHeadRotationModifier());
        return anim;
    }
    
    
    private static final ResourceLocation CLIMB_UP = new ResourceLocation("jojo", "wall_climb_up");
    private static final ResourceLocation CLIMB_DOWN = new ResourceLocation("jojo", "wall_climb_down");
    private static final ResourceLocation CLIMB_LEFT = new ResourceLocation("jojo", "wall_climb_left");
    private static final ResourceLocation CLIMB_RIGHT = new ResourceLocation("jojo", "wall_climb_right");
    @Override
    public boolean setAnimEnabled(PlayerEntity player, boolean enabled) {
        PlayerAnimStuff playerAnimProperties = getOrCreateAnimStuff(player);
        if (enabled) {
            KeyframeAnimation keyframes = PlayerAnimationRegistry.getAnimation(CLIMB_UP);
            if (keyframes == null) return false;
            KeyframeAnimationPlayer keyframePlayer = new KeyframeAnimationPlayer(keyframes);
            playerAnimProperties.keyframePlayer = keyframePlayer;
            playerAnimProperties.onWallClimbUpdated(enabled);
            return setAnim(player, keyframePlayer);
        }
        else {
            playerAnimProperties.keyframePlayer = null;
            playerAnimProperties.onWallClimbUpdated(enabled);
            return setAnim(player, null);
        }
    }

    @Override
    public void tickAnimProperties(PlayerEntity player, boolean isMoving, 
            double movementUp, double movementLeft, float speed) {
        getOrCreateAnimStuff(player).tickProperties(isMoving, movementUp, movementLeft, speed);
    }
    
    
    @Override
    public boolean isForgeEventHandler() {
        return true;
    }
    
    @SubscribeEvent
    public void onRender(RenderPlayerEvent.Post event) {
        // FIXME do this logic in 1st person too
        PlayerEntity player = event.getPlayer();
        PlayerAnimStuff animStuff = getAnimStuff(player);
        if (animStuff != null && animStuff.keyframePlayer != null) {
            animStuff.onRender();
            HandSide handTouch = animStuff.handTouchFrame(animStuff.keyframePlayer.getTick());
            if (handTouch != null) {
                Vector3d particlesPos = player.position().add(EnergyRippleLayer.handTipPos(event.getRenderer().getModel(), handTouch, Vector3d.ZERO, player.yBodyRot));
                HamonUtil.emitHamonSparkParticles(player.level, ClientUtil.getClientPlayer(), 
                        particlesPos.x, particlesPos.y, particlesPos.z, 0.25f, 0.5f);
            }
        }
    }

    
    @Nullable
    private PlayerAnimStuff getAnimStuff(PlayerEntity player) {
        return animStuff;
    }
    
    private PlayerAnimStuff getOrCreateAnimStuff(PlayerEntity player) {
        if (animStuff == null) {
            animStuff = new PlayerAnimStuff((AbstractClientPlayerEntity) player);
        }
        return animStuff;
    }
    
    private class PlayerAnimStuff {
        final SpeedModifier speedModifier = new SpeedModifier(1);
        @Nullable KeyframeAnimationPlayer keyframePlayer;
        
        private int lastTick = 0;
        private boolean leftHandTouch = false;
        private boolean rightHandTouch = false;
        
        private boolean isPlayerMoving;
        private boolean setStoppedMovingTick = false;
        private int stoppedMovingTick;
        private boolean stoppedAnim = false;
        
        PlayerAnimStuff(AbstractClientPlayerEntity player) {
            KosmXWallClimbLayer.this.getAnimLayer(player).addModifierLast(speedModifier);
        }
        
        void onWallClimbUpdated(boolean isEnabled) {
            leftHandTouch = false;
            rightHandTouch = false;
            isPlayerMoving = false;
            setStoppedMovingTick = false;
            stoppedAnim = false;
            speedModifier.speed = 1;
        }
        
        void tickProperties(boolean isMoving, double movementUp, double movementLeft, float speed) {
            if (isMoving) {
                if (Math.abs(movementUp) > 1E-7) 
                    speedModifier.speed = speed;{
                }
                
                stoppedAnim = false;
                setStoppedMovingTick = false;
            }
            this.isPlayerMoving = isMoving;
        }
        
        void onRender() {
            if (!isPlayerMoving && !stoppedAnim) {
                int tick = keyframePlayer.getTick() % 24;
                if (!setStoppedMovingTick) {
                    stoppedMovingTick = tick;
                    setStoppedMovingTick = true;
                }
                if (!(stoppedMovingTick < 12 ^ tick >= 12)) {
                    speedModifier.speed = 0;
                    stoppedAnim = true;
                }
            }
        }
        
        @Nullable HandSide handTouchFrame(int tick) {
            tick = (tick + 3) % 24;
            int lastTick = this.lastTick;
            this.lastTick = tick;
            
            if (!rightHandTouch && lastTick < 12 && tick >= 12) {
                leftHandTouch = false;
                rightHandTouch = true;
                return HandSide.RIGHT;
            }
            if (!leftHandTouch && lastTick >= 12 && tick < 12) {
                leftHandTouch = true;
                rightHandTouch = false;
                return HandSide.LEFT;
            }
            
            return null;
        }
    }
    
}
