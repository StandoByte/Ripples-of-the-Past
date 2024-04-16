package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;

public class StandActionAnimation {

    public StandActionAnimation withPhase(StandEntityAction.Phase phase, Animation anim) {
        return withPhase(phase, anim, PhaseSpeedChange.LINEAR);
    }

    public StandActionAnimation withPhase(StandEntityAction.Phase phase, Animation anim, PhaseSpeedChange speedChange) {
        // TODO 
        return this;
    }
    
    
    @FunctionalInterface
    public interface PhaseSpeedChange {
        public static final PhaseSpeedChange LINEAR = (time, anim, taskPhaseLength) -> time * taskPhaseLength / (20 * anim.lengthInSeconds());
        
        float getNewTime(float time, Animation anim, float taskPhaseLength);
    }
    
}
