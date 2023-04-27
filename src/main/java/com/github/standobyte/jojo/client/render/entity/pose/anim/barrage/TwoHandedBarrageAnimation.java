package com.github.standobyte.jojo.client.render.entity.pose.anim.barrage;

import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;

public class TwoHandedBarrageAnimation<T extends StandEntity> extends ArmsBarrageAnimation<T> {

    public TwoHandedBarrageAnimation(StandEntityModel<T> model, IModelPose<T> loop, IModelPose<T> recovery) {
        super(model, loop, recovery, 4);
    }
    
    @Override
    public void animateSwing(T entity, float loopCompletion, HandSide side, float yRotationOffset, float xRotation, float zRotationOffset) {
        super.animateSwing(entity, loopCompletion, side, yRotationOffset, xRotation, zRotationOffset);
        ModelRenderer arm = model.getArm(side);
        arm.zRot = arm.zRot + HumanoidStandModel.barrageHitEasing(loopCompletion) * zRotationOffset;
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
