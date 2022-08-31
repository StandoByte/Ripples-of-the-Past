package com.github.standobyte.jojo.client.model.pose.anim.barrage;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.HandSide;

public class TwoHandedBarrageAnimation<T extends StandEntity> extends ArmsBarrageAnimation<T> {

    public TwoHandedBarrageAnimation(StandEntityModel<T> model, IModelPose<T> loop) {
        super(model, loop, 4);
    }

    @Override
    protected HandSide getHandSide(Phase phase, T entity, float ticks) {
        return HandSide.RIGHT;
    }
    
    @Override
    protected boolean switchesArms() {
        return true;
    }
}
