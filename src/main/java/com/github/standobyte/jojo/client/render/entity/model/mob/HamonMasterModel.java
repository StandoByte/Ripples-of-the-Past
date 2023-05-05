package com.github.standobyte.jojo.client.render.entity.model.mob;

import com.github.standobyte.jojo.client.ClientTicking;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.client.playeranim.playeranimator.IEntityAnimApplier;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.model.BipedModel;

public class HamonMasterModel extends BipedModel<HamonMasterEntity> implements ClientTicking.ITicking {
    private IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> sittingAnim;
    private boolean animInit = false;

    public HamonMasterModel() {
        super(0.0F);
    }
    
    public void initPose() {
        if (!animInit) {
            sittingAnim = PlayerAnimationHandler.getPlayerAnimator().initHamonMasterPose(this);
            animInit = true;
        }
    }
    
    public void setupPoseRotations(MatrixStack matrixStack, float partialTick) {
        sittingAnim.applyBodyTransforms(matrixStack, partialTick);
    }

    @Override
    public void setupAnim(HamonMasterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        setDefaultPivot();
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        sittingAnim.setEmote();
    }

    private void setDefaultPivot(){
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.rightArm.z = 0.0F;
        this.rightArm.x = - 5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        this.body.xRot = 0.0F;
        this.rightLeg.z = 0.1F;
        this.leftLeg.z = 0.1F;
        this.rightLeg.y = 12.0F;
        this.leftLeg.y = 12.0F;
        this.head.y = 0.0F;
        this.head.zRot = 0f;
        this.body.y = 0.0F;
        this.body.x = 0f;
        this.body.z = 0f;
        this.body.yRot = 0;
        this.body.zRot = 0;
    }
    
    @Override
    public void tick() {
        if (sittingAnim != null) {
            sittingAnim.tick();
        }
    }
}
