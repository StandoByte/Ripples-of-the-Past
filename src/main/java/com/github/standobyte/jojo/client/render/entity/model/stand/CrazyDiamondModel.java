package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockBullet;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRepairItem;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.pose.ConditionalModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.CopyBipedUserPose;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.stands.CrazyDiamondEntity;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;

// Made with Blockbench 4.1.3


public class CrazyDiamondModel extends HumanoidStandModel<CrazyDiamondEntity> {
    private final ModelRenderer helmet;
    private final ModelRenderer bone2;
    private final ModelRenderer bone12;
    private final ModelRenderer bone13;
    private final ModelRenderer bone14;
    private final ModelRenderer bone15;
    private final ModelRenderer rightEar;
    private final ModelRenderer bone3;
    private final ModelRenderer bone4;
    private final ModelRenderer bone5;
    private final ModelRenderer bone6;
    private final ModelRenderer bone7;
    private final ModelRenderer leftEar;
    private final ModelRenderer bone;
    private final ModelRenderer heartSmall2;
    private final ModelRenderer smallHeartCube4;
    private final ModelRenderer smallHeartCube5;
    private final ModelRenderer smallHeartCube6;
    private final ModelRenderer tube;
    private final ModelRenderer tube2;
    private final ModelRenderer tube3;
    private final ModelRenderer tube4;
    private final ModelRenderer tube5;
    private final ModelRenderer tube6;
    private final ModelRenderer heartLarge;
    private final ModelRenderer largeHeartCube1;
    private final ModelRenderer largeHeartCube2;
    private final ModelRenderer heart3;
    private final ModelRenderer heartCube7;
    private final ModelRenderer heartCube8;
    private final ModelRenderer heartCube9;
    private final ModelRenderer heartLeftShoulder;
    private final ModelRenderer largeHeartCube7;
    private final ModelRenderer largeHeartCube8;
    private final ModelRenderer heartRightShoulder;
    private final ModelRenderer largeHeartCube3;
    private final ModelRenderer largeHeartCube4;
    private final ModelRenderer heartLeftLeg;
    private final ModelRenderer heartCube5;
    private final ModelRenderer heartCube6;
    private final ModelRenderer heartCube10;
    private final ModelRenderer heartRightLeg;
    private final ModelRenderer heartCube2;
    private final ModelRenderer heartCube3;
    private final ModelRenderer heartCube4;

    public CrazyDiamondModel() {
        super();
        
        addHumanoidBaseBoxes(null);
        texWidth = 128;
        texHeight = 128;

        helmet = new ModelRenderer(this);
        helmet.setPos(0.0F, 24.5F, 0.0F);
        head.addChild(helmet);
        helmet.texOffs(91, 0).addBox(-4.0F, -32.95F, -2.15F, 8.0F, 0.0F, 6.0F, 0.05F, false);
        helmet.texOffs(96, 6).addBox(-3.5F, -32.95F, -3.2125F, 7.0F, 0.0F, 1.0F, 0.05F, false);
        helmet.texOffs(97, 7).addBox(-2.5F, -32.95F, -4.275F, 5.0F, 0.0F, 1.0F, 0.05F, false);
        helmet.texOffs(99, 8).addBox(-1.0F, -33.0F, -4.4F, 2.0F, 4.0F, 1.0F, 0.0F, false);
        helmet.texOffs(63, 14).addBox(-1.0F, -27.375F, -4.65F, 2.0F, 1.0F, 1.0F, -0.125F, false);

        bone2 = new ModelRenderer(this);
        bone2.setPos(0.0F, -33.0F, -5.4F);
        helmet.addChild(bone2);
        setRotationAngle(bone2, 0.0F, 0.3927F, 0.0F);
        bone2.texOffs(61, 0).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
        bone2.texOffs(61, 4).addBox(-4.0F, 4.0F, 0.0F, 1.0F, 5.0F, 1.0F, 0.0F, false);
        bone2.texOffs(62, 6).addBox(-3.0F, 6.0F, 0.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        bone2.texOffs(54, 13).addBox(-3.9707F, 5.625F, 0.106F, 4.0F, 1.0F, 1.0F, -0.125F, false);

        bone12 = new ModelRenderer(this);
        bone12.setPos(-4.0F, 0.0F, 0.0F);
        bone2.addChild(bone12);
        setRotationAngle(bone12, 0.0F, 0.9163F, 0.0F);
        bone12.texOffs(56, 0).addBox(-5.0F, 0.0F, 0.0F, 5.0F, 4.0F, 1.0F, 0.0F, false);
        bone12.texOffs(59, 4).addBox(-2.0F, 4.0F, 0.0F, 2.0F, 5.0F, 1.0F, 0.0F, false);
        bone12.texOffs(56, 4).addBox(-5.0F, 4.0F, 0.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        bone13 = new ModelRenderer(this);
        bone13.setPos(-5.0F, 0.0F, 0.0F);
        bone12.addChild(bone13);
        setRotationAngle(bone13, 0.0F, 0.48F, 0.0F);
        bone13.texOffs(53, 0).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 4.0F, 1.0F, 0.0F, false);
        bone13.texOffs(53, 4).addBox(-3.0F, 4.0F, 0.0F, 3.0F, 2.0F, 1.0F, 0.0F, false);

        bone14 = new ModelRenderer(this);
        bone14.setPos(-3.0F, 0.0F, 0.0F);
        bone13.addChild(bone14);
        setRotationAngle(bone14, 0.0F, 0.8727F, 0.0F);
        bone14.texOffs(51, 0).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 6.0F, 1.0F, 0.0F, false);

        bone15 = new ModelRenderer(this);
        bone15.setPos(-2.0F, 0.0F, 0.0F);
        bone14.addChild(bone15);
        setRotationAngle(bone15, 0.0F, 0.7854F, 0.0F);
        bone15.texOffs(48, 0).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 6.0F, 1.0F, 0.0F, false);

