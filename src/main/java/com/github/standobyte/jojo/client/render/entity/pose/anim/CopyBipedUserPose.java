package com.github.standobyte.jojo.client.render.entity.pose.anim;

import java.util.function.UnaryOperator;

import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;

public class CopyBipedUserPose<T extends StandEntity> implements IModelPose<T> {
    private final HumanoidStandModel<T> model;
    
    public CopyBipedUserPose(HumanoidStandModel<T> model) {
        this.model = model;
    }

    @Override
    public void poseModel(float rotationAmount, T entity, float ticks, float yRotOffsetRad, float xRotRad,
            HandSide side) {
        LivingEntity user = entity.getUser();
        if (user != null) {
            EntityRenderer<?> userRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(user);
            if (userRenderer instanceof LivingRenderer) {
                EntityModel<?> userModel = ((LivingRenderer<?, ?>) userRenderer).getModel();
                if (userModel instanceof BipedModel) {
                    BipedModel<?> userBipedModel = (BipedModel<?>) userModel;

                    model.resetPose(entity);

                    copyRotation(model.getHead(), userBipedModel.head);
                    copyRotation(model.getTorso(), userBipedModel.body);
                    copyRotation(model.getArm(HandSide.LEFT), userBipedModel.leftArm);
                    copyRotation(model.getArm(HandSide.RIGHT), userBipedModel.rightArm);
                    copyRotation(model.getLeg(HandSide.LEFT), userBipedModel.leftLeg);
                    copyRotation(model.getLeg(HandSide.RIGHT), userBipedModel.rightLeg);
                }
            }
        }        
    }
    
    private void copyRotation(ModelRenderer to, ModelRenderer from) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
    }

    @Override
    public IModelPose<T> setEasing(UnaryOperator<Float> function) {
        return this;
    }

}
