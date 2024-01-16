package com.github.standobyte.jojo.client.render.entity.bb;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

// Made with Blockbench 4.6.4
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod, generate all required imports and add the package declaration


public class CrazyDiamondModelConvertExample extends EntityModel<Entity> {
	private final ModelRenderer head;
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
	private final ModelRenderer body;
	private final ModelRenderer upperPart;
	private final ModelRenderer torso;
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
	private final ModelRenderer leftArm;
	private final ModelRenderer leftArmJoint;
	private final ModelRenderer leftForeArm;
	private final ModelRenderer heartLeftShoulder;
	private final ModelRenderer largeHeartCube7;
	private final ModelRenderer largeHeartCube8;
	private final ModelRenderer rightArm;
	private final ModelRenderer rightArmJoint;
	private final ModelRenderer rightForeArm;
	private final ModelRenderer heartRightShoulder;
	private final ModelRenderer largeHeartCube3;
	private final ModelRenderer largeHeartCube4;
	private final ModelRenderer leftLeg;
	private final ModelRenderer heartLeftLeg;
	private final ModelRenderer heartCube5;
	private final ModelRenderer heartCube6;
	private final ModelRenderer heartCube10;
	private final ModelRenderer leftLegJoint;
	private final ModelRenderer leftLowerLeg;
	private final ModelRenderer rightLeg;
	private final ModelRenderer heartRightLeg;
	private final ModelRenderer heartCube2;
	private final ModelRenderer heartCube3;
	private final ModelRenderer heartCube4;
	private final ModelRenderer rightLegJoint;
	private final ModelRenderer rightLowerLeg;

	public CrazyDiamondModelConvertExample() {
		texWidth = 128;
		texHeight = 128;

		head = new ModelRenderer(this);
		head.setPos(0.0F, 0.0F, 0.0F);
		head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

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

		body = new ModelRenderer(this);
		body.setPos(0.0F, 0.0F, 0.0F);
		

		upperPart = new ModelRenderer(this);
		upperPart.setPos(0.0F, 12.0F, 0.0F);
		body.addChild(upperPart);
		

		torso = new ModelRenderer(this);
		torso.setPos(0.0F, -12.0F, 0.0F);
		upperPart.addChild(torso);
		torso.texOffs(0, 64).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
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
		setRotationAngle(tube, -0.288F, 0.1396F, 0.1571F);
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
		setRotationAngle(tube4, -0.3229F, -0.0349F, -0.192F);
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

		leftArm = new ModelRenderer(this);
		leftArm.setPos(6.0F, -10.0F, 0.0F);
		upperPart.addChild(leftArm);
		leftArm.texOffs(32, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
		leftArm.texOffs(48, 112).addBox(-1.5F, -1.5F, -2.75F, 3.0F, 4.0F, 1.0F, -0.25F, false);
		leftArm.texOffs(56, 112).addBox(-1.5F, -1.5F, 1.75F, 3.0F, 4.0F, 1.0F, -0.25F, false);
		leftArm.texOffs(12, 109).addBox(-1.0F, 2.5F, 1.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

		leftArmJoint = new ModelRenderer(this);
		leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
		leftArm.addChild(leftArmJoint);
		leftArmJoint.texOffs(32, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

		leftForeArm = new ModelRenderer(this);
		leftForeArm.setPos(0.0F, 4.0F, 0.0F);
		leftArm.addChild(leftForeArm);
		leftForeArm.texOffs(32, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
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

		rightArm = new ModelRenderer(this);
		rightArm.setPos(-6.0F, -10.0F, 0.0F);
		upperPart.addChild(rightArm);
		rightArm.texOffs(0, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
		rightArm.texOffs(16, 112).addBox(-1.5F, -1.5F, -2.75F, 3.0F, 4.0F, 1.0F, -0.25F, false);
		rightArm.texOffs(24, 112).addBox(-1.5F, -1.5F, 1.75F, 3.0F, 4.0F, 1.0F, -0.25F, false);
		rightArm.texOffs(44, 109).addBox(-1.0F, 2.5F, 1.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

		rightArmJoint = new ModelRenderer(this);
		rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
		rightArm.addChild(rightArmJoint);
		rightArmJoint.texOffs(0, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

		rightForeArm = new ModelRenderer(this);
		rightForeArm.setPos(0.0F, 4.0F, 0.0F);
		rightArm.addChild(rightForeArm);
		rightForeArm.texOffs(0, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
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

		leftLeg = new ModelRenderer(this);
		leftLeg.setPos(1.9F, 12.0F, 0.0F);
		body.addChild(leftLeg);
		leftLeg.texOffs(96, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
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

		leftLegJoint = new ModelRenderer(this);
		leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
		leftLeg.addChild(leftLegJoint);
		leftLegJoint.texOffs(96, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

		leftLowerLeg = new ModelRenderer(this);
		leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
		leftLeg.addChild(leftLowerLeg);
		leftLowerLeg.texOffs(96, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
		leftLowerLeg.texOffs(118, 117).addBox(1.3F, 0.05F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
		leftLowerLeg.texOffs(112, 118).addBox(-2.3F, 0.05F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
		leftLowerLeg.texOffs(112, 123).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.249F, false);
		leftLowerLeg.texOffs(92, 125).addBox(-2.9F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
		leftLowerLeg.texOffs(124, 125).addBox(1.9F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);

		rightLeg = new ModelRenderer(this);
		rightLeg.setPos(-1.9F, 12.0F, 0.0F);
		body.addChild(rightLeg);
		rightLeg.texOffs(64, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
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

		rightLegJoint = new ModelRenderer(this);
		rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
		rightLeg.addChild(rightLegJoint);
		rightLegJoint.texOffs(64, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

		rightLowerLeg = new ModelRenderer(this);
		rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
		rightLeg.addChild(rightLowerLeg);
		rightLowerLeg.texOffs(64, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
		rightLowerLeg.texOffs(80, 123).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.249F, false);
		rightLowerLeg.texOffs(80, 125).addBox(-2.9F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
		rightLowerLeg.texOffs(112, 125).addBox(1.9F, 3.0F, -0.5F, 1.0F, 1.0F, 1.0F, -0.2F, false);
		rightLowerLeg.texOffs(80, 117).addBox(-2.3F, 0.05F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
		rightLowerLeg.texOffs(86, 118).addBox(1.3F, 0.05F, -1.0F, 1.0F, 3.0F, 2.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		head.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}