        rightEar = new ModelRenderer(this);
        rightEar.setPos(-3.0F, 4.6F, 0.55F);
        bone12.addChild(rightEar);
        rightEar.texOffs(40, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        bone3 = new ModelRenderer(this);
        bone3.setPos(0.0F, -33.0F, -5.4F);
        helmet.addChild(bone3);
        setRotationAngle(bone3, 0.0F, 2.7489F, 0.0F);
        bone3.texOffs(60, 0).addBox(-4.0F, 0.0F, -1.0F, 4.0F, 4.0F, 1.0F, 0.0F, false);
        bone3.texOffs(66, 4).addBox(-4.0F, 4.0F, -1.0F, 1.0F, 5.0F, 1.0F, 0.0F, false);
        bone3.texOffs(65, 6).addBox(-3.0F, 6.0F, -1.0F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        bone3.texOffs(68, 13).addBox(-3.9707F, 5.625F, -1.106F, 4.0F, 1.0F, 1.0F, -0.125F, true);

        bone4 = new ModelRenderer(this);
        bone4.setPos(-4.0F, 0.0F, 0.0F);
        bone3.addChild(bone4);
        setRotationAngle(bone4, 0.0F, -0.9163F, 0.0F);
        bone4.texOffs(63, 0).addBox(-5.0F, 0.0F, -1.0F, 5.0F, 4.0F, 1.0F, 0.0F, false);
        bone4.texOffs(66, 4).addBox(-2.0F, 4.0F, -1.0F, 2.0F, 5.0F, 1.0F, 0.0F, false);
        bone4.texOffs(71, 4).addBox(-5.0F, 4.0F, -1.0F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        bone5 = new ModelRenderer(this);
        bone5.setPos(-5.0F, 0.0F, 0.0F);
        bone4.addChild(bone5);
        setRotationAngle(bone5, 0.0F, -0.48F, 0.0F);
        bone5.texOffs(70, 0).addBox(-3.0F, 0.0F, -1.0F, 3.0F, 4.0F, 1.0F, 0.0F, false);
        bone5.texOffs(70, 4).addBox(-3.0F, 4.0F, -1.0F, 3.0F, 2.0F, 1.0F, 0.0F, false);

        bone6 = new ModelRenderer(this);
        bone6.setPos(-3.0F, 0.0F, 0.0F);
        bone5.addChild(bone6);
        setRotationAngle(bone6, 0.0F, -0.8727F, 0.0F);
        bone6.texOffs(74, 0).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 6.0F, 1.0F, 0.0F, false);

        bone7 = new ModelRenderer(this);
        bone7.setPos(-2.0F, 0.0F, 0.0F);
        bone6.addChild(bone7);
        setRotationAngle(bone7, 0.0F, -0.7854F, 0.0F);
        bone7.texOffs(75, 0).addBox(-3.0F, 0.0F, -1.0F, 3.0F, 6.0F, 1.0F, 0.0F, false);

        leftEar = new ModelRenderer(this);
        leftEar.setPos(-3.0F, 4.6F, -0.45F);
        bone4.addChild(leftEar);
        leftEar.texOffs(83, 0).addBox(-1.0F, -1.0F, -1.1F, 2.0F, 2.0F, 2.0F, 0.0F, false);

        bone = new ModelRenderer(this);
        bone.setPos(0.0F, -29.0F, -4.75F);
        helmet.addChild(bone);
        setRotationAngle(bone, 0.0F, 0.7854F, 0.0F);
        bone.texOffs(64, 10).addBox(-0.75F, -0.75F, -0.25F, 1.0F, 3.0F, 1.0F, -0.25F, false);

        heartSmall2 = new ModelRenderer(this);
        heartSmall2.setPos(0.0F, 0.55F, -4.0F);
        head.addChild(heartSmall2);
        

        smallHeartCube4 = new ModelRenderer(this);
        smallHeartCube4.setPos(0.0F, 0.0F, 0.0F);
        heartSmall2.addChild(smallHeartCube4);
        setRotationAngle(smallHeartCube4, 0.0F, 0.0F, -0.7854F);
        smallHeartCube4.texOffs(0, 4).addBox(0.0F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube5 = new ModelRenderer(this);
        smallHeartCube5.setPos(0.3F, -0.3F, 0.0F);
        heartSmall2.addChild(smallHeartCube5);
        setRotationAngle(smallHeartCube5, 0.0F, 0.0F, -0.7854F);
        smallHeartCube5.texOffs(0, 6).addBox(-0.05F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        smallHeartCube6 = new ModelRenderer(this);
        smallHeartCube6.setPos(-0.3F, -0.3F, 0.0F);
        heartSmall2.addChild(smallHeartCube6);
        setRotationAngle(smallHeartCube6, 0.0F, 0.0F, -0.7854F);
        smallHeartCube6.texOffs(0, 2).addBox(0.0F, -0.95F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        
        torso.texOffs(0, 84).addBox(-4.5F, 4.5F, -0.5F, 9.0F, 1.0F, 3.0F, 0.0F, false);
        torso.texOffs(14, 88).addBox(2.5F, 5.6F, -2.5F, 2.0F, 2.0F, 5.0F, 0.0F, false);
        torso.texOffs(0, 88).addBox(-4.5F, 5.6F, -2.5F, 2.0F, 2.0F, 5.0F, 0.0F, false);
        torso.texOffs(4, 80).addBox(2.25F, 1.5F, 1.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        torso.texOffs(0, 80).addBox(-3.25F, 1.5F, 1.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
        torso.texOffs(29, 64).addBox(0.5F, 1.25F, -2.75F, 3.0F, 3.0F, 1.0F, 0.1F, false);
        torso.texOffs(20, 64).addBox(-3.5F, 1.25F, -2.75F, 3.0F, 3.0F, 1.0F, 0.1F, false);
        torso.texOffs(24, 73).addBox(-2.5F, 4.0F, -2.3F, 5.0F, 6.0F, 1.0F, 0.0F, false);
        torso.texOffs(24, 80).addBox(-1.0F, 10.75F, -2.5F, 2.0F, 4.0F, 1.0F, 0.0F, false);
        torso.texOffs(108, 106).addBox(3.65F, 10.5F, -1.5F, 1.0F, 3.0F, 3.0F, -0.25F, true);
        torso.texOffs(76, 106).addBox(-4.65F, 10.5F, -1.5F, 1.0F, 3.0F, 3.0F, -0.25F, false);

        tube = new ModelRenderer(this);
        tube.setPos(1.0F, 1.25F, 2.0F);
        torso.addChild(tube);
        setRotationAngle(tube, -0.288F, 0.0F, 0.1571F);
        tube.texOffs(27, 16).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 4.0F, 3.0F, 0.0F, false);
        tube.texOffs(27, 23).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.0F, 2.0F, 0.0F, false);

        tube2 = new ModelRenderer(this);
        tube2.setPos(1.5F, 2.25F, 2.25F);
        torso.addChild(tube2);
        setRotationAngle(tube2, -0.0785F, 0.2618F, 0.6458F);
        tube2.texOffs(35, 16).addBox(-0.5F, -5.5F, -0.5F, 1.0F, 6.0F, 3.0F, 0.0F, false);
        tube2.texOffs(36, 25).addBox(-0.5F, -4.5F, -0.5F, 1.0F, 4.0F, 2.0F, 0.0F, false);
        tube2.texOffs(36, 31).addBox(-0.5F, -4.5F, 0.5F, 1.0F, 3.0F, 0.0F, 0.0F, false);

        tube3 = new ModelRenderer(this);
        tube3.setPos(2.0F, 3.5F, 2.0F);
        torso.addChild(tube3);
        setRotationAngle(tube3, -0.0262F, 0.3578F, 0.733F);
        tube3.texOffs(43, 16).addBox(-0.5F, -6.5F, -1.5F, 1.0F, 7.0F, 4.0F, 0.0F, false);
        tube3.texOffs(43, 27).addBox(-0.5F, -5.5F, -0.5F, 1.0F, 5.0F, 2.0F, 0.0F, false);

        tube4 = new ModelRenderer(this);
        tube4.setPos(-1.0F, 1.25F, 2.0F);
        torso.addChild(tube4);
        setRotationAngle(tube4, -0.3229F, 0.0F, -0.192F);
        tube4.texOffs(18, 23).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.0F, 2.0F, 0.0F, false);
        tube4.texOffs(18, 16).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 4.0F, 3.0F, 0.0F, false);

        tube5 = new ModelRenderer(this);
        tube5.setPos(-1.25F, 2.25F, 2.25F);
        torso.addChild(tube5);
        setRotationAngle(tube5, -0.1484F, -0.2356F, -0.6196F);
        tube5.texOffs(10, 16).addBox(-0.5F, -5.5F, -0.5F, 1.0F, 6.0F, 3.0F, 0.0F, false);
        tube5.texOffs(10, 25).addBox(-0.5F, -4.5F, -0.5F, 1.0F, 4.0F, 2.0F, 0.0F, false);
        tube5.texOffs(10, 31).addBox(-0.5F, -4.5F, 0.5F, 1.0F, 3.0F, 0.0F, 0.0F, false);

        tube6 = new ModelRenderer(this);
        tube6.setPos(-1.9F, 3.6F, 2.0F);
        torso.addChild(tube6);
        setRotationAngle(tube6, -0.0436F, -0.3665F, -0.6632F);
        tube6.texOffs(0, 16).addBox(-0.5F, -6.5F, -1.5F, 1.0F, 7.0F, 4.0F, 0.0F, false);
        tube6.texOffs(0, 27).addBox(-0.5F, -5.5F, -0.5F, 1.0F, 5.0F, 2.0F, 0.0F, false);

        heartLarge = new ModelRenderer(this);
        heartLarge.setPos(0.0F, 7.5F, -2.0F);
        torso.addChild(heartLarge);
        

        largeHeartCube1 = new ModelRenderer(this);
        largeHeartCube1.setPos(0.0F, 0.0F, 0.0F);
        heartLarge.addChild(largeHeartCube1);
        setRotationAngle(largeHeartCube1, 0.0F, 0.0F, 0.7854F);
        largeHeartCube1.texOffs(40, 74).addBox(-1.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.4F, false);

        largeHeartCube2 = new ModelRenderer(this);
        largeHeartCube2.setPos(0.0F, 0.0F, 0.0F);
        heartLarge.addChild(largeHeartCube2);
        setRotationAngle(largeHeartCube2, 0.0F, 0.0F, -0.7854F);
        largeHeartCube2.texOffs(36, 74).addBox(0.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.4F, false);

        heart3 = new ModelRenderer(this);
        heart3.setPos(0.0F, 11.75F, -2.3F);
        torso.addChild(heart3);
        

        heartCube7 = new ModelRenderer(this);
        heartCube7.setPos(0.0F, 0.05F, 0.0F);
        heart3.addChild(heartCube7);
        setRotationAngle(heartCube7, 0.0F, 0.0F, -0.7854F);
        heartCube7.texOffs(40, 77).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube8 = new ModelRenderer(this);
        heartCube8.setPos(0.5F, -0.45F, 0.0F);
        heart3.addChild(heartCube8);
        setRotationAngle(heartCube8, 0.0F, 0.0F, -0.7854F);
        heartCube8.texOffs(44, 77).addBox(-0.1F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube9 = new ModelRenderer(this);
        heartCube9.setPos(-0.5F, -0.45F, 0.0F);
        heart3.addChild(heartCube9);
        setRotationAngle(heartCube9, 0.0F, 0.0F, -0.7854F);
        heartCube9.texOffs(36, 77).addBox(0.0F, -0.95F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        leftArm.texOffs(48, 112).addBox(-1.5F, -1.5F, -2.75F, 3.0F, 4.0F, 1.0F, -0.25F, false);
        leftArm.texOffs(56, 112).addBox(-1.5F, -1.5F, 1.75F, 3.0F, 4.0F, 1.0F, -0.25F, false);
        leftArm.texOffs(12, 109).addBox(-1.0F, 2.5F, 1.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        leftForeArm.texOffs(48, 117).addBox(-2.75F, -0.25F, -1.5F, 1.0F, 3.0F, 3.0F, -0.251F, false);
        leftForeArm.texOffs(56, 117).addBox(1.75F, -0.25F, -1.5F, 1.0F, 3.0F, 3.0F, -0.251F, false);
        leftForeArm.texOffs(48, 123).addBox(1.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, true);

        heartLeftShoulder = new ModelRenderer(this);
        heartLeftShoulder.setPos(2.2F, 2.0F, 0.0F);
        leftArm.addChild(heartLeftShoulder);
        setRotationAngle(heartLeftShoulder, 0.1745F, -1.5708F, 0.0F);
        

        largeHeartCube7 = new ModelRenderer(this);
        largeHeartCube7.setPos(0.0F, 0.0F, -0.5F);
        heartLeftShoulder.addChild(largeHeartCube7);
        setRotationAngle(largeHeartCube7, 0.0F, 0.2618F, 0.7854F);
        largeHeartCube7.texOffs(56, 103).addBox(-3.0F, -4.0F, 0.0F, 3.0F, 4.0F, 1.0F, 0.0F, false);
        largeHeartCube7.texOffs(56, 108).addBox(-2.0F, -3.5F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2F, true);

        largeHeartCube8 = new ModelRenderer(this);
        largeHeartCube8.setPos(0.0F, 0.0F, -0.5F);
        heartLeftShoulder.addChild(largeHeartCube8);
        setRotationAngle(largeHeartCube8, 0.0F, -0.2618F, -0.7854F);
        largeHeartCube8.texOffs(48, 103).addBox(0.0F, -4.0F, 0.0F, 3.0F, 4.0F, 1.0F, 0.0F, false);
        largeHeartCube8.texOffs(50, 108).addBox(1.0F, -3.5F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2F, true);

        rightArm.texOffs(16, 112).addBox(-1.5F, -1.5F, -2.75F, 3.0F, 4.0F, 1.0F, -0.25F, false);
        rightArm.texOffs(24, 112).addBox(-1.5F, -1.5F, 1.75F, 3.0F, 4.0F, 1.0F, -0.25F, false);
        rightArm.texOffs(44, 109).addBox(-1.0F, 2.5F, 1.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        rightForeArm.texOffs(16, 117).addBox(-2.75F, -0.25F, -1.5F, 1.0F, 3.0F, 3.0F, -0.251F, false);
        rightForeArm.texOffs(24, 117).addBox(1.75F, -0.25F, -1.5F, 1.0F, 3.0F, 3.0F, -0.251F, false);
        rightForeArm.texOffs(16, 123).addBox(-2.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, false);

        heartRightShoulder = new ModelRenderer(this);
        heartRightShoulder.setPos(-2.2F, 2.0F, 0.0F);
        rightArm.addChild(heartRightShoulder);
        setRotationAngle(heartRightShoulder, 0.1745F, 1.5708F, 0.0F);
        

        largeHeartCube3 = new ModelRenderer(this);
        largeHeartCube3.setPos(0.0F, 0.0F, -0.5F);
        heartRightShoulder.addChild(largeHeartCube3);
        setRotationAngle(largeHeartCube3, 0.0F, 0.2618F, 0.7854F);
        largeHeartCube3.texOffs(24, 103).addBox(-3.0F, -4.0F, 0.0F, 3.0F, 4.0F, 1.0F, 0.0F, false);
        largeHeartCube3.texOffs(24, 108).addBox(-2.0F, -3.5F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2F, false);

        largeHeartCube4 = new ModelRenderer(this);
        largeHeartCube4.setPos(0.0F, 0.0F, -0.5F);
        heartRightShoulder.addChild(largeHeartCube4);
        setRotationAngle(largeHeartCube4, 0.0F, -0.2618F, -0.7854F);
        largeHeartCube4.texOffs(16, 103).addBox(0.0F, -4.0F, 0.0F, 3.0F, 4.0F, 1.0F, 0.0F, false);
        largeHeartCube4.texOffs(18, 108).addBox(1.0F, -3.5F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2F, false);

        leftLeg.texOffs(118, 112).addBox(1.3F, 1.25F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
        leftLeg.texOffs(112, 112).addBox(-2.3F, 0.75F, -1.0F, 1.0F, 4.0F, 2.0F, 0.0F, false);

        heartLeftLeg = new ModelRenderer(this);
        heartLeftLeg.setPos(-3.8F, 6.0F, -1.8F);
        leftLeg.addChild(heartLeftLeg);
        

        heartCube5 = new ModelRenderer(this);
        heartCube5.setPos(0.0F, 0.05F, 0.0F);
        heartLeftLeg.addChild(heartCube5);
        setRotationAngle(heartCube5, 0.0F, 0.0F, -0.7854F);
        heartCube5.texOffs(124, 116).addBox(2.8284F, 1.7784F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube6 = new ModelRenderer(this);
        heartCube6.setPos(0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube6);
        setRotationAngle(heartCube6, 0.0F, 0.0F, -0.7854F);
        heartCube6.texOffs(124, 118).addBox(2.7284F, 1.7784F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube10 = new ModelRenderer(this);
        heartCube10.setPos(-0.5F, -0.45F, 0.0F);
        heartLeftLeg.addChild(heartCube10);
        setRotationAngle(heartCube10, 0.0F, 0.0F, -0.7854F);
        heartCube10.texOffs(124, 114).addBox(2.8284F, 1.8784F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        leftLowerLeg.texOffs(118, 117).addBox(1.3F, 0.05F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
        leftLowerLeg.texOffs(112, 118).addBox(-2.3F, 0.05F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
        leftLowerLeg.texOffs(112, 123).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.249F, false);
        leftLowerLeg.texOffs(92, 125).addBox(-2.9F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
        leftLowerLeg.texOffs(124, 125).addBox(1.9F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

        rightLeg.texOffs(80, 112).addBox(-2.3F, 0.95F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
        rightLeg.texOffs(86, 112).addBox(1.3F, 0.75F, -1.0F, 1.0F, 4.0F, 2.0F, 0.0F, false);

        heartRightLeg = new ModelRenderer(this);
        heartRightLeg.setPos(0.0F, 6.0F, -1.8F);
        rightLeg.addChild(heartRightLeg);
        

        heartCube2 = new ModelRenderer(this);
        heartCube2.setPos(0.0F, 0.05F, 0.0F);
        heartRightLeg.addChild(heartCube2);
        setRotationAngle(heartCube2, 0.0F, 0.0F, -0.7854F);
        heartCube2.texOffs(92, 116).addBox(0.0F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube3 = new ModelRenderer(this);
        heartCube3.setPos(0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube3);
        setRotationAngle(heartCube3, 0.0F, 0.0F, -0.7854F);
        heartCube3.texOffs(92, 118).addBox(-0.1F, -1.05F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        heartCube4 = new ModelRenderer(this);
        heartCube4.setPos(-0.5F, -0.45F, 0.0F);
        heartRightLeg.addChild(heartCube4);
        setRotationAngle(heartCube4, 0.0F, 0.0F, -0.7854F);
        heartCube4.texOffs(92, 114).addBox(0.0F, -0.95F, -0.5F, 1.0F, 1.0F, 1.0F, -0.05F, false);

        rightLowerLeg.texOffs(80, 123).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.249F, false);
        rightLowerLeg.texOffs(80, 117).addBox(-2.3F, 0.05F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
        rightLowerLeg.texOffs(86, 118).addBox(1.3F, 0.05F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
        rightLowerLeg.texOffs(80, 125).addBox(-2.9F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
        rightLowerLeg.texOffs(112, 125).addBox(1.9F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
    }

    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head, -32.5F, 52.5F, -5),
                    RotationAngle.fromDegrees(body, -7.5F, 37.5F, 0),
                    RotationAngle.fromDegrees(upperPart, 0, 12.5F, 0),
                    RotationAngle.fromDegrees(leftArm, 25.1426F, 18.0217F, -54.0834F),
                    RotationAngle.fromDegrees(leftForeArm, -37.1572F, -2.0551F, 44.1678F),
                    RotationAngle.fromDegrees(rightArm, -60.3335F, -8.4521F, 68.4237F),
                    RotationAngle.fromDegrees(rightForeArm, -135, 22.5F, -90),
                    RotationAngle.fromDegrees(leftLeg, -10.1402F, 10.7145F, 3.1723F),
                    RotationAngle.fromDegrees(leftLowerLeg, 45, 0, 0),
                    RotationAngle.fromDegrees(rightLeg, 27.9546F, 33.9337F, 7.8335F),
                    RotationAngle.fromDegrees(rightLowerLeg, 29.6217F, 4.9809F, -8.6822F)
            },
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head, 0, 0, 0),
                    RotationAngle.fromDegrees(body, 15, 0, -10),
                    RotationAngle.fromDegrees(upperPart, 0, -12.5F, 0),
                    RotationAngle.fromDegrees(leftArm, -49.8651F, 9.3787F, -53.3701F),
                    RotationAngle.fromDegrees(leftForeArm, -93.6597F, -5.1369F, 84.6506F),
                    RotationAngle.fromDegrees(rightArm, 30.2141F, -5.5863F, 54.7232F),
                    RotationAngle.fromDegrees(rightForeArm, 0, 0, 0),
                    RotationAngle.fromDegrees(leftLeg, -20, 0, -45),
                    RotationAngle.fromDegrees(leftLowerLeg, 30, 0, 37.5F),
                    RotationAngle.fromDegrees(rightLeg, -74.6528F, 12.0675F, 3.284F),
                    RotationAngle.fromDegrees(rightLowerLeg, 100, 0, 0)
            },
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head, -4.3644F, 30.3695F, -8.668F),
                    RotationAngle.fromDegrees(body, -35, -25, 7),
                    RotationAngle.fromDegrees(upperPart, 0, 10, 0),
                    RotationAngle.fromDegrees(leftArm, 12.5F, 0, -20),
                    RotationAngle.fromDegrees(leftForeArm, 7.5F, 0, -10),
                    RotationAngle.fromDegrees(rightArm, 37.5F, 7.5F, -7.5F),
                    RotationAngle.fromDegrees(rightForeArm, -7.5F, 0, 0),
                    RotationAngle.fromDegrees(leftLeg, -43.5572F, 57.6471F, -22.5553F),
                    RotationAngle.fromDegrees(leftLowerLeg, 77.4538F, 4.8812F, -1.0848F),
                    RotationAngle.fromDegrees(rightLeg, -65, 60, 20),
                    RotationAngle.fromDegrees(rightLowerLeg, 107.7531F, 9.5327F, 3.0351F)
            },
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head, 0, 75, 0),
                    RotationAngle.fromDegrees(body, -10, 60, 0),
                    RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                    RotationAngle.fromDegrees(leftArm, 16.7363F, 5.188F, -39.2363F),
                    RotationAngle.fromDegrees(leftForeArm, -52.4165F, 42.9971F, 31.9928F),
                    RotationAngle.fromDegrees(rightArm, -60, 0, 75),
                    RotationAngle.fromDegrees(rightForeArm, -154.2444F, 12.7F, -103.0794F),
                    RotationAngle.fromDegrees(leftLeg, 12.5F, 0, 0),
                    RotationAngle.fromDegrees(leftLowerLeg, 22.5F, 0, 0),
                    RotationAngle.fromDegrees(rightLeg, -56.6544F, 29.5657F, 5.3615F),
                    RotationAngle.fromDegrees(rightLowerLeg, 112.5F, 0, 0)
            }
        };
    }
    
    @Override
    protected void initActionPoses() {
        ModelPose<CrazyDiamondEntity> heavyPunchPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 385, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, 7.5F, 382.5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                RotationAngle.fromDegrees(leftArm, 0, 0, -22.5F),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 10),
                RotationAngle.fromDegrees(rightArm, 0, 0, 80),
                RotationAngle.fromDegrees(rightForeArm, -70, -22.5F, -30),
                RotationAngle.fromDegrees(leftLeg, 7.5F, 0, -7.5F),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, -7.5F, 0, 5),
                RotationAngle.fromDegrees(rightLowerLeg, 17.5F, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 10, 400.5F, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, 30, 390, 0),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 7.5F, 0, -45),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 17.5F),
                RotationAngle.fromDegrees(rightArm, 15, -5, 100),
                RotationAngle.fromDegrees(rightForeArm, -90, -10, -70),
                RotationAngle.fromDegrees(leftLeg, 15, 0, -7.5F),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, -12.5F, 0, 10),
                RotationAngle.fromDegrees(rightLowerLeg, 25, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 345, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, -7.5F, 337.5F, -7.5F),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, -22.5F, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -45, -15, 75),
                RotationAngle.fromDegrees(rightArm, 0, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, 0, 90, 0),
                RotationAngle.fromDegrees(leftLeg, 20, 0, -5),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, 1.6667F, -1.6667F, 34.1667F),
                RotationAngle.fromDegrees(rightLowerLeg, 26.67F, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose4 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -5, 322.5F, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, -21.25F, 282.5F, -13.75F),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, -5.62F, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -45, -15, 75),
                RotationAngle.fromDegrees(rightArm, 30, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, 0, 90, 0),
                RotationAngle.fromDegrees(leftLeg, 25, 0, -2.5F),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, 15.8334F, -3.3334F, 58.3334F),
                RotationAngle.fromDegrees(rightLowerLeg, 28.34F, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose5 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -17.5F, 300, -22.5F).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, -30, 225, -15),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 11.25F, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -45, -15, 75),
                RotationAngle.fromDegrees(rightArm, 60, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, -20, 60, -20),
                RotationAngle.fromDegrees(leftLeg, 30, 0, 0),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, 30, -5, 82.5F),
                RotationAngle.fromDegrees(rightLowerLeg, 30, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose6 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 15, 60, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, 15, 102.5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 45, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -45, -15, 75),
                RotationAngle.fromDegrees(rightArm, 60, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, -60, 0, -60),
                RotationAngle.fromDegrees(leftLeg, -3.3333F, 6.6667F, -6.6667F),
                RotationAngle.fromDegrees(leftLowerLeg, 45, 0, 0),
                RotationAngle.fromDegrees(rightLeg, -49.1667F, 10.8333F, 36.6667F),
                RotationAngle.fromDegrees(rightLowerLeg, 80, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose7 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 15, -7.5F), 
                RotationAngle.fromDegrees(body, 22.5F, 36.25F, -7.5F),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -67.5F, 0, 86.25F),
                RotationAngle.fromDegrees(rightArm, 45, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, -45, -5, -120),
                RotationAngle.fromDegrees(leftLeg, -20, 10, -10),
                RotationAngle.fromDegrees(leftLowerLeg, 67.5F, 0, 0),
                RotationAngle.fromDegrees(rightLeg, -88.75F, 18.75F, 13.75F),
                RotationAngle.fromDegrees(rightLowerLeg, 105, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose8 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 0, 0), 
                RotationAngle.fromDegrees(body, 30, -30, -15),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 15, -60),
                RotationAngle.fromDegrees(leftForeArm, -90, 15, 97.5F),
                RotationAngle.fromDegrees(rightArm, -45, -10, 75),
                RotationAngle.fromDegrees(rightForeArm, -165.665F, -4.4638F, -133.0616F),
                RotationAngle.fromDegrees(leftLeg, -76.5527F, 15.4535F, -3.9853F),
                RotationAngle.fromDegrees(leftLowerLeg, 90, 0, 0),
                RotationAngle.fromDegrees(rightLeg, 15, 30, 0),
                RotationAngle.fromDegrees(rightLowerLeg, 30, 0, 0)
        });
        actionAnim.put(StandPose.HEAVY_ATTACK, new PosedActionAnimation.Builder<CrazyDiamondEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<>(heavyPunchPose1)
                        .addPose(0.2222F, heavyPunchPose2)
                        .addPose(0.3333F, heavyPunchPose3)
                        .addPose(0.4444F, heavyPunchPose4)
                        .addPose(0.5555F, heavyPunchPose5)
                        .addPose(0.7777F, heavyPunchPose6)
                        .addPose(0.8888F, heavyPunchPose7)
                        .build(heavyPunchPose8))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<>(heavyPunchPose8)
                        .addPose(0.5F, heavyPunchPose8)
                        .build(idlePose))
                .build(idlePose));
        
        
        
        IModelPose<CrazyDiamondEntity> heavyFinisherPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 5, 0, 6.36F), 
                RotationAngle.fromDegrees(body, 4.2341F, 39.7845F, 6.5861F), 
                RotationAngle.fromDegrees(upperPart, 0F, 5F, 0F), 
                RotationAngle.fromDegrees(leftArm, 15F, -10F, -52.5F),
                RotationAngle.fromDegrees(leftForeArm, -88.6703F, -3.8472F, 87.0901F),
                RotationAngle.fromDegrees(rightArm, 10.1762F, 16.6443F, 93.1445F), 
                RotationAngle.fromDegrees(rightForeArm, -77.4892F, -4.7192F, -74.0538F),
                RotationAngle.fromDegrees(leftLeg, -52.5F, -37.5F, 0),
                RotationAngle.fromDegrees(leftLowerLeg, 97.447F, -7.3536F, -2.2681F),
                RotationAngle.fromDegrees(rightLeg, 8.2781F, -2.4033F, -0.0432F),
                RotationAngle.fromDegrees(rightLowerLeg, 10, -5, 0)
        });
        IModelPose<CrazyDiamondEntity> heavyFinisherPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -6.9176F, 15.7939F, 16.6495F), 
                RotationAngle.fromDegrees(body, 16.7396F, 58.5251F, 19.4254F), 
                RotationAngle.fromDegrees(upperPart, 0F, 15F, 0F), 
                RotationAngle.fromDegrees(leftArm, -11.0864F, -27.2098F, -49.134F),
                RotationAngle.fromDegrees(leftForeArm, -98.9572F, -21.4891F, 114.4737F),
                RotationAngle.fromDegrees(rightArm, 37.9264F, 14.6364F, 103.3191F), 
                RotationAngle.fromDegrees(rightForeArm, -89.3397F, -34.9867F, -92.8194F),
                RotationAngle.fromDegrees(leftLeg, -36.5212F, -38.7805F, -7.0481F),
                RotationAngle.fromDegrees(leftLowerLeg, 111.7619F, 4.0651F, 10.1255F),
                RotationAngle.fromDegrees(rightLeg, 24.8305F, -0.7714F, 0),
                RotationAngle.fromDegrees(rightLowerLeg, 0.7594F, -5, 0)
        });
        IModelPose<CrazyDiamondEntity> heavyFinisherPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(rightArm, -38.3F, 20.47F, 63.55F), 
                RotationAngle.fromDegrees(rightForeArm, -67.5782F, 1.503F, -72.9104F),
        });
        IModelPose<CrazyDiamondEntity> heavyFinisherPose4 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -30, 0), 
                RotationAngle.fromDegrees(upperPart, 0F, -15F, 0F), 
                RotationAngle.fromDegrees(leftArm, 17.8981F, 11.9128F, -21.186F),
                RotationAngle.fromDegrees(leftForeArm, -83.3352F, 3.9942F, 28.1685F),
                RotationAngle.fromDegrees(rightArm, -72.6819F, 35.6647F, 53.5229F), 
                RotationAngle.fromDegrees(rightForeArm, 0, 0, -12.5F),
                RotationAngle.fromDegrees(leftLeg, -48.4357F, 19.6329F, 0.1075F),
                RotationAngle.fromDegrees(leftLowerLeg, 71.8824F, 15.9537F, 11.2591F),
                RotationAngle.fromDegrees(rightLeg, 33.6661F, 23.9013F, 7.2025F),
                RotationAngle.fromDegrees(rightLowerLeg, 22.5F, -5, 0)
        });

        actionAnim.put(StandPose.HEAVY_ATTACK_FINISHER, new PosedActionAnimation.Builder<CrazyDiamondEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<>(heavyFinisherPose1, heavyFinisherPose2))
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransitionMultiple.Builder<>(heavyFinisherPose2)
                        .addPose(0.5F, heavyFinisherPose3)
                        .build(heavyFinisherPose4))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<>(heavyFinisherPose4)
                        .addPose(0.5F, heavyFinisherPose4)
                        .build(idlePose))
                .build(idlePose));
        
        
        
        RotationAngle[] itemFixRotations = new RotationAngle[] {
                RotationAngle.fromDegrees(head, 31.301F, 27.0408F, 3.6059F),
                RotationAngle.fromDegrees(body, 5.7686F, 29.8742F, 5.3807F),
                RotationAngle.fromDegrees(upperPart, 0.0F, 6.0F, 0.0F),
                RotationAngle.fromDegrees(leftArm, -33.6218F, 25.82F, -22.9983F),
                RotationAngle.fromDegrees(leftForeArm, -53.621F, -34.2195F, 50.6576F),
                RotationAngle.fromDegrees(rightArm, -45.3923F, -27.0377F, 10.4828F),
                RotationAngle.fromDegrees(rightForeArm, -38.0639F, -35.8085F, 4.6156F)
        };
        actionAnim.put(CrazyDiamondRepairItem.ITEM_FIX_POSE, new PosedActionAnimation.Builder<CrazyDiamondEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ConditionalModelPose<CrazyDiamondEntity>()
                        .addPose(stand -> !stand.isArmsOnlyMode() && stand.getUser() != null && stand.getUser().getMainArm() == HandSide.RIGHT, 
                                new ModelPose<CrazyDiamondEntity>(itemFixRotations))
                        .addPose(stand -> !stand.isArmsOnlyMode() && stand.getUser() != null && stand.getUser().getMainArm() == HandSide.LEFT, 
                                new ModelPose<CrazyDiamondEntity>(mirrorAngles(itemFixRotations)))
                        .addPose(stand -> stand.isArmsOnlyMode(), 
                                new CopyBipedUserPose<>(this))
                        )
                .build(idlePose));
        

        
        ModelAnim<CrazyDiamondEntity> armsRotationFull = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            float xRot = Math.min(xRotRad, 1.0467F);
            setSecondXRot(leftArm, xRot);
            setSecondXRot(rightArm, xRot);
        };
        
        RotationAngle[] blockBulletRotations = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 30.7167F, 25.4083F, 17.1091F),
                RotationAngle.fromDegrees(upperPart, 0.0F, -4.0F, 0.0F),
                RotationAngle.fromDegrees(leftArm, -88.9066F, 18.1241F, -39.2738F),
                RotationAngle.fromDegrees(leftForeArm, -45.6386F, -43.0305F, 61.5635F),
                RotationAngle.fromDegrees(rightArm, -65.0702F, -23.5085F, 5.5623F),
                RotationAngle.fromDegrees(rightForeArm, -97.8419F, 36.1268F, -102.0079F),
                RotationAngle.fromDegrees(leftLeg, -50.8435F, -8.788F, -8.0132F),
                RotationAngle.fromDegrees(leftLowerLeg, 97.5F, 10, 0),
                RotationAngle.fromDegrees(rightLeg, 7.76F, -2.1895F, -3.2001F),
                RotationAngle.fromDegrees(rightLowerLeg, 10, -5, 0)
        };
        actionAnim.put(CrazyDiamondBlockBullet.BLOCK_BULLET_SHOT_POSE, new PosedActionAnimation.Builder<CrazyDiamondEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ConditionalModelPose<CrazyDiamondEntity>()
                        .addPose(stand -> !stand.isArmsOnlyMode() && stand.getUser() != null && stand.getUser().getMainArm() == HandSide.RIGHT, 
                                new ModelPose<CrazyDiamondEntity>(mirrorAngles(blockBulletRotations))
                                .setAdditionalAnim(armsRotationFull))
                        .addPose(stand -> !stand.isArmsOnlyMode() && stand.getUser() != null && stand.getUser().getMainArm() == HandSide.LEFT, 
                                new ModelPose<CrazyDiamondEntity>(blockBulletRotations)
                                .setAdditionalAnim(armsRotationFull))
                        .addPose(stand -> stand.isArmsOnlyMode(), 
                                new CopyBipedUserPose<>(this))
                        )
                .build(idlePose));
        
        super.initActionPoses();
    }
    
    @Override
    protected ModelPose<CrazyDiamondEntity> initIdlePose() {
        return new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(body, 5.7686F, 29.8742F, 5.3807F),
                RotationAngle.fromDegrees(upperPart, 0, 6, 0),
                RotationAngle.fromDegrees(leftArm, 3.25F, -6.25F, -42.5F),
                RotationAngle.fromDegrees(leftForeArm, -75, -15, 92.5F),
                RotationAngle.fromDegrees(rightArm, 35, -15, 40),
                RotationAngle.fromDegrees(rightForeArm, -85, -5, -20),
                RotationAngle.fromDegrees(leftLeg, -52.5F, -15, 0),
                RotationAngle.fromDegrees(leftLowerLeg, 97.5F, 10, 0),
                RotationAngle.fromDegrees(rightLeg, 7.9315F, -12.0964F, -4.5742F),
                RotationAngle.fromDegrees(rightLowerLeg, 10, -5, 0)
        });
    }

    @Override
    protected ModelPose<CrazyDiamondEntity> initIdlePose2Loop() {
        return new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(leftArm, 7.9708F, -6.7104F, -40.0269F),
                RotationAngle.fromDegrees(leftForeArm, -74.8671F, -9.523F, 91.3614F),
                RotationAngle.fromDegrees(rightArm, 40.9054F, -11.7546F, 36.0897F),
                RotationAngle.fromDegrees(rightForeArm, -92.4423F, -9.9808F, -20.4419F)
        });
    }
}