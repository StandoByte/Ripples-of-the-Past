package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.client.render.MeshModelBox;
import com.github.standobyte.jojo.entity.stand.stands.TheWorldEntity;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

//Made with Blockbench 4.8.3


public class TheWorldModel extends HumanoidStandModel<TheWorldEntity> {
    ModelRenderer heartLargeAbdomen;

    public TheWorldModel() {
        super();

        ModelRenderer headpieceNew;
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

        root = new ModelRenderer(this);
        root.setPos(0.0F, 0.0F, 0.0F);
        

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        root.addChild(head);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headpieceNew = new ModelRenderer(this);
        headpieceNew.setPos(0.0F, -2.0F, -5.3F);
        head.addChild(headpieceNew);
        

        slopeNew = new ModelRenderer(this);
        slopeNew.setPos(0.0F, -6.4F, 1.3F);
        headpieceNew.addChild(slopeNew);
        
        new MeshModelBox.Builder(true, this)
            .startFace(Direction.EAST)
                .withVertex(    4.2,       3,     8.4,      30,  22)
                .withVertex(    4.2,    -4.4,     8.4,      30,  28)
                .withVertex(    4.2,    -6.4,       0,      38,  28)
                .withVertex(    4.2,       0,       0,      38,  22)
            .createFace()
            .startFace(Direction.WEST)
                .withVertex(   -4.2,       3,     8.4,      54,  22)
                .withVertex(   -4.2,       0,       0,      46,  22)
                .withVertex(   -4.2,    -6.4,       0,      46,  28)
                .withVertex(   -4.2,    -4.4,     8.4,      54,  28)
            .createFace()
            .startFaceCalcNormal()
                .withVertex(    4.2,       3,     8.4,      38,  14)
                .withVertex(    4.2,       0,       0,      38,  22)
                .withVertex(   -4.2,       0,       0,      46,  22)
                .withVertex(   -4.2,       3,     8.4,      46,  14)
            .createFace()
            .startFaceCalcNormal()
                .withVertex(    4.2,    -4.4,     8.4,      46,  22)
                .withVertex(   -4.2,    -4.4,     8.4,      54,  22)
                .withVertex(   -4.2,    -6.4,       0,      54,  14)
                .withVertex(    4.2,    -6.4,       0,      46,  14)
            .createFace()
            .startFace(Direction.SOUTH)
                .withVertex(    4.2,       3,     8.4,      62,  22)
                .withVertex(   -4.2,       3,     8.4,      54,  22)
                .withVertex(   -4.2,    -4.4,     8.4,      54,  28)
                .withVertex(    4.2,    -4.4,     8.4,      62,  28)
            .createFace()
        .buildCube().addCube(slopeNew);
        
        
        faceRightNew = new ModelRenderer(this);
        faceRightNew.setPos(-4.2F, -6.4F, 1.3F);
        headpieceNew.addChild(faceRightNew);
        
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
                .withVertex(-4.2005,   -6.75,  -1.31,      74,  28)
                .withVertex(-4.2005,    -0.5,  -1.31,      74,  22)
            .createFace()
            .startFaceCalcNormal()
                .withVertex(-4.6173,       0, 0.0266,      75,  22)
                .withVertex(-4.2005,    -0.5,  -1.31,      74,  22)
                .withVertex(-4.6173,    -6.4, 0.0266,      75,  28)
                .withVertex(-4.2005,   -6.75,  -1.31,      74,  28)
            .createFace()
        .buildCube().addCube(faceRightNew);
        

        faceLeftNew = new ModelRenderer(this);
        faceLeftNew.setPos(4.2F, -6.4F, 1.3F);
        headpieceNew.addChild(faceLeftNew);
        
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
                .withVertex(      0,    -6.4,       0,     81,  28)
                .withVertex(      0,       0,       0,     81,  22)
            .createFace()
            .startFaceCalcNormal()
                .withVertex( 4.6173,       0,  0.0266,     85,  22)
                .withVertex( 4.6173,    -6.4,  0.0266,     85,  28)
                .withVertex( 4.2005,    -0.5,   -1.31,     86,  22)
                .withVertex( 4.2005,   -6.75,   -1.31,     86,  28)
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
        root.addChild(body);
        

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

        leftArmXRot = new ModelRenderer(this);
        leftArmXRot.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArmXRot);
        

