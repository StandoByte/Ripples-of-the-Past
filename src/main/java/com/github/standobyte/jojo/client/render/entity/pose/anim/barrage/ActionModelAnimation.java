package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.pose.anim.IActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;

public abstract class ActionModelAnimation<T extends StandEntity> implements IActionAnimation<T> {
    protected final StandEntityModel<T> model;
    
    public ActionModelAnimation(StandEntityModel<T> model) {
        this.model = model;
    }
}
