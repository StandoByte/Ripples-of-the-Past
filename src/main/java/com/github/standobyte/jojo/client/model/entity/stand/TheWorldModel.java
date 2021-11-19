package com.github.standobyte.jojo.client.model.entity.stand;

import com.github.standobyte.jojo.entity.stand.TheWorldEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class TheWorldModel extends HumanoidStandModel<TheWorldEntity> {
    private final ModelRenderer slope;
    private final ModelRenderer bone1;
    private final ModelRenderer bone2;
    private final ModelRenderer leftCable;
    private final ModelRenderer rightCable;
    private final ModelRenderer heartSmallHead;
    private final ModelRenderer smallHeartCube4;
    private final ModelRenderer smallHeartCube5;
    private final ModelRenderer smallHeartCube6;
    private final ModelRenderer heartLargeAbdomen;
    private final ModelRenderer largeHeartCube1;
    private final ModelRenderer largeHeartCube2;
    private final ModelRenderer heartSmallAbdomen;
    private final ModelRenderer smallHeartCube1;
    private final ModelRenderer smallHeartCube2;
    private final ModelRenderer smallHeartCube3;
    private final ModelRenderer heartLeftArm;
    private final ModelRenderer heartCube4;
    private final ModelRenderer heartCube5;
    private final ModelRenderer heartCube6;
    private final ModelRenderer heartRightArm;
    private final ModelRenderer heartCube1;
    private final ModelRenderer heartCube2;
    private final ModelRenderer heartCube3;
    private final ModelRenderer heartLeftLeg;
    private final ModelRenderer heartCube10;
    private final ModelRenderer heartCube11;
    private final ModelRenderer heartCube12;
    private final ModelRenderer heartRightLeg;
    private final ModelRenderer heartCube7;
    private final ModelRenderer heartCube8;
    private final ModelRenderer heartCube9;
    
    public TheWorldModel() {
        this(64, 64);
    }
    
    public TheWorldModel(int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);

//        head.texOffs(48, 0).addBox(-2.5F, -2.75F, -5.5F, 5.0F, 5.0F, 3.0F, -1.75F, false);


        heartSmallHead = new ModelRenderer(this);
        heartSmallHead.setPos(0.0F, 0.55F, -4.0F);
        head.addChild(heartSmallHead);

        smallHeartCube4 = new ModelRenderer(this);
        smallHeartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartSmallHead.addChild(smallHeartCube4);
        setRotationAngle(smallHeartCube4, 0.0F, 0.0F, -0.7854F);
        smallHeartCube4.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube5 = new ModelRenderer(this);
        smallHeartCube5.setPos(0.3F, -0.3F, 0.0F);
        heartSmallHead.addChild(smallHeartCube5);
        setRotationAngle(smallHeartCube5, 0.0F, 0.0F, -0.7854F);
        smallHeartCube5.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube6 = new ModelRenderer(this);
        smallHeartCube6.setPos(-0.3F, -0.3F, 0.0F);
        heartSmallHead.addChild(smallHeartCube6);
        setRotationAngle(smallHeartCube6, 0.0F, 0.0F, -0.7854F);
        smallHeartCube6.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
        
        
        slope = new ModelRenderer(this);
        slope.setPos(0.0F, -6.3F, -0.5F);
        head.addChild(slope);
        setRotationAngle(slope, 0.2618F, 0.0F, 0.0F);
        slope.texOffs(0, 16).addBox(-4.0F, -3.0F, -4.5F, 8.0F, 6.0F, 9.0F, 0.1F, false);

        bone1 = new ModelRenderer(this);
        bone1.setPos(-4.1F, -3.1F, -4.6F);
        slope.addChild(bone1);
        setRotationAngle(bone1, 0.0F, 0.219F, 0.0F);
        bone1.texOffs(25, 16).addBox(0.1F, 0.1F, 0.1F, 4.0F, 6.0F, 1.0F, 0.1F, false);

        bone2 = new ModelRenderer(this);
        bone2.setPos(4.1F, -3.1F, -4.6F);
        slope.addChild(bone2);
        setRotationAngle(bone2, 0.0F, -0.219F, 0.0F);
        bone2.texOffs(35, 16).addBox(-4.1F, 0.1F, 0.1F, 4.0F, 6.0F, 1.0F, 0.1F, false);
        
        
        leftCable = new ModelRenderer(this);
        leftCable.setPos(1.25F, -3.3F, 0.25F);
        head.addChild(leftCable);
        setRotationAngle(leftCable, 0.0873F, 0.1309F, -1.1345F);
        leftCable.texOffs(52, 29).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 5.0F, 0.0F, true);

        rightCable = new ModelRenderer(this);
        rightCable.setPos(-1.25F, -3.3F, 0.25F);
        head.addChild(rightCable);
        setRotationAngle(rightCable, 0.0873F, -0.1309F, 1.2217F);
        rightCable.texOffs(52, 29).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 5.0F, 0.0F, false);

        torso.texOffs(18, 31).addBox(-3.5F, 1.1F, -2.0F, 7.0F, 3.0F, 1.0F, 0.4F, false);
        torso.texOffs(0, 31).addBox(-2.5F, 4.0F, -2.3F, 5.0F, 6.0F, 1.0F, 0.0F, false);
        torso.texOffs(52, 11).addBox(2.0F, -0.75F, -2.75F, 1.0F, 11.0F, 5.0F, 0.0F, true);
        torso.texOffs(52, 11).addBox(-3.0F, -0.75F, -2.75F, 1.0F, 11.0F, 5.0F, 0.0F, false);
        torso.texOffs(24, 0).addBox(0.5F, 1.0F, 2.0F, 2.0F, 6.0F, 2.0F, 0.0F, true);
        torso.texOffs(24, 0).addBox(-2.5F, 1.0F, 2.0F, 2.0F, 6.0F, 2.0F, 0.0F, false);
