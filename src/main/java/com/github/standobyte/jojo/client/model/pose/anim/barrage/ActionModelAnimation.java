package com.github.standobyte.jojo.client.model.pose.anim.barrage;

import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.client.model.pose.anim.IActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;

public abstract class ActionModelAnimation<T extends StandEntity> implements IActionAnimation<T> {
    protected final StandEntityModel<T> model;
    
    public ActionModelAnimation(StandEntityModel<T> model) {
        this.model = model;
    }
}
