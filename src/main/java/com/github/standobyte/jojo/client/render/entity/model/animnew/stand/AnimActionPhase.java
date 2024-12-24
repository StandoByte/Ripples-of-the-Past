package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import com.github.standobyte.jojo.action.stand.StandEntityAction;

public class AnimActionPhase {
    public final StandEntityAction.Phase phase;
    public final Mode timeAnimMode;
    public final float loopBackTo;
    
    public AnimActionPhase(StandEntityAction.Phase phase, Mode timeAnimMode) {
        this(phase, timeAnimMode, 0);
    }
    
    private AnimActionPhase(StandEntityAction.Phase phase, Mode timeAnimMode, float loopBackTo) {
        this.phase = phase;
        this.timeAnimMode = timeAnimMode;
        this.loopBackTo = loopBackTo;
    }
    
    public static AnimActionPhase loopBack(StandEntityAction.Phase phase, float loopBackTo) {
        return new AnimActionPhase(phase, Mode.LOOP_BACK, loopBackTo);
    }
    
    
    
    public enum Mode {
        FIT_PHASE_LENGTH,
        PRESERVE_PHASE_LENGTH,
        LOOP_BACK;
    }
}
