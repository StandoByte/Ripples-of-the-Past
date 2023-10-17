package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;

// Made with Blockbench 4.6.4
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports


public class GoldExperienceModelExported extends EntityModel<Entity> {
    private final ModelRenderer head;
    private final ModelRenderer headpiece;
    private final ModelRenderer body;
    private final ModelRenderer upperPart;
    private final ModelRenderer torso;
    private final ModelRenderer torso_r1;
    private final ModelRenderer theThing;
    private final ModelRenderer rightString;
    private final ModelRenderer leftString;
    private final ModelRenderer loincloth;
    private final ModelRenderer leftPartLoincloth;
    private final ModelRenderer rightPartLoincloth;
    private final ModelRenderer rightArm;
    private final ModelRenderer rightWing;
    private final ModelRenderer rightArmJoint;
    private final ModelRenderer rightForeArm;
    private final ModelRenderer leftArm;
    private final ModelRenderer leftWing;
    private final ModelRenderer leftArmJoint;
    private final ModelRenderer leftForeArm;
    private final ModelRenderer rightLeg;
    private final ModelRenderer rightLeg_r1;
    private final ModelRenderer rightLegJoint;
    private final ModelRenderer rightLowerLeg;
    private final ModelRenderer rightLowerLeg_r1;
    private final ModelRenderer rightLowerLeg_r2;
    private final ModelRenderer leftLeg;
    private final ModelRenderer leftLeg_r1;
    private final ModelRenderer leftLegJoint;
    private final ModelRenderer leftLowerLeg;
    private final ModelRenderer leftLowerLeg_r1;
    private final ModelRenderer leftLowerLeg_r2;

