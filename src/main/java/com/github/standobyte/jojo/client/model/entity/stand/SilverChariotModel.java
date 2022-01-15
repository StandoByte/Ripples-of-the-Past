package com.github.standobyte.jojo.client.model.entity.stand;

import com.github.standobyte.jojo.action.actions.StandEntityAction.Phase;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;

// Made with Blockbench 3.9.2


public class SilverChariotModel extends HumanoidStandModel<SilverChariotEntity> {
    private ModelRenderer backCord;
    private ModelRenderer bone1;
    private ModelRenderer bone2;
    private ModelRenderer rapier;
    private ModelRenderer rapierBlade;
    
    public SilverChariotModel() {
        this(64, 64);
    }
    
    public SilverChariotModel(int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);
    }
    
    @Override
    protected void addBaseBoxes() {
        head.texOffs(22, 0).addBox(-4.0F, -5.1F, -4.0F, 8.0F, 3.0F, 3.0F, -0.1F, false);
        head.texOffs(0, 8).addBox(-4.0F, -4.25F, -1.05F, 8.0F, 2.0F, 5.0F, -0.05F, false);
        head.texOffs(18, 18).addBox(-4.0F, -4.85F, -4.0F, 8.0F, 3.0F, 1.0F, 0.25F, false);
        head.texOffs(38, 10).addBox(-3.5F, -3.1F, -2.75F, 7.0F, 3.0F, 6.0F, 0.0F, false);
        head.texOffs(50, 6).addBox(-1.0F, -2.15F, -4.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(58, 4).addBox(-4.5F, -4.25F, -3.0F, 1.0F, 2.0F, 2.0F, 0.1F, false);
        head.texOffs(58, 14).addBox(-3.5F, -1.5F, -3.0F, 1.0F, 1.0F, 1.0F, 0.25F, false);
        head.texOffs(58, 12).addBox(2.5F, -1.5F, -3.0F, 1.0F, 1.0F, 1.0F, 0.25F, false);
        head.texOffs(58, 8).addBox(3.5F, -4.25F, -3.0F, 1.0F, 2.0F, 2.0F, 0.1F, false);
        head.texOffs(9, 16).addBox(-0.5F, -7.85F, -3.5F, 1.0F, 4.0F, 7.0F, 0.2F, false);
        head.texOffs(22, 6).addBox(-2.5F, -6.9F, -4.4F, 5.0F, 1.0F, 1.0F, -0.333F, false);
        head.texOffs(22, 8).addBox(-2.5F, -6.4F, -4.4F, 5.0F, 1.0F, 1.0F, -0.333F, false);
        head.texOffs(26, 10).addBox(1.5F, -6.9F, -4.07F, 1.0F, 1.0F, 3.0F, -0.333F, false);
        head.texOffs(26, 10).addBox(1.5F, -6.4F, -4.07F, 1.0F, 1.0F, 3.0F, -0.333F, false);
        head.texOffs(26, 10).addBox(-2.5F, -6.9F, -4.07F, 1.0F, 1.0F, 3.0F, -0.333F, false);
        head.texOffs(26, 10).addBox(-2.5F, -6.4F, -4.07F, 1.0F, 1.0F, 3.0F, -0.333F, false);
        head.texOffs(48, 0).addBox(-3.5F, -1.5F, -4.25F, 7.0F, 1.0F, 1.0F, -0.333F, false);
        head.texOffs(54, 2).addBox(2.5F, -1.5F, -3.917F, 1.0F, 1.0F, 2.0F, -0.333F, false);
        head.texOffs(48, 2).addBox(-3.5F, -1.5F, -3.917F, 1.0F, 1.0F, 2.0F, -0.333F, false);
        head.texOffs(34, 6).addBox(-2.0F, -8.3F, -0.5F, 4.0F, 1.0F, 1.0F, -0.333F, false);
        head.texOffs(38, 8).addBox(1.0F, -7.967F, -0.5F, 1.0F, 2.0F, 1.0F, -0.333F, false);
        head.texOffs(34, 8).addBox(-2.0F, -7.967F, -0.5F, 1.0F, 2.0F, 1.0F, -0.333F, false);

        backCord = new ModelRenderer(this);
        backCord.setPos(0.0F, -4.0F, 2.55F);
        head.addChild(backCord);
        setRotationAngle(backCord, 0.0873F, 0.0F, 0.0F);
        backCord.texOffs(26, 16).addBox(-2.5F, -2.75F, -0.5F, 5.0F, 1.0F, 1.0F, -0.333F, false);
        backCord.texOffs(36, 12).addBox(1.5F, -2.417F, -0.5F, 1.0F, 3.0F, 1.0F, -0.333F, false);
        backCord.texOffs(40, 12).addBox(-2.5F, -2.417F, -0.5F, 1.0F, 3.0F, 1.0F, -0.333F, false);

        bone1 = new ModelRenderer(this);
        bone1.setPos(4.25F, -4.45F, -0.25F);
        head.addChild(bone1);
        setRotationAngle(bone1, 0.0F, 0.0F, -0.1309F);
        bone1.texOffs(0, 15).addBox(-4.0F, -3.25F, -2.0F, 4.0F, 4.0F, 4.0F, -0.25F, true);

        bone2 = new ModelRenderer(this);
        bone2.setPos(-4.25F, -4.45F, -0.25F);
        head.addChild(bone2);
        setRotationAngle(bone2, 0.0F, 0.0F, 0.1309F);
        bone2.texOffs(0, 15).addBox(0.0F, -3.25F, -2.0F, 4.0F, 4.0F, 4.0F, -0.25F, false);

        torso.texOffs(0, 0).addBox(-4.0F, 1.0F, -1.5F, 8.0F, 5.0F, 3.0F, 0.0F, false);
        torso.texOffs(33, 29).addBox(-1.5F, 0.5F, -2.5F, 3.0F, 3.0F, 5.0F, -0.6F, false);
        torso.texOffs(29, 30).addBox(-0.5F, -0.1F, -0.5F, 1.0F, 12.0F, 1.0F, 0.1F, false);
        torso.texOffs(0, 27).addBox(-2.5F, -0.1F, -2.0F, 1.0F, 2.0F, 4.0F, -0.2F, false);
        torso.texOffs(0, 33).addBox(1.5F, -0.1F, -2.0F, 1.0F, 2.0F, 4.0F, -0.2F, false);
        torso.texOffs(33, 37).addBox(-3.0F, 10.0F, -2.0F, 6.0F, 2.0F, 4.0F, 0.1F, false);

        leftArm.texOffs(52, 59).addBox(-2.0F, 2.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, true);
        leftArm.texOffs(14, 47).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 0.0F, true);

        leftArmJoint.setPos(-0.5F, 4.0F, 0.0F);
        leftArmJoint.texOffs(2, 39).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, true);

        leftForeArm.texOffs(12, 55).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, true);

        rightArm.texOffs(52, 54).addBox(-1.0F, 2.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, false);
        rightArm.texOffs(0, 47).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 0.0F, false);

        rightArmJoint.setPos(0.5F, 4.0F, 0.0F);
        rightArmJoint.texOffs(2, 39).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, false);

        rightForeArm.texOffs(0, 55).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rapier = new ModelRenderer(this);
        rapier.setPos(0.25F, 4.5F, 0.0F);
        rightForeArm.addChild(rapier);
        setRotationAngle(rapier, 0.7854F, 0.0F, 0.0F);
        rapier.texOffs(11, 35).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 5.0F, 0.25F, false);
        rapier.texOffs(13, 28).addBox(-0.5F, -0.5F, -3.25F, 1.0F, 1.0F, 6.0F, 0.0F, false);

        rapierBlade = new ModelRenderer(this);
        rapierBlade.setPos(0.0F, 0.0F, 0.0F);
        rapier.addChild(rapierBlade);
        rapierBlade.texOffs(12, 28).addBox(-0.5F, -0.5F, -17.0F, 1.0F, 1.0F, 15.0F, -0.3F, false);

        leftLeg.setPos(1.4F, 12.0F, 0.0F);
        leftLeg.texOffs(40, 46).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        leftLegJoint.texOffs(54, 39).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, true);

        leftLowerLeg.texOffs(36, 55).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rightLeg.setPos(-1.4F, 12.0F, 0.0F);
        rightLeg.texOffs(28, 46).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rightLegJoint.texOffs(54, 39).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, false);

        rightLowerLeg.texOffs(24, 55).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);
    }

    @Override
    public void prepareMobModel(SilverChariotEntity entity, float walkAnimPos, float walkAnimSpeed, float partialTick) {
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        super.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
        if (rapierBlade != null) {
            rapierBlade.visible = entity.hasRapier();
        }
    }
    
    @Override
    protected void swingArm(SilverChariotEntity entity, float xRotation, HandSide swingingHand) {
        if (!entity.hasRapier()) {
            super.swingArm(entity, xRotation, swingingHand);
        }
        else {
            if (swingingHand != entity.getMainArm()) {
                swingingHand = swingingHand.getOpposite();
                attackTime = 1 - attackTime;
            }
            ModelRenderer punchingArm;
            ModelRenderer punchingForeArm;
            ModelRenderer otherArm;
            if (swingingHand == HandSide.LEFT) {
                punchingArm = this.leftArm;
                punchingForeArm = this.leftForeArm;
                otherArm = this.rightArm;
            }
            else {
                punchingArm = this.rightArm;
                punchingForeArm = this.rightForeArm;
                otherArm = this.leftArm;
            }    
            
            float f1 = 1.0F - this.attackTime;
            f1 = f1 * f1;
            f1 = f1 * f1;
            f1 = 1.0F - f1;
            
            upperPart.yRot = -0.7854F;
            if (swingingHand == HandSide.LEFT) {
                this.upperPart.yRot *= -1.0F;
            }
            
            punchingArm.zRot = 0;
            otherArm.zRot = 1.0472F;
            leftArm.zRot *= -1.0F;
            punchingForeArm.zRot = -1.5708F * (1 - f1);
            
            punchingArm.yRot = 2.3562F - 1.7453F * f1;
            otherArm.yRot = 0;
            leftArm.yRot *= -1.0F;
            
            punchingArm.xRot = -1.5708F + xRotation * MathUtil.DEG_TO_RAD;
            
            if (rapier != null) {
                rapier.xRot = 1.5708F;
            }
        }
    }
    
    @Override
    protected void resetPose() {
        super.resetPose();
        if (rapier != null) {
            rapier.xRot = 0.7854F;
            rapier.yRot = 0;
            rapier.zRot = 0;
        }
    }
    
    @Override
    public void blockingPose(SilverChariotEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        if (!entity.hasRapier()) {
            super.blockingPose(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        }
        else {
            setRotationAngle(leftArm, -0.8727F, 0.0F, -0.1745F);
            setRotationAngle(leftForeArm, -1.5708F, 0.2618F, 0.0F);
            setRotationAngle(rightArm, 0.5236F, 0.0F, 0.1746F);
            setRotationAngle(rightForeArm, -1.9199F, 0.0F, 0.0F);
            if (rapier != null) {
                setRotationAngle(rapier, 0.829F, 0.0F, -1.1781F);
            }
        }
    }
    
    @Override
    protected int getSummonPosesCount() {
        return 3;
    }
    
    @Override
    protected void summonPose(float animationFactor, int poseVariant) {
        switch (poseVariant) {
        case 0:
            setSummonPoseRotationAngle(head, -0.1745F, 0.0F, -0.0873F, animationFactor);
            setSummonPoseRotationAngle(upperPart, 0.0F, -0.2618F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftArm, 0.7854F, -0.2618F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightArm, -1.8326F, -0.4363F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftLeg, 0.2618F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightLeg, 0.2618F, 0.0F, 0.2618F, animationFactor);
            if (rapier != null) {
                setSummonPoseRotationAngle(rapier, -0.3491F, 2.3562F, 0.0F, animationFactor, 0.7854F, 0.0F, 0.0F);
            }
            break;
        case 1:
            setSummonPoseRotationAngle(body, 0.0F, -0.3927F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftArm, 0.5236F, 0.0F, -0.9599F, animationFactor);
            setSummonPoseRotationAngle(leftForeArm, 0.0F, 0.0F, 1.9199F, animationFactor);
            setSummonPoseRotationAngle(rightArm, -1.5708F, 0.3927F, 0.0F, animationFactor);
            if (rapier != null) {
                setSummonPoseRotationAngle(rapier, 1.5708F, 0.0F, 0.0F, animationFactor, 0.7854F, 0.0F, 0.0F);
            }
            break;
        case 2:
            setSummonPoseRotationAngle(head, 0.0873F, 0.0436F, -0.1745F, animationFactor);
            setSummonPoseRotationAngle(body, 0.0F, -0.3927F, -0.1309F, animationFactor);
            setSummonPoseRotationAngle(upperPart, 0.0F, 0.3927F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftArm, 0.3927F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightArm, 0.2618F, -0.2618F, 1.3963F, animationFactor);
            setSummonPoseRotationAngle(rightForeArm, -2.3562F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftLeg, 0.0F, 0.3927F, 0.0873F, animationFactor);
            setSummonPoseRotationAngle(rightLeg, 0.0F, 0.3927F, 0.2182F, animationFactor);
            setSummonPoseRotationAngle(rightLowerLeg, 0.3491F, 0.0F, -0.0873F, animationFactor);
            if (rapier != null) {
                setSummonPoseRotationAngle(rapier, 2.0071F, 0.0F, 0.0F, animationFactor);
            }
            break;
        }
    }
    
    @Override
    protected void rangedAttackPose(SilverChariotEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
        entity.setYBodyRot(entity.yRot);
        setRotationAngle(body, 0.0F, -0.7854F, 0.0F);
        setRotationAngle(upperPart, 0.0F, -0.7854F, 0.0F);
        setRotationAngle(leftArm, 0.2618F, 0.0F, -0.1309F);
        setRotationAngle(rightArm, -1.5708F, 1.5708F, 0.0F);
        setRotationAngle(rightArm, 0.0F, 1.5708F, 1.5708F - xRotation * MathUtil.DEG_TO_RAD);
        if (rapier != null) {
            setRotationAngle(rapier, 1.5708F, 0.0F, 0.0F);
        }
    }
}
