package com.github.standobyte.jojo.client.playeranim.playeranimator.anim.hamon;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Easing;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IBendHelper;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class PlayerBarrageAnim implements IAnimation, IModelPose<AbstractClientPlayerEntity> {
    private final KeyframeAnimation data;
    private boolean isRunning = true;
    private int currentTick;
    private boolean isLoopStarted = false;

    private final Map<String, BodyPart> bodyParts;
    
    private final Map<String, ModelRenderer> modelParts;
    private final Map<String, IBendHelper> sameModelPartsBendable;
    private final PlayerModel<AbstractClientPlayerEntity> model;

    public PlayerBarrageAnim(PlayerModel<AbstractClientPlayerEntity> model) {
        KeyframeAnimation emote = PlayerAnimationRegistry.getAnimation(new ResourceLocation(JojoMod.MOD_ID, "punch_barrage"));
        this.data = emote;
        this.bodyParts = new HashMap<>(emote.getBodyParts().size());
        
        this.model = model;
        this.modelParts = new HashMap<>(emote.getBodyParts().size());
        this.sameModelPartsBendable = new HashMap<>(emote.getBodyParts().size());
        
        for (Map.Entry<String, KeyframeAnimation.StateCollection> part : emote.getBodyParts().entrySet()){
            this.bodyParts.put(part.getKey(), new BodyPart(part.getValue()));
            
            if (model != null) {
                switch (part.getKey()) {
                case "head":
                    addModelPart(part.getKey(), m -> m.head, m -> null);
                    break;
                case "leftArm":
                    addModelPart(part.getKey(), m -> m.leftArm, IMutableModel::getLeftArm);
                    break;
                case "rightArm":
                    addModelPart(part.getKey(), m -> m.rightArm, IMutableModel::getRightArm);
                    break;
                case "leftLeg":
                    addModelPart(part.getKey(), m -> m.leftLeg, IMutableModel::getLeftLeg);
                    break;
                case "rightLeg":
                    addModelPart(part.getKey(), m -> m.rightLeg, IMutableModel::getRightLeg);
                    break;
                }
            }
        }
    }
    
    private void addModelPart(String key, 
            Function<BipedModel<?>, ModelRenderer> part, 
            Function<IMutableModel, IBendHelper> partBendable) {
        modelParts.put(key, part.apply(model));
        if (model instanceof IMutableModel) {
            IBendHelper bendable = partBendable.apply((IMutableModel) model);
            if (bendable != null) {
                sameModelPartsBendable.put(key, bendable);
            }
        }
    }
    
    @Override
    public void tick() {
        if (this.isRunning) {
            this.currentTick++;
            if (data.isInfinite && this.currentTick > data.returnToTick + (data.endTick - data.returnToTick) * 2) {
                this.currentTick = data.returnToTick;
                this.isLoopStarted = true;
            }
        }
    }
    
    private float getPlayerAnimatorLoopTick(float partialTick) {
        boolean backwards = currentTick >= data.endTick;
        return backwards ? data.endTick * 2 - currentTick : currentTick + partialTick;
    }

    @Override
    public boolean isActive() {
        return this.isRunning;
    }
    
    public void rotateBody(MatrixStack matrixStack, float rotationAmount, HandSide side) {
        float loopTick = getBarrageEffectLoopingTick(rotationAmount, side);
        int tick = (int) loopTick;
        float partialTick = loopTick - tick;
        Vec3f rot = get3DTransform("body", TransformType.ROTATION, tick, partialTick, Vec3f.ZERO);
        matrixStack.mulPose(Vector3f.YP.rotation(-rot.getY()));
    }
    
    private float getBarrageEffectLoopingTick(float rotAmount, HandSide side) {
        if (side == HandSide.RIGHT) {
            rotAmount = 1 - rotAmount;
        }
        return data.returnToTick + (data.endTick - data.returnToTick) * rotAmount;
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float partialTick, Vec3f value0) {
        float tick = getPlayerAnimatorLoopTick(partialTick);
        return get3DTransform(modelName, type, (int) tick, tick - (int) tick, value0);
    }
    
    private Vec3f get3DTransform(String modelName, TransformType type, int tick, float partialTick, Vec3f value0) {
        Vec3f vec;
        BodyPart part = bodyParts.get(modelName);
        if (part == null) return value0;
        switch (type) {
            case POSITION:
                vec = part.getBodyOffset(value0, tick, partialTick);
                return vec;
            case ROTATION:
                vec = part.getBodyRotation(value0, tick, partialTick);
                return vec;
            case BEND:
                Pair<Float, Float> bend = part.getBend(new Pair<>(value0.getX(), value0.getY()), tick, partialTick);
                vec = new Vec3f(bend.getLeft(), bend.getRight(), 0f);
                return vec;
            default:
                return value0;
        }
    }

    @Override
    public void poseModel(float rotationAmount, AbstractClientPlayerEntity entity, 
            float ticks, float yRotOffsetRad, float xRotRad, HandSide side) {
        if (model != null) {
            float loopTick = getBarrageEffectLoopingTick(rotationAmount, side);
            int tick = (int) loopTick;
            float partialTick = loopTick - tick;
            for (String partName : modelParts.keySet()) {
                ModelRenderer part = modelParts.get(partName);
                if (part != null) {
                    Vec3f rot = get3DTransform(partName, TransformType.ROTATION, tick, partialTick, new Vec3f(part.xRot, part.yRot, part.zRot));
                    
                    float entityXRot = entity.xRot;
                    Vector3f anglesNew = ClientUtil.rotateAngles(rot.getX(), rot.getY(), rot.getZ(), entityXRot * MathUtil.DEG_TO_RAD);
                    rot = new Vec3f(anglesNew.x(), anglesNew.y(), anglesNew.z());
                    
                    part.xRot = rot.getX();
                    part.yRot = rot.getY();
                    part.zRot = rot.getZ();
                    
                    IBendHelper partBendable = sameModelPartsBendable.get(partName);
                    if (partBendable != null) {
                        Vec3f bend = get3DTransform(partName, TransformType.BEND, tick, partialTick, Vec3f.ZERO);
                        partBendable.bend(new Pair<>(bend.getX(), bend.getY()));
                    }
                }
            }
            model.leftPants.copyFrom(model.leftLeg);
            model.rightPants.copyFrom(model.rightLeg);
            model.leftSleeve.copyFrom(model.leftArm);
            model.rightSleeve.copyFrom(model.rightArm);
            model.jacket.copyFrom(model.body);
        }
    }

    @Override
    public void setupAnim(float partialTick) {
    }

    public boolean isLoopStarted() {
        return isLoopStarted;
    }


    public KeyframeAnimation getData() {
        return data;
    }

    public BodyPart getPart(String string){
        BodyPart part = bodyParts.get(string);
        return part == null ? new BodyPart(null) : part;
    }
    
    public boolean isInfinite() {
        return data.isInfinite;
    }


    private class BodyPart {
        @Nullable
        private final KeyframeAnimation.StateCollection part;
        private final Axis x;
        private final Axis y;
        private final Axis z;
        private final RotationAxis pitch;
        private final RotationAxis yaw;
        private final RotationAxis roll;
        private final RotationAxis bendAxis;
        private final RotationAxis bend;


        private BodyPart(@Nullable KeyframeAnimation.StateCollection part) {
            this.part = part;
            if(part != null) {
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
                this.x = null;
                this.y = null;
                this.z = null;
                this.pitch = null;
                this.yaw = null;
                this.roll = null;
                this.bendAxis = null;
                this.bend = null;
            }
        }


        private Pair<Float, Float> getBend(Pair<Float, Float> value0, int currentTick, float partialTick) {
            if(bend == null) return value0;
            return new Pair<>(
                    this.bendAxis.getValueAtCurrentTick(value0.getLeft(), currentTick, partialTick), 
                    this.bend.getValueAtCurrentTick(value0.getRight(), currentTick, partialTick));
        }

        private Vec3f getBodyOffset(Vec3f value0, int currentTick, float partialTick) {
            if(this.part == null) return value0;
            float x = this.x.getValueAtCurrentTick(value0.getX(), currentTick, partialTick);
            float y = this.y.getValueAtCurrentTick(value0.getY(), currentTick, partialTick);
            float z = this.z.getValueAtCurrentTick(value0.getZ(), currentTick, partialTick);
            return new Vec3f(x, y, z);
        }

        private Vec3f getBodyRotation(Vec3f value0, int currentTick, float partialTick) {
            if(this.part == null) return value0;
            return new Vec3f(
                    this.pitch.getValueAtCurrentTick(value0.getX(), currentTick, partialTick),
                    this.yaw.getValueAtCurrentTick(value0.getY(), currentTick, partialTick),
                    this.roll.getValueAtCurrentTick(value0.getZ(), currentTick, partialTick)
            );
        }

    }

    private class Axis {
        protected final KeyframeAnimation.StateCollection.State keyframes;


        private Axis(KeyframeAnimation.StateCollection.State keyframes) {
            this.keyframes = keyframes;
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
        private KeyframeAnimation.KeyFrame findBefore(int pos, float currentState, int currentTick) {
            if (pos == -1) {
                return (currentTick < data.beginTick) ?
                        new KeyframeAnimation.KeyFrame(0, currentState) :
                        (currentTick < data.endTick) ?
                                new KeyframeAnimation.KeyFrame(data.beginTick, keyframes.defaultValue) :
                                new KeyframeAnimation.KeyFrame(data.endTick, keyframes.defaultValue);
            }
            KeyframeAnimation.KeyFrame frame = this.keyframes.getKeyFrames().get(pos);
            if (!isInfinite() && currentTick >= getData().endTick && pos == keyframes.length() - 1 && frame.tick < getData().endTick) {
                return new KeyframeAnimation.KeyFrame(getData().endTick, frame.value, frame.ease);
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
        private KeyframeAnimation.KeyFrame findAfter(int pos, float currentState, int currentTick) {
            if (this.keyframes.length() > pos + 1) {
                return this.keyframes.getKeyFrames().get(pos + 1);
            }

            if (isInfinite()) {
                return new KeyframeAnimation.KeyFrame(getData().endTick + 1, keyframes.defaultValue);
            }

            if (currentTick < getData().endTick && this.keyframes.length() > 0) {
                KeyframeAnimation.KeyFrame lastFrame = this.keyframes.getKeyFrames().get(this.keyframes.length() - 1);
                return new KeyframeAnimation.KeyFrame(getData().endTick, lastFrame.value, lastFrame.ease);
            }

            return currentTick >= data.endTick ?
                    new KeyframeAnimation.KeyFrame(data.stopTick, currentState) :
                    currentTick >= getData().beginTick ?
                            new KeyframeAnimation.KeyFrame(getData().endTick, keyframes.defaultValue) :
                            new KeyframeAnimation.KeyFrame(getData().beginTick, keyframes.defaultValue);
        }


        /**
         * Get the current value of this axis.
         *
         * @param currentValue the Current value of the axis
         * @return value
         */
        protected float getValueAtCurrentTick(float currentValue, int currentTick, float partialTick) {
            if(keyframes != null && keyframes.isEnabled()) {
                int pos = keyframes.findAtTick(currentTick);
                KeyframeAnimation.KeyFrame keyBefore = findBefore(pos, currentValue, currentTick);
                if (isLoopStarted && keyBefore.tick < data.returnToTick) {
                    keyBefore = findBefore(keyframes.findAtTick(data.endTick), currentValue, currentTick);
                }
                KeyframeAnimation.KeyFrame keyAfter = findAfter(pos, currentValue, currentTick);
                if (data.isInfinite && keyAfter.tick > data.endTick) { //If we found nothing, try finding something from the beginning
                    keyAfter = findAfter(keyframes.findAtTick(data.returnToTick - 1), currentValue, currentTick);
                }
                return getValueFromKeyframes(keyBefore, keyAfter, currentTick, partialTick);
            }
            return currentValue;
        }

        /**
         * Calculate the current value between keyframes
         *
         * @param before Keyframe before
         * @param after  Keyframe after
         * @return value
         */
        private final float getValueFromKeyframes(KeyframeAnimation.KeyFrame before, KeyframeAnimation.KeyFrame after, int currentTick, float partialTick) {
            int tickBefore = before.tick;
            int tickAfter = after.tick;
            if (tickBefore >= tickAfter) {
                if (currentTick < tickBefore) tickBefore -= data.endTick - data.returnToTick + 1;
                else tickAfter += data.endTick - data.returnToTick + 1;
            }
            if (tickBefore == tickAfter) return before.value;
            float f = (currentTick + partialTick - (float) tickBefore) / (tickAfter - tickBefore);
            return MathHelper.lerp(Easing.easingFromEnum(data.isEasingBefore ? after.ease : before.ease, f), before.value, after.value);
        }

    }

    private class RotationAxis extends Axis {

        private RotationAxis(KeyframeAnimation.StateCollection.State keyframes) {
            super(keyframes);
        }

        @Override
        protected float getValueAtCurrentTick(float currentValue, int currentTick, float partialTick) {
            return MathHelper.clampToRadian(super.getValueAtCurrentTick(MathHelper.clampToRadian(currentValue), currentTick, partialTick));
        }
    }
    
    
    
    @Override
    public IModelPose<AbstractClientPlayerEntity> setEasing(UnaryOperator<Float> function) {
        return this;
    }
}
