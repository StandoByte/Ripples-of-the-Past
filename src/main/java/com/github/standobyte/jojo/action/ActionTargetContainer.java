package com.github.standobyte.jojo.action;

public class ActionTargetContainer {
    private ActionTarget target;
    
    public ActionTargetContainer(ActionTarget target) {
        this.target = target;
    }
    
    public void setNewTarget(ActionTarget target) {
        this.target = target;
    }
    
    public ActionTarget getTarget() {
        return target;
    }

}
