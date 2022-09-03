package com.github.standobyte.jojo.client.model.pose.anim.barrage;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;

import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

public abstract class ArmsBarrageAnimation<T extends StandEntity> extends ActionModelAnimation<T> implements IBarrageAnimation<T> {
    private final IModelPose<T> loop;
    private float loopLen;

    public ArmsBarrageAnimation(StandEntityModel<T> model, IModelPose<T> loop, float loopLen) {
        super(model);
        this.loop = loop;
        this.loopLen = loopLen;
    }

    @Override
    public void animate(Phase phase, float actionCompletion, T entity, float ticks, 
            float yRotationOffset, float xRotation, HandSide side, boolean layer) {
        float loop = ticks / getLoopLen();
        side = getHandSide(phase, entity, ticks);
        
        switch (phase) {
        case PERFORM:
            animateSwing(entity, MathHelper.frac(loop), side, yRotationOffset, xRotation);
            if (!layer) {
                BarrageSwingsHolder<T> swings = (BarrageSwingsHolder<T>) entity.getBarrageSwingsHolder();
                float swingsToAdd = swingsToAdd(entity, loop, swings.getLoopCount());
                if (swingsToAdd > 0) {
                    addSwings(entity, swings, side, swingsToAdd);
                }
                swings.setLoopCount(loop);
            }
            break;
        default:
            break;
        }
    }

    protected abstract HandSide getHandSide(Phase phase, T entity, float ticks);
    
    protected abstract boolean switchesArms();
    
    public void animateSwing(T entity, float loopCompletion, HandSide side, float yRotationOffset, float xRotation) {
        loop.poseModel(loopCompletion, entity, 0, yRotationOffset, xRotation, side);
    }
    
    protected float getLoopLen() {
        return loopLen;
    }
    
    protected float swingsToAdd(StandEntity entity, float loop, float lastLoop) {
        return (int) (loop * 2) > (int) (lastLoop * 2) ? 
                StandStatFormulas.getBarrageHitsPerSecond(entity.getAttackSpeed()) * getLoopLen() / 20F - 1
                : 0;
    }

    protected void addSwings(T entity, BarrageSwingsHolder<T> swings, HandSide side, float hits) {
        int swingsToAdd = (int) hits;
        if (entity.getRandom().nextFloat() <= (float) (hits - swingsToAdd)) swingsToAdd++;
        double maxOffset = 0.8 / (entity.getPrecision() / 24 + 1);
        if (switchesArms() && entity.getRandom().nextBoolean()) side = side.getOpposite();
        
        for (int i = 0; i < swingsToAdd; i++) {
            float f = ((float) i / (float) swingsToAdd
                    + (entity.getRandom().nextFloat() - 0.5F) * 0.4F / hits)
                    * getLoopLen();
            if (switchesArms()) side = side.getOpposite();
            swings.addSwing(new ArmBarrageSwing<T>(this, f, getLoopLen(), side, entity, maxOffset));
        }
    }
}
