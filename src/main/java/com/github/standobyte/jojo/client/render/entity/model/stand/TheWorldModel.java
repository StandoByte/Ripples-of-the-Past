package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.TheWorldTSHeavyAttack;
import com.github.standobyte.jojo.client.render.MeshModelBox;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.stands.TheWorldEntity;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

//Made with Blockbench 4.8.3


public class TheWorldModel extends HumanoidStandModel<TheWorldEntity> {
    ModelRenderer heartLargeAbdomen;

    public TheWorldModel() {
        super();

        ModelRenderer headpiece;
        ModelRenderer slopeNew;
        ModelRenderer faceRightNew;
        ModelRenderer faceLeftNew;
        ModelRenderer leftCable;
        ModelRenderer rightCable;
        ModelRenderer heartSmallHead;
        ModelRenderer smallHeartCube4;
        ModelRenderer smallHeartCube5;
        ModelRenderer smallHeartCube6;
        ModelRenderer beltLeft;
        ModelRenderer beltRight;
        ModelRenderer strapLeft;
        ModelRenderer strapRight;
        ModelRenderer largeHeartCube1;
        ModelRenderer largeHeartCube2;
        ModelRenderer largeHeartCube3;
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
        ModelRenderer heartRightLeg;
        ModelRenderer heartCube10;
        ModelRenderer heartCube11;
        ModelRenderer heartCube12;
        ModelRenderer heartLeftLeg;
        ModelRenderer heartCube7;
        ModelRenderer heartCube8;
        ModelRenderer heartCube9;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headpiece = new ModelRenderer(this);
        headpiece.setPos(0.0F, -2.0F, -5.3F);
        head.addChild(headpiece);
        

        slopeNew = new ModelRenderer(this);
        slopeNew.setPos(0.0F, -6.4F, 1.3F);
        headpiece.addChild(slopeNew);
        
        new MeshModelBox.Builder(true, this)
            .startFace(Direction.EAST)
                .withVertex(    4.2,       3,     8.4,      30,  22)
                .withVertex(    4.2,    -4.4,     8.4,      30,  28)
                .withVertex(    4.2,       0,       0,      38,  22)
                .withVertex(    4.2,    -6.4,       0,      38,  28)
            .createFace()
            .startFace(Direction.WEST)
                .withVertex(   -4.2,       3,     8.4,      54,  22)
                .withVertex(   -4.2,       0,       0,      46,  22)
                .withVertex(   -4.2,    -4.4,     8.4,      54,  28)
                .withVertex(   -4.2,    -6.4,       0,      46,  28)
            .createFace()
            .startFaceCalcNormal()
                .withVertex(    4.2,       3,     8.4,      38,  14)
                .withVertex(    4.2,       0,       0,      38,  22)
                .withVertex(   -4.2,       3,     8.4,      46,  14)
                .withVertex(   -4.2,       0,       0,      46,  22)
            .createFace()
            .startFaceCalcNormal()
                .withVertex(    4.2,    -4.4,     8.4,      46,  22)
                .withVertex(   -4.2,    -4.4,     8.4,      54,  22)
                .withVertex(    4.2,    -6.4,       0,      46,  14)
                .withVertex(   -4.2,    -6.4,       0,      54,  14)
            .createFace()
            .startFace(Direction.SOUTH)
                .withVertex(    4.2,       3,     8.4,      62,  22)
                .withVertex(   -4.2,       3,     8.4,      54,  22)
                .withVertex(    4.2,    -4.4,     8.4,      62,  28)
                .withVertex(   -4.2,    -4.4,     8.4,      54,  28)
            .createFace()
        .buildCube().addCube(slopeNew);
        
        
        faceRightNew = new ModelRenderer(this);
        faceRightNew.setPos(-4.2F, -6.4F, 1.3F);
        headpiece.addChild(faceRightNew);
        
        new MeshModelBox.Builder(true, this)
            .startFaceCalcNormal()
                .withVertex(      0,       0,       0,      70,  21)
                .withVertex(-4.2005,    -0.5,   -1.31,      74,  21)
                .withVertex(-4.2005,       0,  0.0266,      74,  22)
            .createFace()
            .startFaceCalcNormal()
                .withVertex(      0,    -6.4,       0,      74,  22)
                .withVertex(-4.2005,    -6.4,  0.0266,      78,  21)
                .withVertex(-4.2005,   -6.75,   -1.31,      78,  22)
            .createFace()
//            .startFaceCalcNormal()
            .startFace(new Vector3f(-0.5f, 0, -0.86602540378f))
                .withVertex(      0,       0,      0,      70,  22)
                .withVertex(      0,    -6.4,      0,      70,  28)
                .withVertex(-4.2005,    -0.5,  -1.31,      74,  22)
                .withVertex(-4.2005,   -6.75,  -1.31,      74,  28)
            .createFace()
            .startFaceCalcNormal()
                .withVertex(-4.6173,       0, 0.0266,      75,  22)
                .withVertex(-4.2005,    -0.5,  -1.31,      74,  22)
                .withVertex(-4.2005,   -6.75,  -1.31,      74,  28)
                .withVertex(-4.6173,    -6.4, 0.0266,      75,  28)
            .createFace()
        .buildCube().addCube(faceRightNew);
        

        faceLeftNew = new ModelRenderer(this);
        faceLeftNew.setPos(4.2F, -6.4F, 1.3F);
        headpiece.addChild(faceLeftNew);
        
        new MeshModelBox.Builder(true, this)
            .startFaceCalcNormal()
                .withVertex( 4.2005,       0,  0.0266,     85,  22)
                .withVertex( 4.2005,    -0.5,   -1.31,     85,  21)
                .withVertex(      0,       0,       0,     81,  21)
            .createFace()
            .startFaceCalcNormal()
                .withVertex( 4.2005,   -6.75,   -1.31,     89,  22)
                .withVertex( 4.2005,    -6.4,  0.0266,     89,  21)
                .withVertex(      0,    -6.4,       0,     85,  22)
            .createFace()
//            .startFaceCalcNormal()
            .startFace(new Vector3f(0.5f, 0, -0.86602540378f))
                .withVertex( 4.2005,    -0.5,   -1.31,     85,  22)
                .withVertex( 4.2005,   -6.75,   -1.31,     85,  28)
                .withVertex(      0,       0,       0,     81,  22)
                .withVertex(      0,    -6.4,       0,     81,  28)
            .createFace()
            .startFaceCalcNormal()
                .withVertex( 4.6173,       0,  0.0266,     85,  22)
                .withVertex( 4.6173,    -6.4,  0.0266,     85,  28)
                .withVertex( 4.2005,   -6.75,   -1.31,     86,  28)
                .withVertex( 4.2005,    -0.5,   -1.31,     86,  22)
            .createFace()
        .buildCube().addCube(faceLeftNew);
        

        leftCable = new ModelRenderer(this);
        leftCable.setPos(1.25F, -3.3F, 0.25F);
        head.addChild(leftCable);
        setRotationAngle(leftCable, 0.0873F, 0.1309F, -1.2217F);
        leftCable.texOffs(13, 16).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 5.0F, 0.0F, true);
        leftCable.texOffs(13, 25).addBox(-0.5F, 1.0F, 1.0F, 1.0F, 2.0F, 3.0F, 0.0F, true);

