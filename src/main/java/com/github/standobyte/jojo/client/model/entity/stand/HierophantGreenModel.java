package com.github.standobyte.jojo.client.model.entity.stand;

import com.github.standobyte.jojo.action.actions.StandEntityAction.Phase;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class HierophantGreenModel extends HumanoidStandModel<HierophantGreenEntity> {
    private final ModelRenderer bone7;
    private final ModelRenderer bone8;
    private final ModelRenderer bone9;
    private final ModelRenderer bone10;
    private final ModelRenderer bone11;
    private final ModelRenderer bone12;
    
    public HierophantGreenModel() {
        this(64, 64);
    }
    
    public HierophantGreenModel(int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);
        head.texOffs(0, 16).addBox(-4.5F, -4.5F, -1.0F, 9.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(52, 28).addBox(-1.5F, -8.5F, -1.5F, 3.0F, 1.0F, 3.0F, 0.0F, false);
        head.texOffs(48, 4).addBox(-1.5F, -2.1F, -4.5F, 3.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(48, 0).addBox(-1.0F, -6.0F, -4.3F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        head.texOffs(20, 0).addBox(-3.0F, -4.0F, -4.15F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        head.texOffs(20, 0).addBox(1.0F, -4.0F, -4.15F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        torso.texOffs(30, 16).addBox(-1.0F, 3.0F, -2.8F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        torso.texOffs(36, 16).addBox(-1.0F, 3.0F, 1.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        torso.texOffs(0, 20).addBox(-2.0F, -0.5F, -2.5F, 4.0F, 4.0F, 5.0F, 0.0F, false);
        torso.texOffs(48, 16).addBox(-2.5F, 4.5F, -2.6F, 5.0F, 5.0F, 1.0F, -0.4F, false);

        bone7 = new ModelRenderer(this);
        bone7.setPos(-1.5F, 3.5F, 0.0F);
        torso.addChild(bone7);
        setRotationAngle(bone7, 0.0F, 0.0F, -0.5236F);
        bone7.texOffs(18, 18).addBox(-0.5F, -4.5F, -2.5F, 1.0F, 5.0F, 5.0F, 0.0F, false);

        bone8 = new ModelRenderer(this);
        bone8.setPos(1.5F, 3.5F, 0.0F);
        torso.addChild(bone8);
        setRotationAngle(bone8, 0.0F, 0.0F, 0.5236F);
        bone8.texOffs(18, 18).addBox(-0.5F, -4.5F, -2.5F, 1.0F, 5.0F, 5.0F, 0.0F, true);

        bone9 = new ModelRenderer(this);
        bone9.setPos(-1.5F, 3.5F, 0.0F);
        torso.addChild(bone9);
        setRotationAngle(bone9, 0.0F, 0.0F, -0.8727F);
        bone9.texOffs(30, 21).addBox(-0.5F, -5.5F, -2.5F, 1.0F, 6.0F, 5.0F, 0.0F, false);

        bone10 = new ModelRenderer(this);
        bone10.setPos(1.5F, 3.5F, 0.0F);
        torso.addChild(bone10);
        setRotationAngle(bone10, 0.0F, 0.0F, 0.8727F);
        bone10.texOffs(30, 21).addBox(-0.5F, -5.5F, -2.5F, 1.0F, 6.0F, 5.0F, 0.0F, false);

        bone11 = new ModelRenderer(this);
        bone11.setPos(-1.5F, 3.5F, -0.25F);
        torso.addChild(bone11);
        setRotationAngle(bone11, 0.0F, 0.0F, -2.5307F);
        bone11.texOffs(42, 18).addBox(-0.5F, -4.5F, -2.5F, 1.0F, 5.0F, 5.0F, 0.0F, false);

        bone12 = new ModelRenderer(this);
        bone12.setPos(1.5F, 3.5F, -0.25F);
        torso.addChild(bone12);
        setRotationAngle(bone12, 0.0F, 0.0F, 2.5307F);
        bone12.texOffs(42, 18).addBox(-0.5F, -4.5F, -2.5F, 1.0F, 5.0F, 5.0F, 0.0F, true);
    }

//    @Override
//    protected int getSummonPosesCount() {
//        return 1;
//    }
    
    @Override
    protected void summonPose(float animationFactor, int poseVariant) {
//        switch (poseVariant) {
//        case 0:
            setSummonPoseRotationAngle(head, -0.5236F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftArm, 0.0F, 0.0F, -0.5236F, animationFactor);
            setSummonPoseRotationAngle(leftForeArm, -0.5236F, 0.0F, 1.3963F, animationFactor);
            setSummonPoseRotationAngle(rightArm, -1.2217F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftLeg, -1.8326F, 0.0F, 0.2618F, animationFactor);
            setSummonPoseRotationAngle(leftLowerLeg, 1.8326F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightLeg, 0.0F, 0.0F, -0.2618F, animationFactor);
            setSummonPoseRotationAngle(rightLowerLeg, 1.5708F, 0.0F, 0.0F, animationFactor);
//            break;
//        }
    }
    
    @Override
    protected void rangedAttackPose(HierophantGreenEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
        entity.setYBodyRot(entity.yRot);
        setRotationAngle(leftArm, -1.5708F, 0.0F, -1.309F);
        setRotationAngle(leftForeArm, -0.9163F, 0.0F, 0.0F);
        setRotationAngle(rightArm, -1.5708F, -0.6109F, 1.309F);
        setRotationAngle(rightForeArm, 0.7854F, 3.1416F, 0.0F);
        setRotationAngle(leftLeg, 0.0F, 0.0F, -0.7418F);
        setRotationAngle(leftLowerLeg, 0.0F, 0.0F, 2.0944F);
        setRotationAngle(rightLeg, 0.0F, 0.0F, 0.2618F);
    }
    
    @Override
    protected void specialAbilityPose(HierophantGreenEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
        super.specialAbilityPose(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation, phase);
        entity.setYBodyRot(entity.yRot);
        ModelRenderer arm = getArm(entity.getMainArm());
        arm.xRot = -1.5708F;
    }
}