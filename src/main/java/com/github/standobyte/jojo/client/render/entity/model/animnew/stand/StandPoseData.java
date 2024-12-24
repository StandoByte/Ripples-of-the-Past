package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.entity.stand.StandPose;

public class StandPoseData {
    public StandPose standPose = StandPose.IDLE;
    public Optional<Phase> actionPhase = Optional.empty();
    public float animTime = 0;
    public float phaseCompletion = 0;
    
    public static StandPoseDataFill start() {
        INSTANCE.standPose = StandPose.IDLE;
        INSTANCE.actionPhase = Optional.empty();
        INSTANCE.phaseCompletion = 0;
        INSTANCE.animTime = 0;
        return CHAINABLE_FILL_INSTANCE;
    }
    
    public StandPoseDataFill edit() {
        return CHAINABLE_FILL_INSTANCE;
    }
    
    public static class StandPoseDataFill {
        
        private StandPoseDataFill() {}
        
        public StandPoseDataFill standPose(StandPose standPose) {
            INSTANCE.standPose = standPose;
            return this;
        }
        
        public StandPoseDataFill actionPhase(Phase actionPhase) {
            INSTANCE.actionPhase = Optional.of(actionPhase);
            return this;
        }
        
        public StandPoseDataFill actionPhase(Optional<Phase> actionPhase) {
            INSTANCE.actionPhase = actionPhase;
            return this;
        }
        
        public StandPoseDataFill phaseCompletion(float phaseCompletion) {
            INSTANCE.phaseCompletion = phaseCompletion;
            return this;
        }
        
        public StandPoseDataFill animTime(float animTime) {
            INSTANCE.animTime = animTime;
            return this;
        }
        
        public StandPoseData end() {
            return INSTANCE;
        }
    }
    
    
    private static final StandPoseData INSTANCE = new StandPoseData();
    private static final StandPoseDataFill CHAINABLE_FILL_INSTANCE = new StandPoseDataFill();
    private StandPoseData() {}
}