        leftArmBone = new ModelRenderer(this);
        leftArmBone.setPos(0.0F, 0.0F, 0.0F);
        leftArmXRot.addChild(leftArmBone);
        leftArmBone.texOffs(32, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        leftArmBone.texOffs(53, 95).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, 0.1F, true);
        leftArmBone.texOffs(48, 110).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 3.0F, 4.0F, 0.25F, true);
        leftArmBone.texOffs(48, 105).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.075F, true);

        heartLeftArm = new ModelRenderer(this);
        heartLeftArm.setPos(0.0F, 3.8F, 1.8F);
        leftArmBone.addChild(heartLeftArm);
        

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
        leftArmBone.addChild(leftArmJoint);
        leftArmJoint.texOffs(32, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArmBone.addChild(leftForeArm);
        leftForeArm.texOffs(32, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, true);
        leftForeArm.texOffs(48, 105).addBox(-2.0F, 3.1F, -2.0F, 4.0F, 1.0F, 4.0F, 0.075F, true);
        leftForeArm.texOffs(48, 119).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 4.0F, 4.0F, 0.15F, true);
        leftForeArm.texOffs(32, 96).addBox(0.9F, 2.7F, -1.5F, 2.0F, 3.0F, 3.0F, -0.6F, true);
        leftForeArm.texOffs(42, 97).addBox(1.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, true);

        rightArmXRot = new ModelRenderer(this);
        rightArmXRot.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArmXRot);
        

        rightArmBone = new ModelRenderer(this);
        rightArmBone.setPos(0.0F, 0.0F, 0.0F);
        rightArmXRot.addChild(rightArmBone);
        rightArmBone.texOffs(0, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        rightArmBone.texOffs(18, 87).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, 0.1F, false);
        rightArmBone.texOffs(16, 110).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 3.0F, 4.0F, 0.25F, false);
        rightArmBone.texOffs(16, 105).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.075F, false);

        heartRightArm = new ModelRenderer(this);
        heartRightArm.setPos(0.0F, 3.8F, 1.8F);
        rightArmBone.addChild(heartRightArm);
        

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
        rightArmBone.addChild(rightArmJoint);
        rightArmJoint.texOffs(0, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArmBone.addChild(rightForeArm);
        rightForeArm.texOffs(0, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        rightForeArm.texOffs(16, 105).addBox(-2.0F, 3.1F, -2.0F, 4.0F, 1.0F, 4.0F, 0.075F, false);
        rightForeArm.texOffs(16, 119).addBox(-2.0F, -0.4F, -2.0F, 4.0F, 4.0F, 4.0F, 0.15F, false);
        rightForeArm.texOffs(0, 96).addBox(-2.9F, 2.7F, -1.5F, 2.0F, 3.0F, 3.0F, -0.6F, false);
        rightForeArm.texOffs(10, 97).addBox(-2.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, false);

        leftLegXRot = new ModelRenderer(this);
        leftLegXRot.setPos(2.0F, 12.0F, 0.0F);
        body.addChild(leftLegXRot);
        

        leftLegBone = new ModelRenderer(this);
        leftLegBone.setPos(0.0F, 0.0F, 0.0F);
        leftLegXRot.addChild(leftLegBone);
        leftLegBone.texOffs(96, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        leftLegBone.texOffs(112, 99).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, 0.2F, false);
        leftLegBone.texOffs(112, 108).addBox(1.8F, -1.75F, -1.5F, 1.0F, 3.0F, 3.0F, 0.0F, true);

        heartRightLeg = new ModelRenderer(this);
        heartRightLeg.setPos(0.0F, 6.0F, -1.8F);
        leftLegBone.addChild(heartRightLeg);
        

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
        leftLegBone.addChild(leftLegJoint);
        leftLegJoint.texOffs(96, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLegBone.addChild(leftLowerLeg);
        leftLowerLeg.texOffs(96, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        leftLowerLeg.texOffs(112, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.2F, false);

        rightLegXRot = new ModelRenderer(this);
        rightLegXRot.setPos(-2.0F, 12.0F, 0.0F);
        body.addChild(rightLegXRot);
        

        rightLegBone = new ModelRenderer(this);
        rightLegBone.setPos(0.0F, 0.0F, 0.0F);
        rightLegXRot.addChild(rightLegBone);
        rightLegBone.texOffs(64, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        rightLegBone.texOffs(80, 99).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, 0.2F, false);
        rightLegBone.texOffs(80, 108).addBox(-2.8F, -1.25F, -1.5F, 1.0F, 3.0F, 3.0F, 0.0F, false);

        heartLeftLeg = new ModelRenderer(this);
        heartLeftLeg.setPos(0.0F, 6.0F, -1.8F);
        rightLegBone.addChild(heartLeftLeg);
        

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
        rightLegBone.addChild(rightLegJoint);
        rightLegJoint.texOffs(64, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLegBone.addChild(rightLowerLeg);
        rightLowerLeg.texOffs(64, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        rightLowerLeg.texOffs(80, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.2F, false);
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
        putNamedModelPart("heartLargeAbdomen", heartLargeAbdomen);
    }
    
}