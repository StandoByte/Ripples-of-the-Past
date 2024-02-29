package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.TheWorldTSHeavyAttack;
import com.github.standobyte.jojo.client.render.CustomVerticesModelBox;
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

// Made with Blockbench 3.9.2


public class TheWorldModelOld extends HumanoidStandModel<TheWorldEntity> {

    @SuppressWarnings("deprecation")
    public TheWorldModelOld() {
        super();
        
        addHumanoidBaseBoxes(null);

        ModelRenderer headpiece;
        ModelRenderer slopeOld;
        ModelRenderer slopeOld2;
        ModelRenderer slopeOldBack;
        ModelRenderer slopeNew;
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


        heartSmallHead = new ModelRenderer(this);
        heartSmallHead.setPos(0.0F, 0.55F, -4.0F);
        head.addChild(heartSmallHead);

        smallHeartCube4 = new ModelRenderer(this);
        smallHeartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartSmallHead.addChild(smallHeartCube4);
        setRotationAngle(smallHeartCube4, 0.0F, 0.0F, -0.7854F);
        smallHeartCube4.texOffs(4, 6).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube5 = new ModelRenderer(this);
        smallHeartCube5.setPos(0.3F, -0.3F, 0.0F);
        heartSmallHead.addChild(smallHeartCube5);
        setRotationAngle(smallHeartCube5, 0.0F, 0.0F, -0.7854F);
        smallHeartCube5.texOffs(4, 6).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube6 = new ModelRenderer(this);
        smallHeartCube6.setPos(-0.3F, -0.3F, 0.0F);
        heartSmallHead.addChild(smallHeartCube6);
        setRotationAngle(smallHeartCube6, 0.0F, 0.0F, -0.7854F);
        smallHeartCube6.texOffs(4, 6).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
        
        
        headpiece = new ModelRenderer(this);
        headpiece.setPos(0.0F, -2.0F, -5.3F);
        head.addChild(headpiece);
        headpiece.texOffs(82, 2).addBox(-4.0F, -1.2F, 1.5F, 8.0F, 1.0F, 1.0F, 0.2F, false);
        headpiece.texOffs(90, 22).addBox(3.0F, -6.2F, 1.5F, 1.0F, 5.0F, 1.0F, 0.2F, true);
        headpiece.texOffs(65, 22).addBox(-4.0F, -6.2F, 1.5F, 1.0F, 5.0F, 1.0F, 0.2F, false);

        slopeOld = new ModelRenderer(this);
        slopeOld.setPos(0.0F, -6.4F, 1.3F);
        headpiece.addChild(slopeOld);
        setRotationAngle(slopeOld, 0.3578F, 0.0F, 0.0F);
        slopeOld.texOffs(64, 9).addBox(-4.0F, 0.2F, 0.2F, 8.0F, 4.0F, 8.0F, 0.2F, false);
        slopeOld.texOffs(56, 7).addBox(-4.2F, 0.06F, 6.35F, 1.0F, 4.0F, 3.0F, -1.0F, true);
        slopeOld.texOffs(56, 14).addBox(-4.2F, 2.21F, 5.25F, 1.0F, 4.0F, 3.0F, -1.0F, true);
        slopeOld.texOffs(96, 14).addBox(3.2F, 2.21F, 5.25F, 1.0F, 4.0F, 3.0F, -1.0F, false);
        slopeOld.texOffs(96, 7).addBox(3.2F, 0.06F, 6.35F, 1.0F, 4.0F, 3.0F, -1.0F, false);

        slopeOldBack = new ModelRenderer(this);
        slopeOldBack.setPos(0.0F, 0.0F, 8.4F);
        slopeOld.addChild(slopeOldBack);
        setRotationAngle(slopeOldBack, -0.3578F, 0.0F, 0.0F);
        slopeOldBack.texOffs(65, 1).addBox(-4.0F, 0.2F, 0.2F, 8.0F, 7.0F, 0.0F, 0.2F, false);

        slopeOld2 = new ModelRenderer(this);
        slopeOld2.setPos(0.0F, 0.0F, 2.7F);
        headpiece.addChild(slopeOld2);
        setRotationAngle(slopeOld2, 0.2755F, 0.0F, 0.0F);
        slopeOld2.texOffs(98, 14).addBox(-4.0F, -3.2F, 0.2F, 8.0F, 3.0F, 7.0F, 0.2F, false);
        slopeOld2.texOffs(82, 0).addBox(-4.0F, -1.2F, 5.9372F, 8.0F, 1.0F, 1.0F, 0.2F, false);

        slopeNew = new ModelRenderer(this);
        slopeNew.setPos(0.0F, -6.4F, 1.3F);
        headpiece.addChild(slopeNew);
//        SlopeModelBox.addSlopeBox(slopeNew, 
//                30, 14, 
//                -4.0F, 0.2F, -2.8F, 0.2F, 
//                8.0F, 6.0F, 7.0F, 8.0F, 
//                0.2F, false, texWidth, texHeight);
        new CustomVerticesModelBox.Builder(true)
        .withVertex(true,  true,  false,  4.2F,     0,     0)
        .withVertex(false, true,  false, -4.2F,     0,     0)
        .withVertex(true,  false, false,  4.2F, -6.4F,     0)
        .withVertex(false, false, false, -4.2F, -6.4F,     0)
        .withVertex(true,  true,   true,  4.2F,     3,  8.4F)
        .withVertex(false, true,   true, -4.2F,     3,  8.4F)
        .withVertex(true,  false,  true,  4.2F, -4.4F,  8.4F)
        .withVertex(false, false,  true, -4.2F, -4.4F,  8.4F)
        .withUvFace(Direction.UP,    38, 14,  8,  8)
        .withUvFace(Direction.DOWN,  46, 14,  8,  8)
        .withUvFace(Direction.EAST,  30, 22,  8,  6)
        .withUvFace(Direction.NORTH, 38, 22,  8,  6)
        .withUvFace(Direction.WEST,  46, 22,  8,  6)
        .withUvFace(Direction.SOUTH, 54, 22,  8,  6)
        .addCube(slopeNew, texWidth, texHeight, false);

        faceRight = new ModelRenderer(this);
        faceRight.setPos(-4.2F, -6.4F, 1.3F);
        headpiece.addChild(faceRight);
        setRotationAngle(faceRight, 0.0F, 0.3023F, 0.0F);
//        faceRight.texOffs(69, 21).addBox(0.2F, 0.2F, 0.2F, 4.0F, 6.0F, 1.0F, 0.2F, false);
        new CustomVerticesModelBox.Builder(true)
        .withVertex(true,  true,  false,     0,      0,     0)
        .withVertex(false, true,  false, -4.4F,  -0.5F,     0)
        .withVertex(true,  false, false,     0,  -6.4F,     0)
        .withVertex(false, false, false, -4.4F, -6.75F,     0)
        .withVertex(true,  true,   true,     0,      0,     0.0001F)
        .withVertex(false, true,   true, -4.4F,      0,  1.4F)
        .withVertex(true,  false,  true,     0, -6.15F,  1.1F)
        .withVertex(false, false,  true, -4.4F,  -6.4F,  1.4F)
        .withUvFace(Direction.UP,    70, 21,  4,  1)
        .withUvFace(Direction.DOWN,  74, 21,  4,  1)
        .withUvFace(Direction.EAST,  69, 22,  1,  6)
        .withUvFace(Direction.NORTH, 70, 22,  4,  6)
        .withUvFace(Direction.WEST,  74, 22,  1,  6)
        .withUvFace(Direction.SOUTH, 75, 22,  4,  6)
        .addCube(faceRight, texWidth, texHeight, false);

        faceLeft = new ModelRenderer(this);
        faceLeft.setPos(4.2F, -6.4F, 1.3F);
        headpiece.addChild(faceLeft);
        setRotationAngle(faceLeft, 0.0F, -0.3023F, 0.0F);
//        faceLeft.texOffs(80, 21).addBox(-4.2F, 0.2F, 0.2F, 4.0F, 6.0F, 1.0F, 0.2F, true);
        new CustomVerticesModelBox.Builder(true)
        .withVertex(true,  true,  false,  4.4F,  -0.5F,     0)
        .withVertex(false, true,  false,     0,      0,     0)
        .withVertex(true,  false, false,  4.4F, -6.75F,     0)
        .withVertex(false, false, false,     0,  -6.4F,     0)
        .withVertex(true,  true,   true,  4.4F,      0,  1.4F)
        .withVertex(false, true,   true,     0,      0,     0.0001F)
        .withVertex(true,  false,  true,  4.4F,  -6.4F,  1.4F)
        .withVertex(false, false,  true,     0, -6.15F,  1.1F)
        .withUvFace(Direction.UP,    81, 21,  4,  1)
        .withUvFace(Direction.DOWN,  85, 21,  4,  1)
        .withUvFace(Direction.EAST,  80, 22,  1,  6)
        .withUvFace(Direction.NORTH, 81, 22,  4,  6)
        .withUvFace(Direction.WEST,  74, 22,  1,  6)
        .withUvFace(Direction.SOUTH, 86, 22,  4,  6)
        .addCube(faceLeft, texWidth, texHeight, true);

        leftCable = new ModelRenderer(this);
        leftCable.setPos(1.25F, -3.3F, 0.25F);
        head.addChild(leftCable);
        setRotationAngle(leftCable, 0.0873F, 0.1309F, -1.1345F);
        leftCable.texOffs(13, 16).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 5.0F, 0.0F, true);
        leftCable.texOffs(13, 25).addBox(-0.5F, 1.0F, 1.0F, 1.0F, 2.0F, 3.0F, 0.0F, true);

