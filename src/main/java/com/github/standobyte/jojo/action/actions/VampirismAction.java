package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

public abstract class VampirismAction extends Action<INonStandPower> {
    
    public VampirismAction(Action.AbstractBuilder<?> builder) {
        super(builder);
    }
}
