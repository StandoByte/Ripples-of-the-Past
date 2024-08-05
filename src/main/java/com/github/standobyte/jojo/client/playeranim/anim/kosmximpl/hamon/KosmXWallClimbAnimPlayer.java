package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.playeranim.kosmx.anim.KosmXKeyframeAnimPlayer;

import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;

public class KosmXWallClimbAnimPlayer extends KosmXKeyframeAnimPlayer {
    protected final Map<ClimbDir, KeyframeAnimation> climbAnim;
    protected final Map<ClimbDir, Map<String, BodyPartTransform>> climbBodyParts;
    protected ClimbDir curAnimUsed;
    
    public KosmXWallClimbAnimPlayer(
            @Nonnull KeyframeAnimation upAnim,
            @Nonnull KeyframeAnimation downAnim,
            @Nonnull KeyframeAnimation leftAnim,
            @Nonnull KeyframeAnimation rightAnim) {
        super(upAnim);
        Objects.requireNonNull(upAnim);
        Objects.requireNonNull(downAnim);
        Objects.requireNonNull(leftAnim);
        Objects.requireNonNull(rightAnim);
        
        climbAnim = new EnumMap<>(ClimbDir.class);
        climbAnim.put(ClimbDir.UP, upAnim);
        climbAnim.put(ClimbDir.DOWN, downAnim);
        climbAnim.put(ClimbDir.LEFT, leftAnim);
        climbAnim.put(ClimbDir.RIGHT, rightAnim);

        climbBodyParts = new EnumMap<>(ClimbDir.class);
        for (Map.Entry<ClimbDir, KeyframeAnimation> anim : climbAnim.entrySet()) {
            KeyframeAnimation emote = anim.getValue();
            Map<String, BodyPartTransform> bodyParts = new HashMap<>(emote.getBodyParts().size());
            for (Map.Entry<String, KeyframeAnimation.StateCollection> part : emote.getBodyParts().entrySet()) {
                bodyParts.put(part.getKey(), new BodyPartTransform(part.getValue()));
            }
            climbBodyParts.put(anim.getKey(), bodyParts);
        }
        
        setClimbDirection(ClimbDir.UP);
    }

    
//    @Override
//    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
//        if (curAnimUsed == null) return value0;
//        Map<String, BodyPartTransform> bodyParts = climbBodyParts.get(curAnimUsed);
//        BodyPartTransform part = bodyParts.get(modelName);
//        if (part == null) return value0;
//        
//        KeyframeAnimation keyframes = climbAnim.get(curAnimUsed);
//        return part.get3DTransform(type, currentTick, tickDelta, value0, keyframes, isLoopStarted);
//    }
    
    public void setClimbDirection(ClimbDir direction) {
        this.curAnimUsed = direction;
        this.data = climbAnim.get(direction);
        this.bodyParts = climbBodyParts.get(direction);
    }
    
//    @Override
//    public void tick() {
//        if (isActive()) {
//            currentTick++;
//            if (isInfinite() && getCurrentTick() > getEndTick()) {
//                currentTick = getReturnToTick();
//                isLoopStarted = true;
//            }
//            if (currentTick >= getStopTick()) {
//                stop();
//            }
//        }
//    }
//    
//    
//    protected int getCurrentTick() {
//        return currentTick;
//    }
//    
//    protected int getStopTick() {
//        return data.stopTick;
//    }
//
//    protected boolean isInfinite() {
//        return data.isInfinite;
//    }
//    
//    protected int getEndTick() {
//        return data.endTick;
//    }
//    
//    protected int getReturnToTick() {
//        return data.returnToTick;
//    }
    
    
    enum ClimbDir {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    
    
    private SpeedModifier speedModifier;
    
    private int lastHandTouchTick = 0;
    private boolean leftHandTouch = false;
    private boolean rightHandTouch = false;
    
    private boolean isPlayerMoving;
    private boolean setStoppedMovingTick = false;
    private int stoppedMovingTick;
    private boolean stoppedAnim = false;
    
    void onInit(PlayerEntity player, ModifierLayer<?> animLayer, SpeedModifier speedModifier) {
        this.speedModifier = speedModifier;
        leftHandTouch = false;
        rightHandTouch = false;
        isPlayerMoving = false;
        setStoppedMovingTick = false;
        stoppedAnim = false;
        speedModifier.speed = 0.25f;
    }
    
    void tickProperties(boolean isMoving, double movementUp, double movementLeft, float speed) {
        KosmXWallClimbAnimPlayer.ClimbDir direction = null;
        if (movementUp > 1E-7) {
            direction = KosmXWallClimbAnimPlayer.ClimbDir.UP;
        }
        else if (movementUp < -1E-7) {
            direction = KosmXWallClimbAnimPlayer.ClimbDir.DOWN;
        }
        else if (movementLeft > 1E-7) {
            direction = KosmXWallClimbAnimPlayer.ClimbDir.LEFT;
        }
        else if (movementLeft < -1E-7) {
            direction = KosmXWallClimbAnimPlayer.ClimbDir.RIGHT;
        }
        else {
            isMoving = false;
        }
        
        if (isMoving) {
            setClimbDirection(direction);
            speedModifier.speed = Math.min(speed, 2.5f);
            
            stoppedAnim = false;
            setStoppedMovingTick = false;
        }
        this.isPlayerMoving = isMoving;
    }
    
    void onRender() {
        if (getAnimTick() == 0 || !isPlayerMoving && !stoppedAnim) {
        	int tick = getAnimTick() % 24;
            if (!setStoppedMovingTick) {
                stoppedMovingTick = getAnimTick() > 0 ? tick : -1;
                setStoppedMovingTick = true;
            }
            if (stoppedMovingTick == -1 && tick >= 1 || !(stoppedMovingTick < 12 ^ tick >= 12)) {
                speedModifier.speed = 0;
                stoppedAnim = true;
            }
        }
    }
    
    @Nullable HandSide handTouchFrame() {
        int tick = (getAnimTick() + 3) % 24;
        int lastTick = this.lastHandTouchTick;
        this.lastHandTouchTick = tick;
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
    
    private int getAnimTick() {
        return getTick();
    }
}
