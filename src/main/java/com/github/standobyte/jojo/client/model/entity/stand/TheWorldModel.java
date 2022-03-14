package com.github.standobyte.jojo.client.model.entity.stand;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.client.model.pose.ModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.model.pose.RotationAngle;
import com.github.standobyte.jojo.client.model.pose.StandActionAnimation;
import com.github.standobyte.jojo.entity.stand.stands.TheWorldEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class TheWorldModel extends HumanoidStandModel<TheWorldEntity> {

    public TheWorldModel() {
        this(64, 64);
    }

    public TheWorldModel(int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);

        ModelRenderer slope;
        ModelRenderer slope2;
        ModelRenderer slopeBack;
        ModelRenderer faceRight;
        ModelRenderer faceLeft;
        ModelRenderer leftCable;
        ModelRenderer rightCable;
        ModelRenderer heartSmallHead;
        ModelRenderer smallHeartCube4;
        ModelRenderer smallHeartCube5;
        ModelRenderer smallHeartCube6;
        ModelRenderer heartLargeAbdomen;
        ModelRenderer largeHeartCube1;
        ModelRenderer largeHeartCube2;
        ModelRenderer heartSmallAbdomen;
        ModelRenderer smallHeartCube1;
        ModelRenderer smallHeartCube2;
        ModelRenderer smallHeartCube3;
        ModelRenderer heartLeftArm;
        ModelRenderer heartCube4;
        ModelRenderer heartCube5;
        ModelRenderer heartCube6;
        ModelRenderer heartRightArm;
        ModelRenderer heartCube1;
        ModelRenderer heartCube2;
        ModelRenderer heartCube3;
        ModelRenderer heartLeftLeg;
        ModelRenderer heartCube10;
        ModelRenderer heartCube11;
        ModelRenderer heartCube12;
        ModelRenderer heartRightLeg;
        ModelRenderer heartCube7;
        ModelRenderer heartCube8;
        ModelRenderer heartCube9;

        head.texOffs(0, 28).addBox(-4.0F, -3.3F, -3.8F, 8.0F, 1.0F, 1.0F, 0.2F, false);
        head.texOffs(22, 31).addBox(3.0F, -8.3F, -3.8F, 1.0F, 5.0F, 1.0F, 0.2F, true);
        head.texOffs(22, 31).addBox(-4.0F, -8.3F, -3.8F, 1.0F, 5.0F, 1.0F, 0.2F, false);


        heartSmallHead = new ModelRenderer(this);
        heartSmallHead.setPos(0.0F, 0.55F, -4.0F);
        head.addChild(heartSmallHead);

        smallHeartCube4 = new ModelRenderer(this);
        smallHeartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartSmallHead.addChild(smallHeartCube4);
        setRotationAngle(smallHeartCube4, 0.0F, 0.0F, -0.7854F);
        smallHeartCube4.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube5 = new ModelRenderer(this);
        smallHeartCube5.setPos(0.3F, -0.3F, 0.0F);
        heartSmallHead.addChild(smallHeartCube5);
        setRotationAngle(smallHeartCube5, 0.0F, 0.0F, -0.7854F);
        smallHeartCube5.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube6 = new ModelRenderer(this);
        smallHeartCube6.setPos(-0.3F, -0.3F, 0.0F);
        heartSmallHead.addChild(smallHeartCube6);
        setRotationAngle(smallHeartCube6, 0.0F, 0.0F, -0.7854F);
        smallHeartCube6.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);


        slope = new ModelRenderer(this);
        slope.setPos(0.0F, -8.5F, -4.0F);
        head.addChild(slope);
        setRotationAngle(slope, 0.3578F, 0.0F, 0.0F);
        slope.texOffs(0, 16).addBox(-4.0F, 0.2F, 0.2F, 8.0F, 4.0F, 8.0F, 0.2F, false);
        slope.texOffs(0, 16).addBox(-4.2F, 0.06F, 6.35F, 1.0F, 4.0F, 3.0F, -1.0F, true);
        slope.texOffs(12, 41).addBox(-4.2F, 2.21F, 5.25F, 1.0F, 4.0F, 3.0F, -1.0F, true);
        slope.texOffs(12, 41).addBox(3.2F, 2.21F, 5.25F, 1.0F, 4.0F, 3.0F, -1.0F, false);
        slope.texOffs(0, 16).addBox(3.2F, 0.06F, 6.35F, 1.0F, 4.0F, 3.0F, -1.0F, false);

        slopeBack = new ModelRenderer(this);
        slopeBack.setPos(0.0F, 0.0F, 8.4F);
        slope.addChild(slopeBack);
        setRotationAngle(slopeBack, -0.3578F, 0.0F, 0.0F);
        slopeBack.texOffs(25, 17).addBox(-4.0F, 0.2F, 0.2F, 8.0F, 7.0F, 0.0F, 0.2F, false);

        slope2 = new ModelRenderer(this);
        slope2.setPos(0.0F, -2.1F, -2.6F);
        head.addChild(slope2);
        setRotationAngle(slope2, 0.2755F, 0.0F, 0.0F);
        slope2.texOffs(26, 24).addBox(-4.0F, -3.2F, 0.2F, 8.0F, 3.0F, 7.0F, 0.2F, false);
        slope2.texOffs(0, 28).addBox(-4.0F, -1.2F, 5.9372F, 8.0F, 1.0F, 1.0F, 0.2F, false);

        faceRight = new ModelRenderer(this);
        faceRight.setPos(-4.2F, -8.5F, -4.0F);
        head.addChild(faceRight);
        setRotationAngle(faceRight, 0.0F, 0.3023F, 0.0F);
        faceRight.texOffs(12, 30).addBox(0.2F, 0.2F, 0.2F, 4.0F, 6.0F, 1.0F, 0.2F, false);

        faceLeft = new ModelRenderer(this);
        faceLeft.setPos(4.2F, -8.5F, -4.0F);
        head.addChild(faceLeft);
        setRotationAngle(faceLeft, 0.0F, -0.3023F, 0.0F);
        faceLeft.texOffs(12, 30).addBox(-4.2F, 0.2F, 0.2F, 4.0F, 6.0F, 1.0F, 0.2F, true);


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

        torso.texOffs(9, 37).addBox(-3.5F, 1.1F, -2.0F, 7.0F, 3.0F, 1.0F, 0.4F, false);
        torso.texOffs(0, 30).addBox(-2.5F, 4.0F, -2.3F, 5.0F, 6.0F, 1.0F, 0.0F, false);
        torso.texOffs(52, 11).addBox(2.0F, -0.25F, -2.5F, 1.0F, 10.0F, 5.0F, 0.25F, true);
        torso.texOffs(52, 11).addBox(-3.0F, -0.25F, -2.5F, 1.0F, 10.0F, 5.0F, 0.25F, false);
        torso.texOffs(24, 0).addBox(0.5F, 1.0F, 2.0F, 2.0F, 6.0F, 2.0F, 0.0F, true);
        torso.texOffs(24, 0).addBox(-2.5F, 1.0F, 2.0F, 2.0F, 6.0F, 2.0F, 0.0F, false);

        heartLargeAbdomen = new ModelRenderer(this);
        heartLargeAbdomen.setPos(0.0F, 13.5F, -1.75F);
        torso.addChild(heartLargeAbdomen);

        largeHeartCube1 = new ModelRenderer(this);
        largeHeartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartLargeAbdomen.addChild(largeHeartCube1);
        setRotationAngle(largeHeartCube1, 0.0F, 0.0F, 0.7854F);
        largeHeartCube1.texOffs(48, 1).addBox(-1.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.25F, false);

        largeHeartCube2 = new ModelRenderer(this);
        largeHeartCube2.setPos(0.0F, 0.0F, 0.0F);
        heartLargeAbdomen.addChild(largeHeartCube2);
        setRotationAngle(largeHeartCube2, 0.0F, 0.0F, -0.7854F);
        largeHeartCube2.texOffs(48, 1).addBox(0.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.25F, false);


        heartSmallAbdomen = new ModelRenderer(this);
        heartSmallAbdomen.setPos(0.0F, 11.3F, -2.05F);
        torso.addChild(heartSmallAbdomen);

        smallHeartCube1 = new ModelRenderer(this);
        smallHeartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube1);
        setRotationAngle(smallHeartCube1, 0.0F, 0.0F, -0.7854F);
        smallHeartCube1.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube2 = new ModelRenderer(this);
        smallHeartCube2.setPos(0.3F, -0.3F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube2);
        setRotationAngle(smallHeartCube2, 0.0F, 0.0F, -0.7854F);
        smallHeartCube2.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube3 = new ModelRenderer(this);
        smallHeartCube3.setPos(-0.3F, -0.3F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube3);
        setRotationAngle(smallHeartCube3, 0.0F, 0.0F, -0.7854F);
        smallHeartCube3.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        leftArm.texOffs(48, 0).addBox(0.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.25F, true);

        heartLeftArm = new ModelRenderer(this);
        heartLeftArm.setPos(0.0F, -0.2F, 1.8F);
        leftForeArm.addChild(heartLeftArm);

        heartCube4 = new ModelRenderer(this);
        heartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartLeftArm.addChild(heartCube4);
        setRotationAngle(heartCube4, 0.0F, 0.0F, -0.7854F);
        heartCube4.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube5 = new ModelRenderer(this);
        heartCube5.setPos(0.5F, -0.5F, 0.0F);
        heartLeftArm.addChild(heartCube5);
        setRotationAngle(heartCube5, 0.0F, 0.0F, -0.7854F);
        heartCube5.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube6 = new ModelRenderer(this);
        heartCube6.setPos(-0.5F, -0.5F, 0.0F);
        heartLeftArm.addChild(heartCube6);
        setRotationAngle(heartCube6, 0.0F, 0.0F, -0.7854F);
        heartCube6.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);


        leftForeArm.texOffs(36, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, 0.25F, true);
        leftForeArm.texOffs(41, 18).addBox(1.0F, 2.0F, -1.5F, 2.0F, 3.0F, 3.0F, -0.4F, true);
        leftForeArm.texOffs(25, 34).addBox(1.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, true);

        rightArm.texOffs(48, 0).addBox(-2.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.25F, false);

        heartRightArm = new ModelRenderer(this);
        heartRightArm.setPos(0.0F, -0.2F, 1.8F);
        rightArmJoint.addChild(heartRightArm);

        heartCube1 = new ModelRenderer(this);
        heartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartRightArm.addChild(heartCube1);
        setRotationAngle(heartCube1, 0.0F, 0.0F, -0.7854F);
        heartCube1.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube2 = new ModelRenderer(this);
        heartCube2.setPos(0.5F, -0.5F, 0.0F);
        heartRightArm.addChild(heartCube2);
        setRotationAngle(heartCube2, 0.0F, 0.0F, -0.7854F);
        heartCube2.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube3 = new ModelRenderer(this);
        heartCube3.setPos(-0.5F, -0.5F, 0.0F);
        heartRightArm.addChild(heartCube3);
        setRotationAngle(heartCube3, 0.0F, 0.0F, -0.7854F);
        heartCube3.texOffs(48, 1).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        rightForeArm.texOffs(36, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, 0.25F, false);
        rightForeArm.texOffs(41, 18).addBox(-3.0F, 2.0F, -1.5F, 2.0F, 3.0F, 3.0F, -0.4F, false);
        rightForeArm.texOffs(25, 34).addBox(-2.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, false);

        leftLeg.texOffs(28, 42).addBox(1.6F, -1.75F, -1.5F, 1.0F, 3.0F, 3.0F, 0.0F, true);

        heartLeftLeg = new ModelRenderer(this);
        heartLeftLeg.setPos(0.0F, 0.0F, -1.8F);
        leftLegJoint.addChild(heartLeftLeg);

        heartCube10 = new ModelRenderer(this);
        heartCube10.setPos(0.0F, 0.05F, 0.0F);
        heartLeftLeg.addChild(heartCube10);
        setRotationAngle(heartCube10, 0.0F, 0.0F, -0.7854F);
        heartCube10.texOffs(48, 1).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube11 = new ModelRenderer(this);
        heartCube11.setPos(0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube11);
        setRotationAngle(heartCube11, 0.0F, 0.0F, -0.7854F);
        heartCube11.texOffs(48, 1).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube12 = new ModelRenderer(this);
        heartCube12.setPos(-0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube12);
        setRotationAngle(heartCube12, 0.0F, 0.0F, -0.7854F);
        heartCube12.texOffs(48, 1).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        rightLeg.texOffs(28, 42).addBox(-2.6F, -1.25F, -1.5F, 1.0F, 3.0F, 3.0F, 0.0F, false);

        heartRightLeg = new ModelRenderer(this);
        heartRightLeg.setPos(0.0F, 0.0F, -1.8F);
        rightLegJoint.addChild(heartRightLeg);

        heartCube7 = new ModelRenderer(this);
        heartCube7.setPos(0.0F, 0.05F, 0.0F);
        heartRightLeg.addChild(heartCube7);
        setRotationAngle(heartCube7, 0.0F, 0.0F, -0.7854F);
        heartCube7.texOffs(48, 1).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube8 = new ModelRenderer(this);
        heartCube8.setPos(0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube8);
        setRotationAngle(heartCube8, 0.0F, 0.0F, -0.7854F);
        heartCube8.texOffs(48, 1).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube9 = new ModelRenderer(this);
        heartCube9.setPos(-0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube9);
        setRotationAngle(heartCube9, 0.0F, 0.0F, -0.7854F);
        heartCube9.texOffs(48, 1).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);
    }

    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {
                    new RotationAngle(head, 0.0F, -0.3491F, -0.0873F),
                    new RotationAngle(body, 0.2618F, 0.0F, 0.0F),
                    new RotationAngle(leftArm, -0.2618F, -0.7854F, -1.8326F),
                    new RotationAngle(leftForeArm, -0.7854F, 0.0F, 0.0F),
                    new RotationAngle(rightArm, 0.5236F, 0.0F, 0.0F),
                    new RotationAngle(rightForeArm, 0.0F, 3.1416F, 2.1817F),
                    new RotationAngle(leftLeg, -1.5708F, 0.0F, 0.0F),
                    new RotationAngle(leftLowerLeg, 2.0944F, 0.0F, 0.0F),
                    new RotationAngle(rightLeg, -1.1345F, 0.2618F, 0.0F),
                    new RotationAngle(rightLowerLeg, 0.5236F, 0.0F, 0.0F)
            },
            new RotationAngle[] {
                    new RotationAngle(head, -0.2618F, 0.0F, 0.0F),
                    new RotationAngle(body, 0.4363F, 0.0F, 0.0F),
                    new RotationAngle(leftArm, -0.8727F, -0.2618F, -2.0944F),
                    new RotationAngle(leftForeArm, -0.6981F, -0.8727F, 0.0873F),
                    new RotationAngle(rightArm, 0.0F, 1.309F, 1.3963F),
                    new RotationAngle(rightForeArm, -1.0472F, -0.3491F, -1.6581F),
                    new RotationAngle(leftLeg, 1.0472F, 0.0F, -0.2618F),
                    new RotationAngle(rightLeg, -0.7854F, 0.0F, 0.1745F),
                    new RotationAngle(rightLowerLeg, 0.7854F, 0.2618F, 0.0F)
            },
            new RotationAngle[] {
                    new RotationAngle(leftArm, 0.0F, -0.5236F, -0.8727F),
                    new RotationAngle(leftForeArm, 0.0F, 1.1781F, 0.9599F),
                    new RotationAngle(rightArm, 0.0F, 0.5236F, 1.309F),
                    new RotationAngle(rightForeArm, 0.0F, -1.1781F, -0.9599F),
                    new RotationAngle(leftLeg, 0.0F, 0.0F, -0.1745F),
                    new RotationAngle(rightLeg, -0.4363F, 0.0F, 0.1745F),
                    new RotationAngle(rightLowerLeg, 1.0472F, 0.0F, 0.0F)
            },
            new RotationAngle[] {
                    new RotationAngle(body, 0.6981F, 0.5236F, 0.0F),
                    new RotationAngle(leftArm, -0.4363F, -0.6109F, -1.1345F),
                    new RotationAngle(leftForeArm, -1.0472F, 0.0F, 0.0F),
                    new RotationAngle(rightArm, 0.0F, 1.4835F, 1.5708F),
                    new RotationAngle(rightForeArm, -0.8727F, -0.3927F, -1.5708F),
                    new RotationAngle(leftLeg, -1.5708F, 0.0F, -0.1745F),
                    new RotationAngle(leftLowerLeg, 1.3963F, 0.0F, 0.0F),
                    new RotationAngle(rightLeg, 0.0F, 0.0F, 0.1745F)
            }
        };
    }

    @Override
    protected StandActionAnimation<TheWorldEntity> initHeavyAttackAnim(boolean combo) {
        if (combo) {
            ModelPose<TheWorldEntity> pose1 = new ModelPose<TheWorldEntity>(new RotationAngle[] {
                    RotationAngle.fromDegrees(head, -7.5F, 153.33F, 0F), 
                    RotationAngle.fromDegrees(body, 0F, 153.33F, 0F),
                    RotationAngle.fromDegrees(leftArm, 0F, 0F, -30F),
                    RotationAngle.fromDegrees(leftForeArm, 0F, 0F, 15F),
                    RotationAngle.fromDegrees(rightArm, -32.5F, 22.5F, -22.5F),
                    RotationAngle.fromDegrees(rightForeArm, 0F, 0F, -45F),
                    RotationAngle.fromDegrees(leftLeg, 0F, 0F, 0F),
                    RotationAngle.fromDegrees(rightLeg, -10F, 0F, 20F),
                    RotationAngle.fromDegrees(rightLowerLeg, 50F, 0F, 0F)
            });
            ModelPose<TheWorldEntity> pose2 = new ModelPose<TheWorldEntity>(new RotationAngle[] {
                    RotationAngle.fromDegrees(head, -11.25F, 230F, 0F), 
                    RotationAngle.fromDegrees(body, 0F, 230F, 0F),
                    RotationAngle.fromDegrees(leftArm, 0F, 0F, -45F),
                    RotationAngle.fromDegrees(leftForeArm, 0F, 0F, 22.5F),
                    RotationAngle.fromDegrees(rightArm, -25.28F, 17.5F, -17.5F),
                    RotationAngle.fromDegrees(rightForeArm, 0F, 0F, -35F),
                    RotationAngle.fromDegrees(rightLeg, -15F, 0F, 30F),
                    RotationAngle.fromDegrees(rightLowerLeg, 75F, 0F, 0F)
            });
            ModelPose<TheWorldEntity> pose3 = new ModelPose<TheWorldEntity>(new RotationAngle[] {
                    RotationAngle.fromDegrees(head, -15F, 360F, 0F), 
                    RotationAngle.fromDegrees(body, -43.00306F, 308.28988F, 36.20399F),
                    RotationAngle.fromDegrees(leftArm, 0F, 0F, -60F),
                    RotationAngle.fromDegrees(leftForeArm, 0F, 0F, 30F),
                    RotationAngle.fromDegrees(rightArm, -18.06F, 12.5F, -12.5F),
                    RotationAngle.fromDegrees(rightForeArm, 0F, 0F, -25F),
                    RotationAngle.fromDegrees(leftLeg, 15.55245F, 35.50838F, -13.04428F),
                    RotationAngle.fromDegrees(rightLeg, -30F, 0, 105F),
                    RotationAngle.fromDegrees(rightLowerLeg, 0F, 0F, 0F)
            });
            return new StandActionAnimation.Builder<TheWorldEntity>()
                    .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<TheWorldEntity>(idlePose)
                            .addPose(0.5F, pose1)
                            .addPose(0.75F, pose2)
                            .build(pose3))
                    .build(idlePose);
        }
        else { // FIXME (!!) use xRotation
            ModelPose<TheWorldEntity> pose1 = new ModelPose<TheWorldEntity>(new RotationAngle[] {
                    new RotationAngle(leftArm, 0.0F, 0.0F, -0.5236F),
                    new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.5672F),
                    new RotationAngle(rightArm, 0.2182F, 0.8727F, 1.3963F),
                    new RotationAngle(rightForeArm, -1.1345F, -0.3927F, -1.5708F),
                    new RotationAngle(leftLeg, 0.0F, 0.0436F, -0.1309F),
                    new RotationAngle(rightLeg, 0.2618F, 0.2618F, 0.1309F)
            });
            ModelPose<TheWorldEntity> pose2 = new ModelPose<TheWorldEntity>(new RotationAngle[] {
                    new RotationAngle(head, -0.2182F, 0.0F, 0.0F), // FIXME (!!) ADD head rotation
                    new RotationAngle(body, 0.2182F, 0.2618F, 0.0F),
                    new RotationAngle(upperPart, 0.0F, 0.1745F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 0.0F, -0.5236F),
                    new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.5672F),
                    new RotationAngle(rightArm, 0.6981F, 1.0036F, 2.0071F),
                    new RotationAngle(rightForeArm, -0.7854F, -0.2618F, -1.5708F),
                    new RotationAngle(leftLeg, 0.1745F, 0.0F, 0.0F),
                    new RotationAngle(rightLeg, 0.2618F, 0.2618F, 0.1309F)
            });
            ModelPose<TheWorldEntity> pose3 = new ModelPose<TheWorldEntity>(new RotationAngle[] {
                    new RotationAngle(body, 0.2182F, -0.5236F, 0.0F),
                    new RotationAngle(upperPart, 0.0F, -0.5236F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 0.4363F, -1.5708F),
                    new RotationAngle(leftForeArm, 0.0F, 0.0F, 1.1781F),
                    new RotationAngle(rightArm, -0.3927F, -0.2182F, 1.0472F),
                    new RotationAngle(rightForeArm, 0.0F, 3.1416F, 0.6981F),
                    new RotationAngle(leftLeg, -1.0472F, 0.0F, 0.0F),
                    new RotationAngle(leftLowerLeg, 1.8326F, 0.0F, 0.0F),
                    new RotationAngle(rightLeg, 0.2618F, 0.2618F, 0.1309F)
            });
            ModelPose<TheWorldEntity> pose4 = new ModelPose<TheWorldEntity>(new RotationAngle[] {
                    new RotationAngle(body, 0.2182F, -0.0873F, 0.0F),
                    new RotationAngle(upperPart, 0.0F, -0.0873F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 0.4363F, -0.7854F),
                    new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.5672F),
                    new RotationAngle(rightArm, 0.3927F, 0.0F, 0.3491F),
                    new RotationAngle(rightForeArm, -1.4399F, 0.0F, -1.309F),
                    new RotationAngle(leftLeg, -1.0472F, 0.0F, 0.0F),
                    new RotationAngle(leftLowerLeg, 1.8326F, 0.0F, 0.0F),
                    new RotationAngle(rightLeg, 0.2618F, 0.2618F, 0.1309F)
            });
            return new StandActionAnimation.Builder<TheWorldEntity>()
                    .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<TheWorldEntity>(pose1, pose2).setEasing(pr -> Math.max(pr * 3F - 2F, 0F)))
                    .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransition<TheWorldEntity>(pose2, pose3))
                    .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<TheWorldEntity>(pose3)
                            .addPose(0.5F, pose3)
                            .addPose(0.7F, pose4)
                            .addPose(0.8F, pose4)
                            .build(idlePose))
                    .build(idlePose);
        }
    }
}