package com.github.standobyte.jojo.action;

import javax.annotation.Nullable;

import net.minecraft.util.text.ITextComponent;

public class ActionConditionResult {
    private final boolean positive;
    private final boolean stopHeldAction;
    private final boolean isQueued;
    private final ITextComponent warning;
    
    public static final ActionConditionResult POSITIVE = new ActionConditionResult(true, false, false, null);
    public static final ActionConditionResult NEGATIVE = new ActionConditionResult(false, true, false, null);
    public static final ActionConditionResult NEGATIVE_CONTINUE_HOLD = new ActionConditionResult(false, false, false, null);
    public static final ActionConditionResult NEGATIVE_QUEUEABLE = new ActionConditionResult(false, true, true, null);
    
    public static ActionConditionResult createNegative(ITextComponent warning) {
        return new ActionConditionResult(false, true, false, warning);
    }
    
    public static ActionConditionResult noMessage(boolean isPositive) {
        return isPositive ? POSITIVE : NEGATIVE;
    }
    
    private ActionConditionResult(boolean positive, boolean stopHeldAction, boolean isQueued, ITextComponent warning) {
        this.positive = positive;
        this.stopHeldAction = stopHeldAction;
        this.isQueued = isQueued;
        this.warning = warning;
    }
    
    public ActionConditionResult setContinueHold() {
        return setContinueHold(true);
    }
    
    public ActionConditionResult setContinueHold(boolean continueHold) {
        return new ActionConditionResult(this.positive, !continueHold, this.isQueued, this.warning);
    }
    
    public boolean isPositive() {
        return positive;
    }
    
    public boolean shouldStopHeldAction() {
        return !isPositive() && stopHeldAction;
    }
    
    public boolean isQueued() {
        return isQueued;
    }
    
    @Nullable
    public ITextComponent getWarning() {
        return warning;
    }
}