//        torso.texOffs(48, 0).addBox(-2.5F, 10.0F, -3.5F, 5.0F, 5.0F, 3.0F, -1.0F, false);
//        torso.texOffs(48, 0).addBox(-2.5F, 8.0F, -3.65F, 5.0F, 5.0F, 3.0F, -2.0F, false);

        
        heartLargeAbdomen = new ModelRenderer(this);
        heartLargeAbdomen.setPos(0.0F, 13.5F, -1.75F);
        torso.addChild(heartLargeAbdomen);

        largeHeartCube1 = new ModelRenderer(this);
        largeHeartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartLargeAbdomen.addChild(largeHeartCube1);
        setRotationAngle(largeHeartCube1, 0.0F, 0.0F, 0.7854F);
        largeHeartCube1.texOffs(60, 8).addBox(-1.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.25F, false);

        largeHeartCube2 = new ModelRenderer(this);
        largeHeartCube2.setPos(0.0F, 0.0F, 0.0F);
        heartLargeAbdomen.addChild(largeHeartCube2);
        setRotationAngle(largeHeartCube2, 0.0F, 0.0F, -0.7854F);
        largeHeartCube2.texOffs(60, 8).addBox(0.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.25F, false);

        
        heartSmallAbdomen = new ModelRenderer(this);
        heartSmallAbdomen.setPos(0.0F, 11.3F, -2.05F);
        torso.addChild(heartSmallAbdomen);
        
        smallHeartCube1 = new ModelRenderer(this);
        smallHeartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube1);
        setRotationAngle(smallHeartCube1, 0.0F, 0.0F, -0.7854F);
        smallHeartCube1.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube2 = new ModelRenderer(this);
        smallHeartCube2.setPos(0.3F, -0.3F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube2);
        setRotationAngle(smallHeartCube2, 0.0F, 0.0F, -0.7854F);
        smallHeartCube2.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube3 = new ModelRenderer(this);
        smallHeartCube3.setPos(-0.3F, -0.3F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube3);
        setRotationAngle(smallHeartCube3, 0.0F, 0.0F, -0.7854F);
        smallHeartCube3.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        
        leftArm.texOffs(34, 25).addBox(0.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.25F, true);
        
