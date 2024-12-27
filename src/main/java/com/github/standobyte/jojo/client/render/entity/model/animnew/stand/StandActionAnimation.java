package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.molang.AnimContext;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.util.math.MathHelper;

public class StandActionAnimation {
    public static final float ANIM_SPEED = 1;
    public final Animation anim;
    
    @Nullable protected AnimObjTimeline<AnimActionPhase> phasesTimeline;
    @Nullable protected Map<String, AnimObjTimeline<String>> stringValTimelines = new HashMap<>();
    @Nullable protected Map<String, AnimObjTimeline<Double>> numericValTimelines = new HashMap<>();
    
    public float animTime;
    
    public StandActionAnimation(Animation anim) {
        this.anim = anim;
    }
    
    
    
    public void poseStand(@Nullable StandEntity entity, StandEntityModel<?> model, 
            float yRotOffsetDeg, float xRotDeg, StandPoseData poseData) {
        boolean appliedPhaseAnim = false;
        if (poseData.actionPhase.isPresent() && phasesTimeline != null) {
            Phase taskPhase = poseData.actionPhase.get();
            
            Float2ObjectMap.Entry<AnimActionPhase> curPhase = null;
            Float2ObjectMap.Entry<AnimActionPhase> nextPhase = null;
            
            @Nonnull Float2ObjectMap.Entry<AnimActionPhase> iterPrevPhase = null;
            for (Float2ObjectMap.Entry<AnimActionPhase> animPhase : phasesTimeline.getEntries()) {
                if (taskPhase.ordinal() < animPhase.getValue().phase.ordinal()) {
                    curPhase = iterPrevPhase;
                    nextPhase = animPhase;
                    break;
                }
                iterPrevPhase = animPhase;
            }
            if (curPhase == null) {
                if (taskPhase == iterPrevPhase.getValue().phase) {
                    curPhase = iterPrevPhase;
                }
            }
            if (curPhase != null) {
                float curPhaseTime = curPhase.getFloatKey();
                float nextPhaseTime = nextPhase != null ? nextPhase.getFloatKey() : anim.lengthInSeconds();
                switch (curPhase.getValue().timeAnimMode) {
                case FIT_PHASE_LENGTH:
                    animTime = MathHelper.lerp(poseData.phaseCompletion, curPhaseTime, nextPhaseTime);
                    break;
                case PRESERVE_PHASE_LENGTH:
                    animTime = curPhaseTime + poseData.animTime / 20f;
                    if (entity != null && animTime >= anim.lengthInSeconds()) {
                        entity.onSetPoseAnimEnded();
                    }
                    break;
                case LOOP_BACK:
                    float loopLen = nextPhaseTime - curPhase.getValue().loopBackTo;
                    animTime = curPhaseTime + (poseData.animTime / 20f) % loopLen;
                    break;
                default:
                    break;
                }
                appliedPhaseAnim = true;
            }
        }
        
        if (!appliedPhaseAnim) {
            animTime = anim.looping() ? (poseData.animTime / 20f) % anim.lengthInSeconds() : poseData.animTime / 20f;
        }
        
        AnimContext animContext = AnimContext.fillContext(entity, yRotOffsetDeg, xRotDeg);
        GeckoStandAnimator.animateSecs(model, anim, animTime, ANIM_SPEED, animContext);
    }
    
    
    public void parseAssignmentInstruction(String field, String value, float keyframeTime, Map<String, String> assignmentMap) {
        switch (field) {
        case "phase":
            Phase phase = Phase.valueOf(value);
            if (phasesTimeline == null) {
                phasesTimeline = new AnimObjTimeline<>();
            }
            AnimActionPhase animPhase = parseAnimPhase(phase, assignmentMap);
            phasesTimeline.add(keyframeTime, animPhase);
            break;
        default:
            if (stringValTimelines == null) {
                stringValTimelines = new HashMap<>();
            }
            AnimObjTimeline<String> timeline = stringValTimelines.computeIfAbsent(field, __ -> new AnimObjTimeline<>());
            timeline.add(keyframeTime, value);
            break;
        }
    }
    
    protected AnimActionPhase parseAnimPhase(Phase phase, Map<String, String> assignmentMap) {
        if (assignmentMap.containsKey("phase.loopBack")) {
            try {
                float loopBackTo = Float.parseFloat(assignmentMap.get("phase.loopBack"));
                assignmentMap.remove("phase.loopBack");
                return AnimActionPhase.loopBack(phase, loopBackTo);
            }
            catch (NumberFormatException e) {}
        }
        return new AnimActionPhase(phase, AnimActionPhase.Mode.FIT_PHASE_LENGTH);
    }
    
    public void onFinishedParsing() {
        if (stringValTimelines != null) {
            Iterator<Map.Entry<String, AnimObjTimeline<String>>> iter = stringValTimelines.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, AnimObjTimeline<String>> entry = iter.next();
                timelineToNumeric(entry.getValue()).ifPresent(numericTimeline -> {
                    if (numericValTimelines == null) {
                        numericValTimelines = new HashMap<>();
                    }
                    numericValTimelines.put(entry.getKey(), numericTimeline);
                    iter.remove();
                });
            }
        }
        
        if (phasesTimeline != null) phasesTimeline.sort();
        if (stringValTimelines != null) stringValTimelines.values().forEach(AnimObjTimeline::sort);
        if (numericValTimelines != null) numericValTimelines.values().forEach(AnimObjTimeline::sort);
    }
    
    private static Optional<AnimObjTimeline<Double>> timelineToNumeric(AnimObjTimeline<String> stringTimeline) {
        AnimObjTimeline<Double> timeline = new AnimObjTimeline<>();
        for (Float2ObjectMap.Entry<String> entry : stringTimeline.getEntries()) {
            try {
                double numericVal = Double.parseDouble(entry.getValue());
                timeline.add(entry.getFloatKey(), numericVal);
            }
            catch (NumberFormatException notNumeric) {
                return Optional.empty();
            }
        }
        return Optional.of(timeline);
    }
    
    @Nullable
    public String getStringTimelineVal(String key, float animTime) {
        if (stringValTimelines == null) {
            return null;
        }
        AnimObjTimeline<String> timeline = stringValTimelines.get(key);
        if (timeline == null) {
            return null;
        }
        return timeline.getCurValue(animTime);
    }
    
    @Nullable
    public Double getNumericTimelineVal(String key, float animTime) {
        if (numericValTimelines == null) {
            return null;
        }
        AnimObjTimeline<Double> timeline = numericValTimelines.get(key);
        if (timeline == null) {
            return null;
        }
        return timeline.getCurValue(animTime);
    }
    
    public static class TimelineKeys {
        public static final String BARRAGE = "barrage";
    }
    
}
