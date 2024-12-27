package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.render.entity.model.animnew.BarrageSwings;
import com.github.standobyte.jojo.client.render.entity.model.animnew.BarrageSwings.BarrageSwing;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Keyframe;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Transformation.Targets;
import com.github.standobyte.jojo.client.render.entity.model.animnew.molang.AnimContext;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.StandActionAnimation.TimelineKeys;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class GeckoStandAnimator implements IStandAnimator {
    protected final Map<String, List<StandActionAnimation>> namedAnimations = new HashMap<>();
    protected StandActionAnimation idleAnim;
    @Nullable protected StandActionAnimation curAnim;
    protected boolean exists = false;
    
    public GeckoStandAnimator() {}
    
    
    public void setExists() {
        this.exists = true;
    }
    
    public void putNamedAnim(String name, StandActionAnimation anim) {
        name = name.replaceAll("\\d*$", ""); // removes digits at the end
        namedAnimations.computeIfAbsent(name, __ -> new ArrayList<>()).add(anim);
        if (StandPose.IDLE.getName().equals(name)) {
            idleAnim = anim;
        }
    }

//    public StandActionAnimation getNamedAnim(String name) {
//        return namedAnimations.get(name);
//    }
    
    public void onLoad() {
        
    }
    
    @Override
    public <T extends StandEntity> boolean poseStand(@Nullable T entity, StandEntityModel<T> model, StandPoseData poseData, 
            float ticks, float yRotOffsetDeg, float xRotDeg) {
        model.resetPose(entity);
        curAnim = null;
        
        StandPose standPose = poseData.standPose;
        if (standPose == StandPose.SUMMON) {
            List<StandActionAnimation> summonAnims = namedAnimations.get(StandPose.SUMMON.getName());
            if (summonAnims != null && summonAnims.size() > 0) {
                StandActionAnimation summonAnim = StandPose.SUMMON.getAnim(summonAnims, entity);
                
                if (ticks > summonAnim.anim.lengthInSeconds() * 20) {
                    standPose = StandPose.IDLE;
                    model.setStandPose(standPose, entity);
                }
                
                model.idleLoopTickStamp = ticks;
                return applyAnim(summonAnim, entity, model, yRotOffsetDeg, xRotDeg, standPose, poseData);
            }
        }
        
        if (standPose != null && standPose != StandPose.IDLE) {
            model.idleLoopTickStamp = ticks;
            
            List<StandActionAnimation> anims = getAnims(entity, standPose);
            if (anims != null) {
                StandActionAnimation anim = standPose.getAnim(anims, entity);
                if (anim != null) {
                    return applyAnim(anim, entity, model, yRotOffsetDeg, xRotDeg, standPose, poseData);
                }
            }
        }
        
        StandActionAnimation idleAnim = getIdleAnim(entity);
        if (idleAnim != null) {
            return applyAnim(idleAnim, entity, model, yRotOffsetDeg, xRotDeg, standPose, poseData);
        }
        
        return exists;
    }
    
    protected <T extends StandEntity> boolean applyAnim(StandActionAnimation anim, @Nullable T entity, 
            StandEntityModel<T> model, float yRotOffsetDeg, float xRotDeg, StandPose standPose, StandPoseData poseData) {
        curAnim = anim;
        poseData.edit().standPose(standPose);
        poseData.standPose.applyAnim(entity, model, anim, yRotOffsetDeg, xRotDeg, poseData);
        return true;
        
    }
    
    public StandActionAnimation getIdleAnim(@Nullable StandEntity entity) {
        return idleAnim;
    }
    
    protected List<StandActionAnimation> getAnims(@Nullable StandEntity entity, StandPose standPose) {
        String key = standPose.getName();
        if (entity != null && entity.isArmsOnlyMode()) {
            String key2 = "armsOnly_" + key;
            if (namedAnimations.containsKey(key2)) {
                return namedAnimations.get(key2);
            }
        }
        return namedAnimations.get(key);
    }
    
    
    
    public static Vector3f lerpKeyframes(Keyframe[] keyframes, float seconds, float animSpeed) {
        int i = Math.max(0, MathHelper.binarySearch(0, keyframes.length, index -> seconds <= keyframes[index].timestamp()) - 1);
        int j = Math.min(keyframes.length - 1, i + 1);
        Keyframe keyframe = keyframes[i];
        Keyframe keyframe2 = keyframes[j];
        float h = seconds - keyframe.timestamp();
        float k = j != i ? MathHelper.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
        keyframe2.interpolation().apply(TEMP, k, keyframes, i, j, animSpeed);
        return TEMP;
    }
    
    protected static final Vector3f TEMP = new Vector3f();
    public static void animate(StandEntityModel<?> model, Animation animation, float ticks, float animSpeed, AnimContext animContext) {
        float seconds = animation.looping() ? (ticks / 20.0f) % animation.lengthInSeconds() : ticks / 20.0f;
        animateSecs(model, animation, seconds, animSpeed, animContext);
    }
    
    public static void animateSecs(StandEntityModel<?> model, Animation animation, float seconds, float animSpeed, AnimContext animContext) {
        for (Map.Entry<String, List<Transformation>> entry : animation.boneAnimations().entrySet()) {
            ModelRenderer modelPart = model.getModelPart(entry.getKey());
            if (modelPart != null) {
                List<Transformation> transformations = entry.getValue();
                for (Transformation tf : transformations) {
                    Keyframe[] keyframes = tf.keyframes();
                    lerpKeyframes(keyframes, seconds, animSpeed);
                    if (tf.target() == Targets.ROTATE) {
                        TEMP.mul(MathUtil.DEG_TO_RAD);
                    }
                    else if (tf.target() == Targets.TRANSLATE) {
                        TEMP.mul(1, -1, 1);
                    }
                    tf.target().apply(modelPart, TEMP);
                }
            }
        }
    }
    
    
    public void animFromJson(Animation parsedAnim, JsonObject animJson, String name) {
        StandActionAnimation standAnim = new StandActionAnimation(parsedAnim);
        
        JsonObject instructionsJson = animJson.getAsJsonObject("timeline");
        if (instructionsJson != null) {
            for (Map.Entry<String, JsonElement> keyframeEntry : instructionsJson.entrySet()) {
                float time = Float.parseFloat(keyframeEntry.getKey());
                JsonElement value = keyframeEntry.getValue();
                Iterable<JsonElement> instructions = value.isJsonArray() ? value.getAsJsonArray() : Collections.singleton(value);
                Map<String, String> assignmentMap = Streams.stream(instructions)
                        .filter(JSONUtils::isStringValue)
                        .map(JsonElement::getAsString)
                        .map(instruction -> instruction.split("[ ]*=[ ]*"))
                        .filter(assignment -> assignment.length == 2)
                        .peek(assignment -> {
                            if (assignment[1].endsWith(";")) {
                                assignment[1] = assignment[1].substring(0, assignment[1].length() - 1);
                            }
                        })
                        .collect(Collectors.toMap(assignment -> assignment[0], assignment -> assignment[1], 
                                (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); }, LinkedHashMap::new));
                while (!assignmentMap.isEmpty()) {
                    Map.Entry<String, String> assignment = assignmentMap.entrySet().iterator().next();
                    standAnim.parseAssignmentInstruction(assignment.getKey(), assignment.getValue(), time, assignmentMap);
                    assignmentMap.remove(assignment.getKey());
                }
            }
        }
        
        standAnim.onFinishedParsing();
        putNamedAnim(name, standAnim);
    }


    @Override
    public <T extends StandEntity> void addBarrageSwings(T entity, StandEntityModel<T> model, float ticks) {
        boolean isBarraging = false;
        if (curAnim != null) {
            String barrageType = curAnim.getStringTimelineVal(TimelineKeys.BARRAGE, curAnim.animTime);
            if (barrageType != null) {
                isBarraging = BarrageSwings.onBarrageAnim(barrageType, entity, model, curAnim, ticks, curAnim.animTime);
            }
        }
        entity.animWasBarraging = isBarraging;
    }

    @Override
    public <T extends StandEntity> void renderBarrageSwings(T entity, StandEntityModel<T> model, float yRotOffsetDeg, float xRotDeg,
            MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green,
            float blue, float alpha) {
        BarrageSwings swings = entity.getBarrageSwings();
        if (swings != null) {
            for (BarrageSwing swing : swings.getSwings()) {
                swing.poseAndRender(entity, model, 
                        matrixStack, buffer, yRotOffsetDeg, xRotDeg, 
                        packedLight, packedOverlay, red, green, blue, alpha);
            }
        }
    }
    
}