        rightCable = new ModelRenderer(this);
        rightCable.setPos(-1.25F, -3.3F, 0.25F);
        head.addChild(rightCable);
        setRotationAngle(rightCable, 0.0873F, -0.1309F, 1.2217F);
        rightCable.texOffs(0, 16).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 5.0F, 0.0F, false);
        rightCable.texOffs(0, 25).addBox(-0.5F, 1.0F, 1.0F, 1.0F, 2.0F, 3.0F, 0.0F, false);

        heartSmallHead = new ModelRenderer(this);
        heartSmallHead.setPos(0.0F, 0.55F, -4.0F);
        head.addChild(heartSmallHead);
        

        smallHeartCube4 = new ModelRenderer(this);
        smallHeartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartSmallHead.addChild(smallHeartCube4);
        setRotationAngle(smallHeartCube4, 0.0F, 0.0F, -0.7854F);
        smallHeartCube4.texOffs(4, 4).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube5 = new ModelRenderer(this);
        smallHeartCube5.setPos(0.3F, -0.3F, 0.0F);
        heartSmallHead.addChild(smallHeartCube5);
        setRotationAngle(smallHeartCube5, 0.0F, 0.0F, -0.7854F);
        smallHeartCube5.texOffs(4, 6).addBox(0.175F, -1.0F, -0.5F, 0.925F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube6 = new ModelRenderer(this);
        smallHeartCube6.setPos(-0.3F, -0.3F, 0.0F);
        heartSmallHead.addChild(smallHeartCube6);
        setRotationAngle(smallHeartCube6, 0.0F, 0.0F, -0.7854F);
        smallHeartCube6.texOffs(0, 6).addBox(0.0F, -1.1F, -0.5F, 1.0F, 0.925F, 1.0F, -0.2F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        

        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        body.addChild(upperPart);
        

        torso = new ModelRenderer(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);
        torso.texOffs(0, 64).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        torso.texOffs(0, 48).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.1F, false);
        torso.texOffs(20, 64).addBox(-3.5F, 1.1F, -2.0F, 7.0F, 3.0F, 1.0F, 0.4F, false);
        torso.texOffs(24, 73).addBox(-2.5F, 4.0F, -2.3F, 5.0F, 6.0F, 1.0F, 0.0F, false);
        torso.texOffs(9, 80).addBox(0.6F, 1.0F, 2.0F, 2.0F, 5.0F, 2.0F, 0.1F, true);
        torso.texOffs(0, 80).addBox(-2.6F, 1.0F, 2.0F, 2.0F, 5.0F, 2.0F, 0.1F, false);

        beltRight = new ModelRenderer(this);
        beltRight.setPos(-2.0F, 10.35F, 0.0F);
        torso.addChild(beltRight);
        setRotationAngle(beltRight, 0.0F, 0.0F, 0.1309F);
        beltRight.texOffs(64, 74).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F, 0.13F, false);

        beltLeft = new ModelRenderer(this);
        beltLeft.setPos(2.0F, 10.35F, 0.0F);
        torso.addChild(beltLeft);
        setRotationAngle(beltLeft, 0.0F, 0.0F, -0.1309F);
        beltLeft.texOffs(80, 74).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F, 0.13F, false);

        strapLeft = new ModelRenderer(this);
        strapLeft.setPos(2.65F, 10.0F, 0.0F);
        torso.addChild(strapLeft);
        setRotationAngle(strapLeft, 0.0F, 0.0F, 0.0611F);
        strapLeft.texOffs(50, 65).addBox(-1.35F, -10.1F, -2.5F, 2.0F, 10.0F, 5.0F, 0.0F, true);

        strapRight = new ModelRenderer(this);
        strapRight.setPos(-2.3F, 10.0F, 0.0F);
        torso.addChild(strapRight);
        setRotationAngle(strapRight, 0.0F, 0.0F, -0.0611F);
        strapRight.texOffs(36, 65).addBox(-1.0F, -10.1F, -2.5F, 2.0F, 10.0F, 5.0F, 0.0F, false);

        heartLargeAbdomen = new ModelRenderer(this);
        heartLargeAbdomen.setPos(0.0F, 11.5F, -2.0F);
        torso.addChild(heartLargeAbdomen);
        

        largeHeartCube1 = new ModelRenderer(this);
        largeHeartCube1.setPos(0.0F, 2.0F, 0.25F);
        heartLargeAbdomen.addChild(largeHeartCube1);
        setRotationAngle(largeHeartCube1, 0.0F, 0.0F, 0.7854F);
        largeHeartCube1.texOffs(28, 81).addBox(-1.0F, -2.0F, -0.5F, 1.0F, 0.5F, 0.0F, 0.25F, false);

        largeHeartCube2 = new ModelRenderer(this);
        largeHeartCube2.setPos(0.0F, 2.0F, 0.25F);
        heartLargeAbdomen.addChild(largeHeartCube2);
        setRotationAngle(largeHeartCube2, 0.0F, 0.0F, -0.7854F);
        largeHeartCube2.texOffs(25, 81).addBox(0.0F, -2.0F, -0.5F, 1.0F, 0.5F, 0.0F, 0.25F, false);

        largeHeartCube3 = new ModelRenderer(this);
        largeHeartCube3.setPos(0.0F, 2.0F, 0.25F);
        heartLargeAbdomen.addChild(largeHeartCube3);
        setRotationAngle(largeHeartCube3, 0.0F, 0.0F, -0.7854F);
        largeHeartCube3.texOffs(22, 81).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 0.0F, 0.25F, false);

        heartSmallAbdomen = new ModelRenderer(this);
        heartSmallAbdomen.setPos(0.0F, 11.3F, -2.05F);
        torso.addChild(heartSmallAbdomen);
        

        smallHeartCube1 = new ModelRenderer(this);
        smallHeartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube1);
        setRotationAngle(smallHeartCube1, 0.0F, 0.0F, -0.7854F);
        smallHeartCube1.texOffs(17, 82).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube2 = new ModelRenderer(this);
        smallHeartCube2.setPos(0.3F, -0.3F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube2);
        setRotationAngle(smallHeartCube2, 0.0F, 0.0F, -0.7854F);
        smallHeartCube2.texOffs(17, 80).addBox(0.175F, -1.0F, -0.5F, 0.825F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube3 = new ModelRenderer(this);
        smallHeartCube3.setPos(-0.3F, -0.3F, 0.0F);
        heartSmallAbdomen.addChild(smallHeartCube3);
        setRotationAngle(smallHeartCube3, 0.0F, 0.0F, -0.7854F);
        smallHeartCube3.texOffs(17, 85).addBox(0.0F, -1.0F, -0.5F, 1.0F, 0.825F, 1.0F, -0.2F, false);

        leftArm = convertLimb(new ModelRenderer(this));
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);
        leftArm.texOffs(32, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        leftArm.texOffs(53, 95).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, 0.1F, true);
        leftArm.texOffs(48, 110).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 3.0F, 4.0F, 0.25F, true);
        leftArm.texOffs(48, 105).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.075F, true);

        heartLeftArm = new ModelRenderer(this);
        heartLeftArm.setPos(0.0F, 3.8F, 1.8F);
        leftArm.addChild(heartLeftArm);
        

        heartCube4 = new ModelRenderer(this);
        heartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartLeftArm.addChild(heartCube4);
        setRotationAngle(heartCube4, 0.0F, 0.0F, -0.7854F);
        heartCube4.texOffs(48, 119).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube5 = new ModelRenderer(this);
        heartCube5.setPos(0.5F, -0.5F, 0.0F);
        heartLeftArm.addChild(heartCube5);
        setRotationAngle(heartCube5, 0.0F, 0.0F, -0.7854F);
        heartCube5.texOffs(44, 120).addBox(0.19F, -1.0F, -0.5F, 0.81F, 1.0F, 1.0F, -0.05F, false);

        heartCube6 = new ModelRenderer(this);
        heartCube6.setPos(-0.5F, -0.5F, 0.0F);
        heartLeftArm.addChild(heartCube6);
        setRotationAngle(heartCube6, 0.0F, 0.0F, -0.7854F);
        heartCube6.texOffs(48, 121).addBox(0.0F, -1.0F, -0.5F, 1.0F, 0.81F, 1.0F, -0.05F, false);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftArmJoint);
        leftArmJoint.texOffs(32, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftForeArm);
        leftForeArm.texOffs(32, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, true);
        leftForeArm.texOffs(48, 105).addBox(-2.0F, 3.1F, -2.0F, 4.0F, 1.0F, 4.0F, 0.075F, true);
        leftForeArm.texOffs(48, 119).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 4.0F, 4.0F, 0.15F, true);
        leftForeArm.texOffs(32, 96).addBox(0.9F, 2.7F, -1.5F, 2.0F, 3.0F, 3.0F, -0.6F, true);
        leftForeArm.texOffs(42, 97).addBox(1.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, true);

        rightArm = convertLimb(new ModelRenderer(this));
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);
        rightArm.texOffs(0, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        rightArm.texOffs(18, 87).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, 0.1F, false);
        rightArm.texOffs(16, 110).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 3.0F, 4.0F, 0.25F, false);
        rightArm.texOffs(16, 105).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.075F, false);

        heartRightArm = new ModelRenderer(this);
        heartRightArm.setPos(0.0F, 3.8F, 1.8F);
        rightArm.addChild(heartRightArm);
        

        heartCube1 = new ModelRenderer(this);
        heartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartRightArm.addChild(heartCube1);
        setRotationAngle(heartCube1, 0.0F, 0.0F, -0.7854F);
        heartCube1.texOffs(16, 119).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube2 = new ModelRenderer(this);
        heartCube2.setPos(0.5F, -0.5F, 0.0F);
        heartRightArm.addChild(heartCube2);
        setRotationAngle(heartCube2, 0.0F, 0.0F, -0.7854F);
        heartCube2.texOffs(12, 120).addBox(0.19F, -1.0F, -0.5F, 0.81F, 1.0F, 1.0F, -0.05F, false);

        heartCube3 = new ModelRenderer(this);
        heartCube3.setPos(-0.5F, -0.5F, 0.0F);
        heartRightArm.addChild(heartCube3);
        setRotationAngle(heartCube3, 0.0F, 0.0F, -0.7854F);
        heartCube3.texOffs(16, 121).addBox(0.0F, -1.0F, -0.5F, 1.0F, 0.81F, 1.0F, -0.05F, false);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightArmJoint);
        rightArmJoint.texOffs(0, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightForeArm);
        rightForeArm.texOffs(0, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        rightForeArm.texOffs(16, 105).addBox(-2.0F, 3.1F, -2.0F, 4.0F, 1.0F, 4.0F, 0.075F, false);
        rightForeArm.texOffs(16, 119).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 4.0F, 4.0F, 0.15F, false);
        rightForeArm.texOffs(0, 96).addBox(-2.9F, 2.7F, -1.5F, 2.0F, 3.0F, 3.0F, -0.6F, false);
        rightForeArm.texOffs(10, 97).addBox(-2.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, false);

        leftLeg = convertLimb(new ModelRenderer(this));
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        body.addChild(leftLeg);
        leftLeg.texOffs(96, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        leftLeg.texOffs(112, 99).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, 0.2F, false);
        leftLeg.texOffs(112, 108).addBox(1.8F, -1.75F, -1.5F, 1.0F, 3.0F, 3.0F, 0.0F, true);

        heartRightLeg = new ModelRenderer(this);
        heartRightLeg.setPos(0.0F, 6.0F, -1.8F);
        leftLeg.addChild(heartRightLeg);
        

        heartCube10 = new ModelRenderer(this);
        heartCube10.setPos(0.0F, 0.05F, 0.0F);
        heartRightLeg.addChild(heartCube10);
        setRotationAngle(heartCube10, 0.0F, 0.0F, -0.7854F);
        heartCube10.texOffs(96, 120).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube11 = new ModelRenderer(this);
        heartCube11.setPos(0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube11);
        setRotationAngle(heartCube11, 0.0F, 0.0F, -0.7854F);
        heartCube11.texOffs(92, 120).addBox(0.19F, -1.05F, -0.5F, 0.81F, 1.0F, 1.0F, -0.05F, false);

        heartCube12 = new ModelRenderer(this);
        heartCube12.setPos(-0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube12);
        setRotationAngle(heartCube12, 0.0F, 0.0F, -0.7854F);
        heartCube12.texOffs(92, 118).addBox(0.0F, -1.05F, -0.5F, 1.0F, 0.81F, 1.0F, -0.05F, false);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLegJoint);
        leftLegJoint.texOffs(96, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);
        leftLowerLeg.texOffs(96, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        leftLowerLeg.texOffs(112, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.2F, false);

        rightLeg = convertLimb(new ModelRenderer(this));
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        body.addChild(rightLeg);
        rightLeg.texOffs(64, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        rightLeg.texOffs(80, 99).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, 0.2F, false);
        rightLeg.texOffs(80, 108).addBox(-2.8F, -1.25F, -1.5F, 1.0F, 3.0F, 3.0F, 0.0F, false);

        heartLeftLeg = new ModelRenderer(this);
        heartLeftLeg.setPos(0.0F, 6.0F, -1.8F);
        rightLeg.addChild(heartLeftLeg);
        

        heartCube7 = new ModelRenderer(this);
        heartCube7.setPos(0.0F, 0.05F, 0.0F);
        heartLeftLeg.addChild(heartCube7);
        setRotationAngle(heartCube7, 0.0F, 0.0F, -0.7854F);
        heartCube7.texOffs(64, 120).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube8 = new ModelRenderer(this);
        heartCube8.setPos(0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube8);
        setRotationAngle(heartCube8, 0.0F, 0.0F, -0.7854F);
        heartCube8.texOffs(60, 120).addBox(0.19F, -1.05F, -0.5F, 0.81F, 1.0F, 1.0F, -0.05F, false);

        heartCube9 = new ModelRenderer(this);
        heartCube9.setPos(-0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube9);
        setRotationAngle(heartCube9, 0.0F, 0.0F, -0.7854F);
        heartCube9.texOffs(60, 118).addBox(0.0F, -1.05F, -0.5F, 1.0F, 0.81F, 1.0F, -0.05F, false);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLegJoint);
        rightLegJoint.texOffs(64, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);
        rightLowerLeg.texOffs(64, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        rightLowerLeg.texOffs(80, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.2F, false);
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
//            new RotationAngle[] {
//                    new RotationAngle(leftArm, 0.0F, -0.5236F, -0.8727F),
//                    new RotationAngle(leftForeArm, 0.0F, 1.1781F, 0.9599F),
//                    new RotationAngle(rightArm, 0.0F, 0.5236F, 1.309F),
//                    new RotationAngle(rightForeArm, 0.0F, -1.1781F, -0.9599F),
//                    new RotationAngle(leftLeg, 0.0F, 0.0F, -0.1745F),
//                    new RotationAngle(rightLeg, -0.4363F, 0.0F, 0.1745F),
//                    new RotationAngle(rightLowerLeg, 1.0472F, 0.0F, 0.0F)
//            },
            new RotationAngle[] {
                    new RotationAngle(head, 0.0F, 1.3963F, 0.0F),
                    new RotationAngle(body, 0.0F, 1.8326F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 0.0F, -0.6981F),
                    new RotationAngle(leftForeArm, 0.0F, 0.0F, 1.3963F),
                    new RotationAngle(rightArm, -1.8326F, -0.3491F, 0.0F),
                    new RotationAngle(rightForeArm, -0.5236F, 0.7854F, -0.7854F),
                    new RotationAngle(leftLeg, 0.2182F, 0.0F, 0.0F),
                    new RotationAngle(rightLeg, -0.2182F, 0.0F, 0.0F),
                    new RotationAngle(rightLowerLeg, 0.4363F, 0.0F, 0.0F)
            }
        };
    }
    
    @Override
    protected void initActionPoses() {
        ModelPose<TheWorldEntity> heavyPunchPose1 = new ModelPose<>(new RotationAngle[] {
                new RotationAngle(leftArm, 0.0F, 0.0F, -0.5236F),
                new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.5672F),
                new RotationAngle(rightArm, 0.2182F, 0.8727F, 1.3963F),
                new RotationAngle(rightForeArm, -1.1345F, -0.3927F, -1.5708F),
                new RotationAngle(leftLeg, 0.0F, 0.0436F, -0.1309F),
                new RotationAngle(rightLeg, 0.2618F, 0.2618F, 0.1309F)
        });
        ModelPose<TheWorldEntity> heavyPunchPose2 = new ModelPose<>(new RotationAngle[] {
                new RotationAngle(head, -0.2182F, 0.0F, 0.0F), 
                new RotationAngle(body, 0.2182F, 0.2618F, 0.0F),
                new RotationAngle(upperPart, 0.0F, 0.1745F, 0.0F),
                new RotationAngle(leftArm, 0.0F, 0.0F, -0.5236F),
                new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.5672F),
                new RotationAngle(rightArm, 0.6981F, 1.0036F, 2.0071F),
                new RotationAngle(rightForeArm, -0.7854F, -0.2618F, -1.5708F),
                new RotationAngle(leftLeg, 0.1745F, 0.0F, 0.0F),
                new RotationAngle(rightLeg, 0.2618F, 0.2618F, 0.1309F)
        });
        ModelPose<TheWorldEntity> heavyPunchPose3 = new ModelPose<>(new RotationAngle[] {
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
        ModelPose<TheWorldEntity> heavyPunchPose4 = new ModelPose<>(new RotationAngle[] {
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
        actionAnim.put(StandPose.HEAVY_ATTACK, new PosedActionAnimation.Builder<TheWorldEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<>(heavyPunchPose1, heavyPunchPose2).setEasing(pr -> Math.max(pr * 3F - 2F, 0F)))
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransition<>(heavyPunchPose2, heavyPunchPose3))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<>(heavyPunchPose3)
                        .addPose(0.5F, heavyPunchPose3)
                        .addPose(0.7F, heavyPunchPose4)
                        .addPose(0.8F, heavyPunchPose4)
                        .build(idlePose))
                .build(idlePose));
        
        ModelPose<TheWorldEntity> kickPose1 = new ModelPose<>(new RotationAngle[] {
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
        ModelPose<TheWorldEntity> kickPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -11.25F, 230F, 0F), 
                RotationAngle.fromDegrees(body, 0F, 230F, 0F),
                RotationAngle.fromDegrees(leftArm, 0F, 0F, -45F),
                RotationAngle.fromDegrees(leftForeArm, 0F, 0F, 22.5F),
                RotationAngle.fromDegrees(rightArm, -25.28F, 17.5F, -17.5F),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, -35F),
                RotationAngle.fromDegrees(rightLeg, -15F, 0F, 30F),
                RotationAngle.fromDegrees(rightLowerLeg, 75F, 0F, 0F)
        });
        ModelPose<TheWorldEntity> kickPose3 = new ModelPose<>(new RotationAngle[] {
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
        actionAnim.put(StandPose.HEAVY_ATTACK_FINISHER, new PosedActionAnimation.Builder<TheWorldEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<>(idlePose)
                        .addPose(0.5F, kickPose1)
                        .addPose(0.75F, kickPose2)
                        .build(kickPose3))
                .build(idlePose));

        ModelPose<TheWorldEntity> punchPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0.0F, 0.0F, 0F), 
                RotationAngle.fromDegrees(body, 40F, 45F, 0F),
                RotationAngle.fromDegrees(upperPart, 0F, 0F, 0F),
                RotationAngle.fromDegrees(leftArm, -25F, -35F, -65F),
                RotationAngle.fromDegrees(leftForeArm, -50F, 0F, -17.5F),
                RotationAngle.fromDegrees(rightArm, 0F, 85F, 90F),
                RotationAngle.fromDegrees(rightForeArm, -33.0896F, -17.2568F, -105.9398F),
                RotationAngle.fromDegrees(leftLeg, -90F, 0F, -10F),
                RotationAngle.fromDegrees(leftLowerLeg, 80F, 0F, 0F),
                RotationAngle.fromDegrees(rightLeg, 0F, 0F, 10F),
                RotationAngle.fromDegrees(rightLowerLeg, 0F, 0F, 0F)
        });
        ModelPose<TheWorldEntity> punchPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0.0F, 22.5F, 0.0F), 
                RotationAngle.fromDegrees(body, 40F, 60F, 0F),
                RotationAngle.fromDegrees(leftArm, -10F, -35F, -65F),
                RotationAngle.fromDegrees(leftForeArm, -40F, -5F, -17.5F),
                RotationAngle.fromDegrees(rightArm, 105F, 67.5F, 180F),
                RotationAngle.fromDegrees(rightForeArm, -50.4689F, -48.3643F, -73.6793F),
                RotationAngle.fromDegrees(rightLeg, 10F, 0F, 10F)
        });
        ModelPose<TheWorldEntity> punchPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0.0F, 0.0F, 0F), 
                RotationAngle.fromDegrees(body, 30F, -30F, 0F),
                RotationAngle.fromDegrees(leftArm, 5F, -25F, -45F),
                RotationAngle.fromDegrees(leftForeArm, -60F, 20F, 30F),
                RotationAngle.fromDegrees(rightArm, -49.308F, 34.8829F, 81.833F),
                RotationAngle.fromDegrees(rightForeArm, -37.8852F, -9.0337F, -12.7323F),
                RotationAngle.fromDegrees(leftLeg, -120F, 0F, -10F),
                RotationAngle.fromDegrees(leftLowerLeg, 120F, 0F, 0F),
                RotationAngle.fromDegrees(rightLeg, 0F, 0F, 10F),
                RotationAngle.fromDegrees(rightLowerLeg, 45F, 0F, 0F)
        });
        actionAnim.put(TheWorldTSHeavyAttack.TS_PUNCH_POSE, new PosedActionAnimation.Builder<TheWorldEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<>(punchPose1, punchPose2))
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransition<>(punchPose2, punchPose3))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<>(punchPose3)
                        .addPose(0.5F, punchPose3)
                        .build(idlePose))
                .build(idlePose));
        
        super.initActionPoses();
    }
    
    @Override
    protected ModelPose<TheWorldEntity> initIdlePose() {
        return new ModelPose<>(new RotationAngle[] {
                new RotationAngle(upperPart, 0, 0, 0),
                RotationAngle.fromDegrees(body,             -10.3453,   14.7669,    -2.664),
                RotationAngle.fromDegrees(leftArm,          12.2616,    9.8242,     -25.0612),
                RotationAngle.fromDegrees(leftForeArm,      -12.0884,   2.8002,     18.5715),
                RotationAngle.fromDegrees(rightArm,         3.0667,     -8.1272,    29.6295),
                RotationAngle.fromDegrees(rightForeArm,     -34.6,      -4.5795,    -21.7089),
                RotationAngle.fromDegrees(leftLeg,          20.2151,    -12.1546,   -12.2229),
                RotationAngle.fromDegrees(leftLowerLeg,     7.5,        0,          0),
                RotationAngle.fromDegrees(rightLeg,         6.8105,     -11.2924,   10.6414),
                RotationAngle.fromDegrees(rightLowerLeg,    20,         0,          0),
                RotationAngle.fromDegrees(heartLargeAbdomen,-10.2443,   3.4058,     0)
        });
    }

    @Override
    protected ModelPose<TheWorldEntity> initIdlePose2Loop() {
        return new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(leftArm,          18.8684,    10.8486,    -30.036),
                RotationAngle.fromDegrees(leftForeArm,      -19.3046,   3.8358,     23.4714),
                RotationAngle.fromDegrees(rightArm,         8.0667,     -8.1272,    29.6295),
                RotationAngle.fromDegrees(rightForeArm,     -44.4869,   -2.7976,    -23.4661),
                RotationAngle.fromDegrees(leftLeg,          22.7151,    -12.1546,   -12.2229),
                RotationAngle.fromDegrees(leftLowerLeg,     12.5,        0,         0),
                RotationAngle.fromDegrees(rightLeg,         9.2581,     -8.8099,    10.9413),
                RotationAngle.fromDegrees(rightLowerLeg,    25,         0,          0),
                RotationAngle.fromDegrees(heartLargeAbdomen,-8.2352,    2.4217,     0.178)
        });
    }
    
    @Override
    public void poseIdleLoop(TheWorldEntity entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide swingingHand) {
        super.poseIdleLoop(entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
    }
    
}