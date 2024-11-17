package com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.playeranim.anim.kosmximpl.hamon.KosmXWallClimbHandler.PerPlayerModifiersLayer;
import com.github.standobyte.jojo.client.playeranim.kosmx.anim.KosmXKeyframeAnimPlayer;

import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public class KosmXWallClimbKeyframePlayer extends KosmXKeyframeAnimPlayer {
    protected final Map<ClimbDir, KeyframeAnimation> climbAnim;
    protected final Map<ClimbDir, Map<String, BodyPartTransform>> climbBodyParts;
    protected ClimbDir curAnimUsed;
    @Nullable protected ClimbDir nextAnimToSet;
    
    public KosmXWallClimbKeyframePlayer(
            @Nonnull KeyframeAnimation upAnim,
            @Nonnull KeyframeAnimation downAnim,
            @Nonnull KeyframeAnimation leftAnim,
            @Nonnull KeyframeAnimation rightAnim,
            PerPlayerModifiersLayer<?> modifiers) {
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
        
        init(modifiers.speed, modifiers.mirror);
        
        doSetClimbDirection(ClimbDir.UP);
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
        if (stoppedAnim) {
            doSetClimbDirection(direction);
        }
        else {
            this.nextAnimToSet = direction;
        }
    }
    
    private void doSetClimbDirection(ClimbDir direction) {
        if (this.curAnimUsed != direction) {
            ClimbDir prevAnim = this.curAnimUsed;
            this.curAnimUsed = direction;
            this.data = climbAnim.get(direction);
            this.bodyParts = climbBodyParts.get(direction);
            
            if (prevAnim != null && curAnimUsed != null) {
                if (!prevAnim.isHorizontal && curAnimUsed.isHorizontal) {
                    mirrorModifier.setEnabled(currentTick <= 12);
                    currentTick %= data.endTick - data.returnToTick;
                }
                else if (prevAnim.isHorizontal && !curAnimUsed.isHorizontal) {
                    currentTick = mirrorModifier.isEnabled() ? 13 : 1;
                    mirrorModifier.setEnabled(false);
                }
            }
        }
    }
    
    
    enum ClimbDir {
        UP(false),
        DOWN(false),
        LEFT(true),
        RIGHT(true);
        
        private final boolean isHorizontal;
        
        private ClimbDir(boolean isHorizontal) {
            this.isHorizontal = isHorizontal;
        }
    }
    
    
    private SpeedModifier speedModifier;
    private MirrorModifier mirrorModifier;
    
    private int lastHandTouchTick = 0;
    private boolean leftHandTouch = false;
    private boolean rightHandTouch = false;
    
    private boolean isPlayerMoving;
    private boolean consecutiveMotion = true;
    private int lastTickBeforeMotionChange;
    private boolean stoppedAnim = false;
    
    private void init(SpeedModifier speedModifier, MirrorModifier mirrorModifier) {
        leftHandTouch = false;
        rightHandTouch = false;
        isPlayerMoving = false;
        consecutiveMotion = true;
        stoppedAnim = false;
        
        this.speedModifier = speedModifier;
        speedModifier.speed = 0.25f;
        this.mirrorModifier = mirrorModifier;
        mirrorModifier.setEnabled(false);
    }
    
    void tickProperties(boolean isMoving, double movementUp, double movementLeft, float speed) {
        KosmXWallClimbKeyframePlayer.ClimbDir direction = null;
        if (movementUp > 1E-7) {
            direction = KosmXWallClimbKeyframePlayer.ClimbDir.UP;
        }
        else if (movementUp < -1E-7) {
            direction = KosmXWallClimbKeyframePlayer.ClimbDir.DOWN;
        }
        else if (movementLeft > 1E-7) {
            direction = KosmXWallClimbKeyframePlayer.ClimbDir.LEFT;
        }
        else if (movementLeft < -1E-7) {
            direction = KosmXWallClimbKeyframePlayer.ClimbDir.RIGHT;
        }
        else {
            isMoving = false;
        }
        
        if (isMoving) {
            setClimbDirection(direction);
            speedModifier.speed = MathHelper.clamp(speed, 1, Math.abs(movementUp) > 1E-7 ? 2.5f : 1.25f);
            
            if (stoppedAnim) {
                consecutiveMotion = true;
            }
            stoppedAnim = false;
        }
        this.isPlayerMoving = isMoving;
    }
    
    void onRender() {
        boolean shouldStopAnim = getAnimTick() == 0 || !isPlayerMoving && !stoppedAnim;
        if (nextAnimToSet != null || shouldStopAnim) {
            int tick = getAnimTick() % 24;
            if (consecutiveMotion) {
                lastTickBeforeMotionChange = getAnimTick() > 0 ? tick : -1;
                consecutiveMotion = false;
            }
            if (lastTickBeforeMotionChange == -1 && tick >= 1 || !(lastTickBeforeMotionChange < 12 ^ tick >= 12)) {
                if (shouldStopAnim) {
                    speedModifier.speed = 0;
                    stoppedAnim = true;
                }
                else if (nextAnimToSet != null) {
                    doSetClimbDirection(nextAnimToSet);
                    nextAnimToSet = null;
                    consecutiveMotion = true;
                }
            }
        }
    }
    
    @Nullable HandSide handTouchFrame() {
        if (curAnimUsed != null && !curAnimUsed.isHorizontal) {
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
        }
        
        return null;
    }
    
    private int getAnimTick() {
        return getTick();
    }
}
