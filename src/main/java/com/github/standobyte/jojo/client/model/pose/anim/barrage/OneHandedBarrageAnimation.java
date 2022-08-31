package com.github.standobyte.jojo.client.model.pose.anim.barrage;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;

import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;

public class OneHandedBarrageAnimation<T extends StandEntity> extends ArmsBarrageAnimation<T> {
    private final Hand standArm;

    public OneHandedBarrageAnimation(StandEntityModel<T> model, IModelPose<T> loop, Hand standArm) {
        super(model, loop, 2);
        this.standArm = standArm;
    }

    @Override
    protected HandSide getHandSide(Phase phase, T entity, float ticks) {
        return entity.getArm(standArm);
    }
    
    @Override
    protected boolean switchesArms() {
        return false;
    }
    
    @Override
    protected float swingsToAdd(StandEntity entity, float loop, float lastLoop) {
        return StandStatFormulas.getBarrageHitsPerSecond(entity.getAttackSpeed()) / 20F * Math.min(loop - lastLoop, 1) * getLoopLen();
    }
}