//        leftArmJoint.texOffs(48, 0).addBox(-2.5F, -3.5F, 0.7F, 5.0F, 5.0F, 3.0F, -1.25F, false);

        
        heartLeftArm = new ModelRenderer(this);
        heartLeftArm.setPos(0.0F, -0.2F, 1.8F);
        leftForeArm.addChild(heartLeftArm);
        
        heartCube4 = new ModelRenderer(this);
        heartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartLeftArm.addChild(heartCube4);
        setRotationAngle(heartCube4, 0.0F, 0.0F, -0.7854F);
        heartCube4.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube5 = new ModelRenderer(this);
        heartCube5.setPos(0.5F, -0.5F, 0.0F);
        heartLeftArm.addChild(heartCube5);
        setRotationAngle(heartCube5, 0.0F, 0.0F, -0.7854F);
        heartCube5.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube6 = new ModelRenderer(this);
        heartCube6.setPos(-0.5F, -0.5F, 0.0F);
        heartLeftArm.addChild(heartCube6);
        setRotationAngle(heartCube6, 0.0F, 0.0F, -0.7854F);
        heartCube6.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);
        

        leftForeArm.texOffs(36, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, -0.251F, true);
        leftForeArm.texOffs(0, 19).addBox(1.6F, 2.5F, -1.5F, 1.0F, 3.0F, 3.0F, -0.4F, true);
        leftForeArm.texOffs(12, 33).addBox(1.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, true);

        rightArm.texOffs(34, 25).addBox(-2.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.25F, false);

//        rightArmJoint.texOffs(48, 0).addBox(-2.5F, -3.5F, 0.7F, 5.0F, 5.0F, 3.0F, -1.25F, false);

        
        heartRightArm = new ModelRenderer(this);
        heartRightArm.setPos(0.0F, -0.2F, 1.8F);
        rightArmJoint.addChild(heartRightArm);

        heartCube1 = new ModelRenderer(this);
        heartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartRightArm.addChild(heartCube1);
        setRotationAngle(heartCube1, 0.0F, 0.0F, -0.7854F);
        heartCube1.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube2 = new ModelRenderer(this);
        heartCube2.setPos(0.5F, -0.5F, 0.0F);
        heartRightArm.addChild(heartCube2);
        setRotationAngle(heartCube2, 0.0F, 0.0F, -0.7854F);
        heartCube2.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube3 = new ModelRenderer(this);
        heartCube3.setPos(-0.5F, -0.5F, 0.0F);
        heartRightArm.addChild(heartCube3);
        setRotationAngle(heartCube3, 0.0F, 0.0F, -0.7854F);
        heartCube3.texOffs(60, 8).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);
        

        rightForeArm.texOffs(36, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, -0.251F, false);
        rightForeArm.texOffs(0, 19).addBox(-2.6F, 2.5F, -1.5F, 1.0F, 3.0F, 3.0F, -0.4F, false);
        rightForeArm.texOffs(12, 33).addBox(-2.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, false);

        leftLeg.texOffs(12, 54).addBox(1.6F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, 0.0F, true);
        
//        leftLegJoint.texOffs(48, 0).addBox(-2.4F, -2.5F, -3.5F, 5.0F, 5.0F, 3.0F, -1.25F, false);


        heartLeftLeg = new ModelRenderer(this);
        heartLeftLeg.setPos(0.0F, 0.0F, -1.8F);
        leftLegJoint.addChild(heartLeftLeg);

        heartCube10 = new ModelRenderer(this);
        heartCube10.setPos(0.0F, 0.05F, 0.0F);
        heartLeftLeg.addChild(heartCube10);
        setRotationAngle(heartCube10, 0.0F, 0.0F, -0.7854F);
        heartCube10.texOffs(60, 8).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube11 = new ModelRenderer(this);
        heartCube11.setPos(0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube11);
        setRotationAngle(heartCube11, 0.0F, 0.0F, -0.7854F);
        heartCube11.texOffs(60, 8).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube12 = new ModelRenderer(this);
        heartCube12.setPos(-0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube12);
        setRotationAngle(heartCube12, 0.0F, 0.0F, -0.7854F);
        heartCube12.texOffs(60, 8).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);
        

        rightLeg.texOffs(12, 54).addBox(-2.6F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);