        rightCable = new ModelRenderer(this);
        rightCable.setPos(-1.25F, -3.3F, 0.25F);
        head.addChild(rightCable);
        setRotationAngle(rightCable, 0.0873F, -0.1309F, 1.2217F);
        rightCable.texOffs(0, 16).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 4.0F, 5.0F, 0.0F, false);
        rightCable.texOffs(0, 25).addBox(-0.5F, 1.0F, 1.0F, 1.0F, 2.0F, 3.0F, 0.0F, false);

        torso.texOffs(20, 64).addBox(-3.5F, 1.1F, -2.0F, 7.0F, 3.0F, 1.0F, 0.4F, false);
        torso.texOffs(24, 73).addBox(-2.5F, 4.0F, -2.3F, 5.0F, 6.0F, 1.0F, 0.0F, false);
        torso.texOffs(49, 65).addBox(2.7F, 0.0F, -2.5F, 1.0F, 10.0F, 5.0F, 0.25F, true);
        torso.texOffs(36, 65).addBox(-3.7F, 0.0F, -2.5F, 1.0F, 10.0F, 5.0F, 0.25F, false);
        torso.texOffs(9, 80).addBox(0.5F, 1.0F, 2.0F, 2.0F, 6.0F, 2.0F, 0.0F, true);
        torso.texOffs(0, 80).addBox(-2.5F, 1.0F, 2.0F, 2.0F, 6.0F, 2.0F, 0.0F, false);

        
        heartLargeAbdomen = new ModelRenderer(this);
        heartLargeAbdomen.setPos(0.0F, 13.5F, -1.75F);
        torso.addChild(heartLargeAbdomen);

        largeHeartCube1 = new ModelRenderer(this);
        largeHeartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartLargeAbdomen.addChild(largeHeartCube1);
        setRotationAngle(largeHeartCube1, 0.0F, 0.0F, 0.7854F);
        largeHeartCube1.texOffs(21, 80).addBox(-1.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.25F, false);

        largeHeartCube2 = new ModelRenderer(this);
        largeHeartCube2.setPos(0.0F, 0.0F, 0.0F);
        heartLargeAbdomen.addChild(largeHeartCube2);
        setRotationAngle(largeHeartCube2, 0.0F, 0.0F, -0.7854F);
        largeHeartCube2.texOffs(21, 80).addBox(0.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.25F, false);


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

        leftArm.texOffs(48, 110).addBox(0.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.25F, true);

        heartLeftArm = new ModelRenderer(this);
        heartLeftArm.setPos(0.0F, 3.8F, 1.8F);
        leftArm.addChild(heartLeftArm);

        heartCube4 = new ModelRenderer(this);
        heartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartLeftArm.addChild(heartCube4);
        setRotationAngle(heartCube4, 0.0F, 0.0F, -0.7854F);
        heartCube4.texOffs(48, 121).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube5 = new ModelRenderer(this);
        heartCube5.setPos(0.5F, -0.5F, 0.0F);
        heartLeftArm.addChild(heartCube5);
        setRotationAngle(heartCube5, 0.0F, 0.0F, -0.7854F);
        heartCube5.texOffs(48, 121).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube6 = new ModelRenderer(this);
        heartCube6.setPos(-0.5F, -0.5F, 0.0F);
        heartLeftArm.addChild(heartCube6);
        setRotationAngle(heartCube6, 0.0F, 0.0F, -0.7854F);
        heartCube6.texOffs(48, 121).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);


        leftForeArm.texOffs(48, 119).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, 0.25F, true);
        leftForeArm.texOffs(32, 96).addBox(1.0F, 2.0F, -1.5F, 2.0F, 3.0F, 3.0F, -0.4F, true);
        leftForeArm.texOffs(42, 97).addBox(1.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, true);

        rightArm.texOffs(16, 110).addBox(-2.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.25F, false);

        heartRightArm = new ModelRenderer(this);
        heartRightArm.setPos(0.0F, 3.8F, 1.8F);
        rightArm.addChild(heartRightArm);

        heartCube1 = new ModelRenderer(this);
        heartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartRightArm.addChild(heartCube1);
        setRotationAngle(heartCube1, 0.0F, 0.0F, -0.7854F);
        heartCube1.texOffs(16, 121).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube2 = new ModelRenderer(this);
        heartCube2.setPos(0.5F, -0.5F, 0.0F);
        heartRightArm.addChild(heartCube2);
        setRotationAngle(heartCube2, 0.0F, 0.0F, -0.7854F);
        heartCube2.texOffs(16, 121).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube3 = new ModelRenderer(this);
        heartCube3.setPos(-0.5F, -0.5F, 0.0F);
        heartRightArm.addChild(heartCube3);
        setRotationAngle(heartCube3, 0.0F, 0.0F, -0.7854F);
        heartCube3.texOffs(16, 121).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);


        rightForeArm.texOffs(16, 119).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F, 0.25F, false);
        rightForeArm.texOffs(0, 96).addBox(-3.0F, 2.0F, -1.5F, 2.0F, 3.0F, 3.0F, -0.4F, false);
        rightForeArm.texOffs(10, 97).addBox(-2.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, false);

        leftLeg.texOffs(112, 108).addBox(1.6F, -1.75F, -1.5F, 1.0F, 3.0F, 3.0F, 0.0F, true);

        heartLeftLeg = new ModelRenderer(this);
        heartLeftLeg.setPos(0.0F, 6.0F, -1.8F);
        leftLeg.addChild(heartLeftLeg);

        heartCube10 = new ModelRenderer(this);
        heartCube10.setPos(0.0F, 0.05F, 0.0F);
        heartLeftLeg.addChild(heartCube10);
        setRotationAngle(heartCube10, 0.0F, 0.0F, -0.7854F);
        heartCube10.texOffs(96, 120).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube11 = new ModelRenderer(this);
        heartCube11.setPos(0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube11);
        setRotationAngle(heartCube11, 0.0F, 0.0F, -0.7854F);
        heartCube11.texOffs(96, 120).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube12 = new ModelRenderer(this);
        heartCube12.setPos(-0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube12);
        setRotationAngle(heartCube12, 0.0F, 0.0F, -0.7854F);
        heartCube12.texOffs(96, 120).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);
        
        rightLeg.texOffs(80, 108).addBox(-2.6F, -1.25F, -1.5F, 1.0F, 3.0F, 3.0F, 0.0F, false);

        heartRightLeg = new ModelRenderer(this);
        heartRightLeg.setPos(0.0F, 6.0F, -1.8F);
        rightLeg.addChild(heartRightLeg);
        
        heartCube7 = new ModelRenderer(this);
        heartCube7.setPos(0.0F, 0.05F, 0.0F);
        heartRightLeg.addChild(heartCube7);
        setRotationAngle(heartCube7, 0.0F, 0.0F, -0.7854F);
        heartCube7.texOffs(64, 120).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube8 = new ModelRenderer(this);
        heartCube8.setPos(0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube8);
        setRotationAngle(heartCube8, 0.0F, 0.0F, -0.7854F);
        heartCube8.texOffs(64, 120).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube9 = new ModelRenderer(this);
        heartCube9.setPos(-0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube9);
        setRotationAngle(heartCube9, 0.0F, 0.0F, -0.7854F);
        heartCube9.texOffs(64, 120).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);
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
                    new RotationAngle(leftArm, 0.0F, -0.5236F, -0.8727F),
                    new RotationAngle(leftForeArm, 0.0F, 1.1781F, 0.9599F),
                    new RotationAngle(rightArm, 0.0F, 0.5236F, 1.309F),
                    new RotationAngle(rightForeArm, 0.0F, -1.1781F, -0.9599F),
                    new RotationAngle(leftLeg, 0.0F, 0.0F, -0.1745F),
                    new RotationAngle(rightLeg, -0.4363F, 0.0F, 0.1745F),
                    new RotationAngle(rightLowerLeg, 1.0472F, 0.0F, 0.0F)
            },
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
                new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                new RotationAngle(body, 0.0F, 0.2618F, 0.0F),
                new RotationAngle(leftArm, -0.1309F, 0.0F, -0.5236F),
                new RotationAngle(leftForeArm, -0.3491F, 0.0873F, 0.3927F),
                new RotationAngle(rightArm, 0.2618F, 0.0F, 0.0873F),
                new RotationAngle(rightForeArm, -1.0472F, 0.2618F, -0.1745F),
                new RotationAngle(leftLeg, -0.2182F, -0.2182F, 0.0F),
                new RotationAngle(leftLowerLeg, 0.4363F, 0.0F, 0.0F),
                new RotationAngle(rightLeg, 0.0873F, -0.1745F, 0.1309F),
                new RotationAngle(rightLowerLeg, 0.2618F, 0.0F, 0.0F)
        });
    }

    @Override
    protected ModelPose<TheWorldEntity> initIdlePose2Loop() {
        return new ModelPose<>(new RotationAngle[] {
                new RotationAngle(leftArm, -0.1309F, 0.0F, -0.6109F),
                new RotationAngle(leftForeArm, -0.3491F, 0.0873F, 0.5236F),
                new RotationAngle(rightArm, 0.3491F, 0.0F, 0.0873F),
                new RotationAngle(rightForeArm, -1.1345F, 0.2182F, -0.2182F)
        });
    }
    
    @Override
    public void poseIdleLoop(TheWorldEntity entity, float ticks, float yRotOffsetRad, float xRotRad, HandSide swingingHand) {
        super.poseIdleLoop(entity, ticks, yRotOffsetRad, xRotRad, swingingHand);
        head.xRot -= 0.2618F;
    }
    
}