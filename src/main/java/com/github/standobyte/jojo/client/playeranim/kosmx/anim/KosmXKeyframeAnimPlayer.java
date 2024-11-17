package com.github.standobyte.jojo.client.playeranim.kosmx.anim;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.github.standobyte.jojo.client.playeranim.kosmx.KosmXPlayerAnimatorInstalled;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation.StateCollection.State;
import dev.kosmx.playerAnim.core.util.Easing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.util.math.MathHelper;

public class KosmXKeyframeAnimPlayer implements IAnimation {
    protected KeyframeAnimation data;
    protected Map<String, BodyPartTransform> bodyParts;
    
    protected boolean isRunning = true;
    protected int currentTick = 0;
    protected boolean isLoopStarted = false;
    protected float tickDelta;
    
    protected ModifierLayer<IAnimation> playerAnim;
    protected AbstractFadeModifier fadeOut;
    protected boolean startedFadeOut = false;
    
    public KosmXKeyframeAnimPlayer(KeyframeAnimation emote, int t, boolean mutable) {
        Objects.requireNonNull(emote);
        this.data = emote;
        
        bodyParts = new HashMap<>(emote.getBodyParts().size());
        for (Map.Entry<String, KeyframeAnimation.StateCollection> part : emote.getBodyParts().entrySet()) {
            bodyParts.put(part.getKey(), new BodyPartTransform(part.getValue()));
        }
        
        this.currentTick = t;
        int returnToTick = getReturnToTick();
        if (isInfinite() && t > returnToTick) {
            currentTick = (t - currentTick) % (getEndTick() - currentTick + 1) + currentTick;
        }
    }
    
    /**
     *
     * @param emote emote to play
     * @param t begin playing from tick
     */
    public KosmXKeyframeAnimPlayer(KeyframeAnimation emote, int t) {
        this(emote, t, false);
    }
    
    public KosmXKeyframeAnimPlayer(KeyframeAnimation animation) {
        this(animation, 0);
    }
    
    public KosmXKeyframeAnimPlayer withFadeOut(AbstractFadeModifier fadeOut, ModifierLayer<IAnimation> playerAnim) {
        this.fadeOut = fadeOut;
        this.playerAnim = playerAnim;
        return this;
    }


    @Override
    public boolean isActive() {
        return this.isRunning;
    }
    
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        BodyPartTransform part = bodyParts.get(modelName);
        if (part == null) return value0;
        