//        rightLegJoint.texOffs(48, 0).addBox(-2.6F, -2.5F, -3.5F, 5.0F, 5.0F, 3.0F, -1.25F, false);


        heartRightLeg = new ModelRenderer(this);
        heartRightLeg.setPos(0.0F, 0.0F, -1.8F);
        rightLegJoint.addChild(heartRightLeg);

        heartCube7 = new ModelRenderer(this);
        heartCube7.setPos(0.0F, 0.05F, 0.0F);
        heartRightLeg.addChild(heartCube7);
        setRotationAngle(heartCube7, 0.0F, 0.0F, -0.7854F);
        heartCube7.texOffs(60, 8).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube8 = new ModelRenderer(this);
        heartCube8.setPos(0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube8);
        setRotationAngle(heartCube8, 0.0F, 0.0F, -0.7854F);
        heartCube8.texOffs(60, 8).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube9 = new ModelRenderer(this);
        heartCube9.setPos(-0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube9);
        setRotationAngle(heartCube9, 0.0F, 0.0F, -0.7854F);
        heartCube9.texOffs(60, 8).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);
    }
    
    @Override
    protected int getSummonPosesCount() {
        return 4;
    }
    
    @Override
    protected void summonPose(float animationFactor, int poseVariant) {
        switch (poseVariant) {
        case 0:
            setSummonPoseRotationAngle(head, 0.0F, -0.3491F, -0.0873F, animationFactor);
            setSummonPoseRotationAngle(body, 0.2618F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftArm, -0.2618F, -0.7854F, -1.8326F, animationFactor);
            setSummonPoseRotationAngle(leftForeArm, -0.7854F, 0.0F, 0.0F,  animationFactor);
            setSummonPoseRotationAngle(rightArm, 0.5236F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightForeArm, 0.0F, 3.1416F, 2.1817F, animationFactor);
            setSummonPoseRotationAngle(leftLeg, -1.5708F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftLowerLeg, 2.0944F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightLeg, -1.1345F, 0.2618F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightLowerLeg, 0.5236F, 0.0F, 0.0F, animationFactor);
            break;
        case 1:
            setSummonPoseRotationAngle(head, -0.2618F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(body, 0.4363F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftArm, -0.8727F, -0.2618F, -2.0944F, animationFactor);
            setSummonPoseRotationAngle(leftForeArm, -0.6981F, -0.8727F, 0.0873F, animationFactor);
            setSummonPoseRotationAngle(rightArm, 0.0F, 1.309F, 1.3963F, animationFactor);
            setSummonPoseRotationAngle(rightForeArm, -1.0472F, -0.3491F, -1.6581F, animationFactor);
            setSummonPoseRotationAngle(leftLeg, 1.0472F, 0.0F, -0.2618F, animationFactor);
            setSummonPoseRotationAngle(rightLeg, -0.7854F, 0.0F, 0.1745F, animationFactor);
            setSummonPoseRotationAngle(rightLowerLeg, 0.7854F, 0.2618F, 0.0F, animationFactor);
            break;
        case 2:
            setSummonPoseRotationAngle(leftArm, 0.0F, -0.5236F, -0.8727F, animationFactor);
            setSummonPoseRotationAngle(leftForeArm, 0.0F, 1.1781F, 0.9599F, animationFactor);
            setSummonPoseRotationAngle(rightArm, 0.0F, 0.5236F, 1.309F, animationFactor);
            setSummonPoseRotationAngle(rightForeArm, 0.0F, -1.1781F, -0.9599F, animationFactor);
            setSummonPoseRotationAngle(leftLeg, 0.0F, 0.0F, -0.1745F, animationFactor);
            setSummonPoseRotationAngle(rightLeg, -0.4363F, 0.0F, 0.1745F, animationFactor);
            setSummonPoseRotationAngle(rightLowerLeg, 1.0472F, 0.0F, 0.0F, animationFactor);
            break;
        case 3:
            setSummonPoseRotationAngle(body, 0.6981F, 0.5236F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(leftArm, -0.4363F, -0.6109F, -1.1345F, animationFactor);
            setSummonPoseRotationAngle(leftForeArm, -1.0472F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightArm, 0.0F, 1.4835F, 1.5708F, animationFactor);
            setSummonPoseRotationAngle(rightForeArm, -0.8727F, -0.3927F, -1.5708F, animationFactor);
            setSummonPoseRotationAngle(leftLeg, -1.5708F, 0.0F, -0.1745F, animationFactor);
            setSummonPoseRotationAngle(leftLowerLeg, 1.3963F, 0.0F, 0.0F, animationFactor);
            setSummonPoseRotationAngle(rightLeg, 0.0F, 0.0F, 0.1745F, animationFactor);
            break;
        }
    }
}