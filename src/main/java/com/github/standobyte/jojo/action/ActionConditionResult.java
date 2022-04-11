package com.github.standobyte.jojo.action;

import javax.annotation.Nullable;

import net.minecraft.util.text.ITextComponent;

public class ActionConditionResult {
    private final boolean positive;
    private final boolean stopHeldAction;
    private final boolean queueInput;
    private final ITextComponent warning;
    
    public static final ActionConditionResult POSITIVE = new ActionConditionResult(true, false, false, null);
    public static final ActionConditionResult NEGATIVE = new ActionConditionResult(false, true, false, null);
    public static final ActionConditionResult NEGATIVE_CONTINUE_HOLD = new ActionConditionResult(false, false, false, null);
    public static final ActionConditionResult NEGATIVE_QUEUE_INPUT = new ActionConditionResult(false, true, true, null);
    
    public static ActionConditionResult createNegative(ITextComponent warning) {
        return new ActionConditionResult(false, true, false, warning);
    }
    
    public static ActionConditionResult createNegativeContinueHold(ITextComponent warning) {
        return new ActionConditionResult(false, false, false, warning);
    }
    
    private ActionConditionResult(boolean positive, boolean stopHeldAction, boolean queueInput, ITextComponent warning) {
        this.positive = positive;
        this.stopHeldAction = stopHeldAction;
        this.queueInput = queueInput;
        this.warning = warning;
    }
    
    public boolean isPositive() {
        return positive;
    }
    
    public boolean shouldStopHeldAction() {
        return !isPositive() && stopHeldAction;
    }
    
    public boolean queueInput() {
        return queueInput;
    }
    
    @Nullable
    public ITextComponent getWarning() {
        return warning;
    }
}
