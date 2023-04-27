package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;

public abstract class TwoHandedBarrageAnimation<T extends LivingEntity, M extends EntityModel<T>> extends ArmsBarrageAnimation<T, M> {
    public TwoHandedBarrageAnimation(M model, IModelPose<T> loop, IModelPose<T> recovery) {
        super(model, loop, recovery, 4);
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
