package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;

public class StandOneHandedBarrageAnimation<T extends StandEntity> extends ArmsBarrageAnimation<T, StandEntityModel<T>> {
    private final Hand standArm;

    public StandOneHandedBarrageAnimation(StandEntityModel<T> model, IModelPose<T> loop, IModelPose<T> recovery, Hand standArm) {
        super(model, loop, recovery, 2);
        this.standArm = standArm;
    }
    
    @Override
    protected boolean switchesArms() {
        return false;
    }

    @Override
    public BarrageSwingsHolder<T, StandEntityModel<T>> getBarrageSwingsHolder(T entity) {
        return (BarrageSwingsHolder<T, StandEntityModel<T>>) entity.getBarrageSwingsHolder();
    }

    @Override
    protected HandSide getHandSide(Phase phase, T entity, float ticks) {
        return entity.getArm(standArm);
    }
    
    @Override
    protected float swingsToAdd(T entity, float loop, float lastLoop) {
        return standEntitySwings(entity, loop, lastLoop, getLoopLen());
    }

    @Override
    protected double maxSwingOffset(T entity) {
        return maxStandSwingOffset(entity);
    }

    @Override
    protected void addSwing(T entity, BarrageSwingsHolder<T, StandEntityModel<T>> swings, HandSide side, float f,
            double maxOffset) {
        swings.addSwing(new StandArmBarrageSwing<>(this, f, getLoopLen(), side, maxOffset));
    }
    
    @Override
    public void beforeSwingAfterimageRender(MatrixStack matrixStack, StandEntityModel<T> model, float loopCompletion, HandSide side) {
        model.applyXRotation();
    }
}