	public GoldExperienceModelExported() {
        texWidth = 128;
        texHeight = 128;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(0, 0).addBox(-3.0F, -4.0F, -4.15F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        head.texOffs(0, 2).addBox(1.0F, -4.0F, -4.15F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        head.texOffs(56, 5).addBox(-4.2F, -4.4F, -0.7F, 2.0F, 2.0F, 2.0F, 0.0F, true);
        head.texOffs(56, 5).addBox(2.2F, -4.4F, -0.7F, 2.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(4, 6).addBox(-0.5F, -0.85F, -4.15F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        headpiece = new ModelRenderer(this);
        headpiece.setPos(0.0F, 23.9F, -2.0F);
        head.addChild(headpiece);
        setRotationAngle(headpiece, 0.0436F, 0.0F, 0.0F);
        headpiece.texOffs(55, 0).addBox(-4.0F, -32.25F, -1.05F, 8.0F, 4.0F, 9.0F, 0.3F, false);
        headpiece.texOffs(65, 13).addBox(-3.0F, -31.15F, -1.7F, 6.0F, 3.0F, 1.0F, 0.2001F, false);
        headpiece.texOffs(82, 13).addBox(-2.5F, -31.25F, 7.4F, 5.0F, 3.0F, 1.0F, 0.3001F, false);
        headpiece.texOffs(89, 0).addBox(-4.0F, -33.6F, -0.75F, 8.0F, 2.0F, 8.0F, -0.2999F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        

        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 24.0F, 0.0F);
        body.addChild(upperPart);
        

        torso = new ModelRenderer(this);
        torso.setPos(0.0F, 0.0F, 0.0F);
        upperPart.addChild(torso);
        torso.texOffs(5, 81).addBox(-4.5F, -14.0F, -2.5F, 9.0F, 1.0F, 5.0F, 0.0F, false);
        torso.texOffs(0, 64).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        torso.texOffs(0, 80).addBox(1.25F, -21.375F, -3.7F, 3.0F, 4.0F, 2.0F, -0.6F, false);
        torso.texOffs(0, 80).addBox(-4.35F, -21.375F, -3.7F, 3.0F, 4.0F, 2.0F, -0.6F, false);
        torso.texOffs(20, 64).addBox(-3.5F, -22.9F, -1.9F, 7.0F, 3.0F, 1.0F, 0.4F, false);
        torso.texOffs(24, 73).addBox(-2.5F, -20.0F, -2.2F, 5.0F, 6.0F, 1.0F, 0.0F, false);

        torso_r1 = new ModelRenderer(this);
        torso_r1.setPos(0.0F, -22.0F, 0.0F);
        torso.addChild(torso_r1);
        setRotationAngle(torso_r1, 0.0175F, 0.0F, 0.0F);
        torso_r1.texOffs(37, 70).addBox(-4.0F, -2.35F, -2.6F, 8.0F, 5.0F, 5.0F, 0.1F, false);

        theThing = new ModelRenderer(this);
        theThing.setPos(0.0F, -13.0F, -2.25F);
        torso.addChild(theThing);
        theThing.texOffs(36, 81).addBox(-1.45F, -0.25F, -0.6F, 3.0F, 4.0F, 1.0F, -0.25F, false);
        theThing.texOffs(44, 81).addBox(-1.45F, -0.25F, -0.5F, 3.0F, 4.0F, 1.0F, -0.25F, false);

        rightString = new ModelRenderer(this);
        rightString.setPos(-2.325F, -13.5F, -2.4F);
        torso.addChild(rightString);
        rightString.texOffs(73, 64).addBox(-2.375F, -0.2F, -0.4F, 3.0F, 5.0F, 4.0F, -0.2F, false);

        leftString = new ModelRenderer(this);
        leftString.setPos(2.425F, -13.5F, -2.4F);
        torso.addChild(leftString);
        leftString.texOffs(89, 64).addBox(-0.625F, -0.2F, -0.4F, 3.0F, 5.0F, 4.0F, -0.2F, false);

        loincloth = new ModelRenderer(this);
        loincloth.setPos(0.0F, -13.0F, 2.4F);
        torso.addChild(loincloth);
        loincloth.texOffs(76, 73).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 5.0F, 0.0F, 0.0F, false);

        leftPartLoincloth = new ModelRenderer(this);
        leftPartLoincloth.setPos(4.0F, 1.0F, 0.0F);
        loincloth.addChild(leftPartLoincloth);
        leftPartLoincloth.texOffs(82, 77).addBox(0.001F, -2.0F, -1.999F, 0.0F, 5.0F, 2.0F, 0.0F, false);

        rightPartLoincloth = new ModelRenderer(this);
        rightPartLoincloth.setPos(-4.0F, 1.0F, 0.0F);
        loincloth.addChild(rightPartLoincloth);
        rightPartLoincloth.texOffs(90, 77).addBox(-0.001F, -2.0F, -1.999F, 0.0F, 5.0F, 2.0F, 0.0F, true);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-6.0F, -22.0F, 0.0F);
        upperPart.addChild(rightArm);
        rightArm.texOffs(0, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, true);
        rightArm.texOffs(9, 103).addBox(-1.0F, 3.0F, 1.025F, 2.0F, 1.0F, 1.0F, 0.1F, true);
        rightArm.texOffs(16, 109).addBox(-2.0F, -2.2F, -2.0F, 4.0F, 5.0F, 4.0F, 0.1F, true);

        rightWing = new ModelRenderer(this);
        rightWing.setPos(-2.3F, -1.6F, -0.95F);
        rightArm.addChild(rightWing);
        setRotationAngle(rightWing, 0.3054F, 0.0F, 0.0F);
        rightWing.texOffs(18, 103).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 5.0F, 0.0F, true);
        rightWing.texOffs(15, 104).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightArmJoint);
        rightArmJoint.texOffs(0, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightForeArm);
        rightForeArm.texOffs(0, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, true);
        rightForeArm.texOffs(42, 97).addBox(-2.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, false);
        rightForeArm.texOffs(16, 118).addBox(-2.0F, 0.1F, -2.0F, 4.0F, 4.0F, 4.0F, 0.15F, true);
        rightForeArm.texOffs(32, 96).addBox(-3.0F, 2.6F, -1.5F, 2.0F, 3.0F, 3.0F, -0.5F, true);
        rightForeArm.texOffs(32, 90).addBox(-3.0F, 2.3F, -1.5F, 2.0F, 3.0F, 3.0F, -0.5F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(6.0F, -22.0F, 0.0F);
        upperPart.addChild(leftArm);
        leftArm.texOffs(32, 108).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        leftArm.texOffs(41, 103).addBox(-1.0F, 3.0F, 1.0F, 2.0F, 1.0F, 1.0F, 0.1F, false);
        leftArm.texOffs(48, 109).addBox(-2.0F, -2.2F, -2.0F, 4.0F, 5.0F, 4.0F, 0.1F, false);

        leftWing = new ModelRenderer(this);
        leftWing.setPos(2.3F, -1.6F, -0.95F);
        leftArm.addChild(leftWing);
        setRotationAngle(leftWing, 0.3054F, 0.0F, 0.0F);
        leftWing.texOffs(47, 104).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 1.0F, 3.0F, 0.0F, false);
        leftWing.texOffs(50, 103).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 5.0F, 0.0F, false);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftArmJoint);
        leftArmJoint.texOffs(32, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftForeArm);
        leftForeArm.texOffs(32, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        leftForeArm.texOffs(42, 97).addBox(1.5F, 5.1F, -2.0F, 1.0F, 1.0F, 4.0F, -0.2F, true);
        leftForeArm.texOffs(32, 90).addBox(1.0F, 2.3F, -1.5F, 2.0F, 3.0F, 3.0F, -0.5F, false);
        leftForeArm.texOffs(32, 96).addBox(1.0F, 2.6F, -1.5F, 2.0F, 3.0F, 3.0F, -0.5F, false);
        leftForeArm.texOffs(48, 118).addBox(-2.0F, 0.1F, -2.0F, 4.0F, 4.0F, 4.0F, 0.15F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        body.addChild(rightLeg);
        rightLeg.texOffs(64, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);
        rightLeg.texOffs(90, 112).addBox(-1.0F, 5.0F, -2.0F, 2.0F, 1.0F, 1.0F, 0.1F, false);

        rightLeg_r1 = new ModelRenderer(this);
        rightLeg_r1.setPos(-2.2F, 1.65F, 0.0F);
        rightLeg.addChild(rightLeg_r1);
        setRotationAngle(rightLeg_r1, 0.0F, 1.5708F, 0.0F);
        rightLeg_r1.texOffs(80, 108).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, -0.5F, false);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLegJoint);
        rightLegJoint.texOffs(64, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);
        rightLowerLeg.texOffs(64, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        rightLowerLeg.texOffs(80, 118).addBox(-2.0F, 0.1F, -2.0F, 4.0F, 5.0F, 4.0F, 0.15F, false);

        rightLowerLeg_r1 = new ModelRenderer(this);
        rightLowerLeg_r1.setPos(2.2F, 4.25F, -0.1F);
        rightLowerLeg.addChild(rightLowerLeg_r1);
        setRotationAngle(rightLowerLeg_r1, 0.0F, -1.5708F, 0.0F);
        rightLowerLeg_r1.texOffs(78, 119).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, -0.3F, false);

        rightLowerLeg_r2 = new ModelRenderer(this);
        rightLowerLeg_r2.setPos(-2.2F, 4.25F, -0.1F);
        rightLowerLeg.addChild(rightLowerLeg_r2);
        setRotationAngle(rightLowerLeg_r2, 0.0F, -1.5708F, 0.0F);
        rightLowerLeg_r2.texOffs(85, 115).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, -0.3F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        body.addChild(leftLeg);
        leftLeg.texOffs(96, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, true);
        leftLeg.texOffs(90, 112).addBox(-1.0F, 5.0F, -2.0F, 2.0F, 1.0F, 1.0F, 0.1F, true);

        leftLeg_r1 = new ModelRenderer(this);
        leftLeg_r1.setPos(2.2F, 1.65F, 0.0F);
        leftLeg.addChild(leftLeg_r1);
        setRotationAngle(leftLeg_r1, 0.0F, -1.5708F, 0.0F);
        leftLeg_r1.texOffs(112, 108).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 4.0F, 2.0F, -0.5F, true);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLegJoint);
        leftLegJoint.texOffs(96, 102).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);
        leftLowerLeg.texOffs(96, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
        leftLowerLeg.texOffs(112, 118).addBox(-2.0F, 0.1F, -2.0F, 4.0F, 5.0F, 4.0F, 0.15F, true);

        leftLowerLeg_r1 = new ModelRenderer(this);
        leftLowerLeg_r1.setPos(2.2F, 4.25F, -0.1F);
        leftLowerLeg.addChild(leftLowerLeg_r1);
        setRotationAngle(leftLowerLeg_r1, 0.0F, -1.5708F, 0.0F);
        leftLowerLeg_r1.texOffs(110, 119).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, -0.3F, false);

        leftLowerLeg_r2 = new ModelRenderer(this);
        leftLowerLeg_r2.setPos(-2.2F, 4.25F, -0.1F);
        leftLowerLeg.addChild(leftLowerLeg_r2);
        setRotationAngle(leftLowerLeg_r2, 0.0F, -1.5708F, 0.0F);
        leftLowerLeg_r2.texOffs(117, 115).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, -0.3F, false);
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