        float tick = overrideTick(tickDelta);
        int tickInt = MathHelper.floor(tick);
        return part.get3DTransform(type, tickInt, tick - tickInt, value0, data, isLoopStarted);
    }
    
    protected float overrideTick(float partialTick) {
        if (startedFadeOut) {
            return getStopTick() - 1;
        }
        return currentTick + partialTick;
    }
    
    @Override
    public void setupAnim(float tickDelta) {
        this.tickDelta = tickDelta;
        KosmXPlayerAnimatorInstalled.eventHandler.removeAttackAnim();
    }
    
    @Override
    public void tick() {
        if (isActive() && !startedFadeOut) {
            currentTick++;
            if (isInfinite() && getTick() > getEndTick()) {
                currentTick = getReturnToTick();
                isLoopStarted = true;
            }
            if (hasFadeOut()) { 
                if (currentTick >= getStopTick() - 1) {
                    startFadeOut();
                }
            }
            else if (currentTick >= getStopTick()) {
                stop();
            }
        }
    }
    
    public void stop() {
        isRunning = false;
    }
    
    public boolean hasFadeOut() {
        return fadeOut != null && playerAnim != null;
    }
    
    public void startFadeOut() {
        if (hasFadeOut()) {
            startedFadeOut = true;
            currentTick = getStopTick();

            fadeOut.setBeginAnimation(this);
            playerAnim.addModifierLast(fadeOut);
            playerAnim.setAnimation(null);
        }
    }
    
    public KeyframeAnimation getKeyframes() {
        return data;
    }
    
    public int getTick() {
        return currentTick;
    }
    
    public int getStopTick() {
        return data.stopTick;
    }

    public boolean isInfinite() {
        return data.isInfinite;
    }
    
    public int getEndTick() {
        return data.endTick;
    }
    
    public int getReturnToTick() {
        return data.returnToTick;
    }
    
    
    
    
    
    public static class BodyPartTransform {
        @Nullable
        public final KeyframeAnimation.StateCollection part;
        public final Axis x;
        public final Axis y;
        public final Axis z;
        public final Axis pitch;
        public final Axis yaw;
        public final Axis roll;
        public final Axis bendAxis;
        public final Axis bend;


        public BodyPartTransform(@Nullable KeyframeAnimation.StateCollection part) {
            this.part = part;
            if (part != null) {
                this.x = new Axis(part.x);
                this.y = new Axis(part.y);
                this.z = new Axis(part.z);
                this.pitch = new RotationAxis(part.pitch);
                this.yaw = new RotationAxis(part.yaw);
                this.roll = new RotationAxis(part.roll);
                this.bendAxis = new RotationAxis(part.bendDirection);
                this.bend = new RotationAxis(part.bend);
            }
            else {
                this.x = DummyAxis.DUMMY;
                this.y = DummyAxis.DUMMY;
                this.z = DummyAxis.DUMMY;
                this.pitch = DummyAxis.DUMMY;
                this.yaw = DummyAxis.DUMMY;
                this.roll = DummyAxis.DUMMY;
                this.bendAxis = DummyAxis.DUMMY;
                this.bend = DummyAxis.DUMMY;
            }
        }
        
        public Vec3f get3DTransform(TransformType type, int currentTick, float tickDelta, Vec3f value0, KeyframeAnimation emote, boolean isLoopStarted) {
            return get3DTransform(type, currentTick, tickDelta, value0, 
                    emote.beginTick, emote.returnToTick, emote.endTick, emote.stopTick, 
                    isLoopStarted, emote.isInfinite, emote.isEasingBefore);
        }
        
        public Vec3f get3DTransform(TransformType type, int currentTick, float tickDelta, Vec3f value0, 
                int beginTick, int returnToTick, int endTick, int stopTick, 
                boolean isLoopStarted, boolean isInfinite, boolean isEasingBefore) {
            switch (type) {
                case POSITION:
                    return new Vec3f(
                            x.getValueAtCurrentTick(value0.getX(), 
                                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                                    stopTick, isLoopStarted, isInfinite, isEasingBefore),
                            y.getValueAtCurrentTick(value0.getY(), 
                                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                                    stopTick, isLoopStarted, isInfinite, isEasingBefore),
                            z.getValueAtCurrentTick(value0.getZ(), 
                                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                                    stopTick, isLoopStarted, isInfinite, isEasingBefore));
                case ROTATION:
                    return new Vec3f(
                            pitch.getValueAtCurrentTick(value0.getX(), 
                                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                                    stopTick, isLoopStarted, isInfinite, isEasingBefore),
                            yaw.getValueAtCurrentTick(value0.getY(), 
                                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                                    stopTick, isLoopStarted, isInfinite, isEasingBefore),
                            roll.getValueAtCurrentTick(value0.getZ(), 
                                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                                    stopTick, isLoopStarted, isInfinite, isEasingBefore));
                case BEND:
                    return new Vec3f(
                            bendAxis.getValueAtCurrentTick(value0.getX(), 
                                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                                    stopTick, isLoopStarted, isInfinite, isEasingBefore),
                            bend.getValueAtCurrentTick(value0.getY(), 
                                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                                    stopTick, isLoopStarted, isInfinite, isEasingBefore),
                            69420);
                default:
                    return value0;
            }
        }

    }
    
    
    
    public static class Axis {
        protected final KeyframeAnimation.StateCollection.State keyframes;

        public Axis(KeyframeAnimation.StateCollection.State keyframes) {
            this.keyframes = keyframes;
        }

        /**
         * Get the current value of this axis.
         *
         * @param currentValue the Current value of the axis
         * @return value
         */
        public float getValueAtCurrentTick(float currentValue, 
                int currentTick, float tickDelta, int beginTick, int returnToTick, int endTick, 
                int stopTick, boolean isLoopStarted, boolean isInfinite, boolean isEasingBefore) {
            if(keyframes != null && keyframes.isEnabled()) {
                int pos = keyframes.findAtTick(currentTick);
                KeyframeAnimation.KeyFrame keyBefore = findBefore(pos, currentValue, 
                        currentTick, beginTick, endTick, isInfinite);
                if (isLoopStarted && keyBefore.tick < returnToTick) {
                    keyBefore = findBefore(keyframes.findAtTick(endTick), currentValue, 
                            currentTick, beginTick, endTick, isInfinite);
                }
                KeyframeAnimation.KeyFrame keyAfter = findAfter(pos, currentValue,
                        currentTick, beginTick, endTick, stopTick, isInfinite);
                if (isInfinite && keyAfter.tick > endTick) { //If we found nothing, try finding something from the beginning
                    keyAfter = findAfter(keyframes.findAtTick(returnToTick - 1), currentValue,
                            currentTick, beginTick, endTick, stopTick, isInfinite);
                }
                return getValueFromKeyframes(keyBefore, keyAfter, currentTick, tickDelta, returnToTick, endTick, isEasingBefore);
            }
            return currentValue;
        }

        /**
         * Find a keyframe before the current tick
         * If none is given: depending on current tick, returns with none/default
         * If given: returns with before:
         * creates a virtual frame at endTick if not looped
         *
         * @param pos          current tick pos, possible candidate
         * @param currentState none state
         * @return Keyframe
         */
        protected KeyframeAnimation.KeyFrame findBefore(int pos, float currentState, 
                int currentTick, int beginTick, int endTick, boolean isInfinite) {
            if (pos == -1) {
                return (currentTick < beginTick) ?
                        new KeyframeAnimation.KeyFrame(0, currentState) :
                        (currentTick < endTick) ?
                                new KeyframeAnimation.KeyFrame(beginTick, keyframes.defaultValue) :
                                new KeyframeAnimation.KeyFrame(endTick, keyframes.defaultValue);
            }
            KeyframeAnimation.KeyFrame frame = this.keyframes.getKeyFrames().get(pos);
            if (isInfinite && currentTick >= endTick && pos == keyframes.length() - 1 && frame.tick < endTick) {
                return new KeyframeAnimation.KeyFrame(endTick, frame.value, frame.ease);
            }
            return frame;
        }

        /**
         * Return with keyframe after current
         * If given, return
         * If infinity and no following, returns with one, AFTER the end
         * if needed, creates a virtual at the end or other
         * @param pos          pos
         * @param currentState none state
         * @return Keyframe
         */
        protected KeyframeAnimation.KeyFrame findAfter(int pos, float currentState, 
                int currentTick, int beginTick, int endTick, int stopTick, boolean isInfinite) {
            if (this.keyframes.length() > pos + 1) {
                return this.keyframes.getKeyFrames().get(pos + 1);
            }

            if (isInfinite) {
                return new KeyframeAnimation.KeyFrame(endTick + 1, keyframes.defaultValue);
            }

            if (currentTick < endTick && this.keyframes.length() > 0) {
                KeyframeAnimation.KeyFrame lastFrame = this.keyframes.getKeyFrames().get(this.keyframes.length() - 1);
                return new KeyframeAnimation.KeyFrame(endTick, lastFrame.value, lastFrame.ease);
            }

            return currentTick >= endTick ?
                    new KeyframeAnimation.KeyFrame(stopTick, currentState) :
                    currentTick >= beginTick ?
                            new KeyframeAnimation.KeyFrame(endTick, keyframes.defaultValue) :
                            new KeyframeAnimation.KeyFrame(beginTick, keyframes.defaultValue);
        }

        /**
         * Calculate the current value between keyframes
         *
         * @param before Keyframe before
         * @param after  Keyframe after
         * @return value
         */
        protected float getValueFromKeyframes(KeyframeAnimation.KeyFrame before, KeyframeAnimation.KeyFrame after, 
                int currentTick, float tickDelta, int returnToTick, int endTick, boolean isEasingBefore) {
            int tickBefore = before.tick;
            int tickAfter = after.tick;
            if (tickBefore >= tickAfter) {
                if (currentTick < tickBefore) tickBefore -= endTick - returnToTick + 1;
                else tickAfter += endTick - returnToTick + 1;
            }
            if (tickBefore == tickAfter) return before.value;
            float f = (currentTick + tickDelta - (float) tickBefore) / (tickAfter - tickBefore);
            return MathHelper.lerp(Easing.easingFromEnum(isEasingBefore ? after.ease : before.ease, f), before.value, after.value);
        }

    }
    
    
    
    public static class RotationAxis extends Axis {

        public RotationAxis(KeyframeAnimation.StateCollection.State keyframes) {
            super(keyframes);
        }

        @Override
        public float getValueAtCurrentTick(float currentValue, 
                int currentTick, float tickDelta, int beginTick, int returnToTick, int endTick, 
                int stopTick, boolean isLoopStarted, boolean isInfinite, boolean isEasingBefore) {
            return dev.kosmx.playerAnim.core.util.MathHelper.clampToRadian(super.getValueAtCurrentTick(dev.kosmx.playerAnim.core.util.MathHelper.clampToRadian(currentValue), 
                    currentTick, tickDelta, beginTick, returnToTick, endTick, 
                    stopTick, isLoopStarted, isInfinite, isEasingBefore));
        }
    }
    
    
    
    public static final class DummyAxis extends Axis {
        public static final DummyAxis DUMMY = new DummyAxis(null);

        private DummyAxis(State keyframes) {
            super(keyframes);
        }

        @Override
        public float getValueAtCurrentTick(float currentValue, 
                int currentTick, float tickDelta, int beginTick, int returnToTick, int endTick, 
                int stopTick, boolean isLoopStarted, boolean isInfinite, boolean isEasingBefore) {
            return currentValue;
        }
        
    }
}
