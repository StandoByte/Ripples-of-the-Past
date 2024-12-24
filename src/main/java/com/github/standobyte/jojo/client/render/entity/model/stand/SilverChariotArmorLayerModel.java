package com.github.standobyte.jojo.client.render.entity.model.stand;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class SilverChariotArmorLayerModel extends SilverChariotModel {
    private ModelRenderer rightTorsoTube;
    private ModelRenderer leftTorsoTube;
    private ModelRenderer leftShoulder;
    private ModelRenderer rightShoulder;

    public SilverChariotArmorLayerModel() {
        super();
        isLayerModel = true;

        root = new ModelRenderer(this);
        root.setPos(0.0F, 24.0F, 0.0F);
        

        head = new ModelRenderer(this);
        head.setPos(0.0F, -24.0F, 0.0F);
        root.addChild(head);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(32, 0).addBox(-1.0F, -8.0F, -4.0F, 2.0F, 2.0F, 8.0F, 0.15F, false);
        head.texOffs(32, 10).addBox(-2.0F, -9.0F, -4.25F, 4.0F, 4.0F, 1.0F, -0.85F, false);
        head.texOffs(42, 10).addBox(-0.5F, -6.5F, -4.5F, 1.0F, 1.0F, 1.0F, -0.1F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, -24.0F, 0.0F);
        root.addChild(body);
        

        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        body.addChild(upperPart);
        

        torso = new ModelRenderer(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);
        torso.texOffs(0, 64).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 6.0F, 4.0F, 0.1F, false);
        torso.texOffs(20, 65).addBox(-3.5F, 0.75F, -2.5F, 7.0F, 2.0F, 1.0F, 0.1F, false);
        torso.texOffs(24, 68).addBox(-3.5F, 2.9F, -2.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);
        torso.texOffs(28, 68).addBox(2.5F, 2.9F, -2.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);
        torso.texOffs(24, 70).addBox(-3.5F, 2.9F, 1.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);
        torso.texOffs(28, 70).addBox(2.5F, 2.9F, 1.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);
        torso.texOffs(20, 62).addBox(-3.5F, 0.75F, 1.5F, 7.0F, 2.0F, 1.0F, 0.1F, false);
        torso.texOffs(0, 81).addBox(-4.0F, 10.0F, -2.0F, 8.0F, 2.0F, 4.0F, 0.2F, false);

        rightTorsoTube = new ModelRenderer(this);
        rightTorsoTube.setPos(-0.75F, 9.0F, 0.0F);
        torso.addChild(rightTorsoTube);
        setRotationAngle(rightTorsoTube, 0.0F, 0.0F, -0.3491F);
        rightTorsoTube.texOffs(0, 74).addBox(-1.5F, -3.0F, -0.5F, 3.0F, 6.0F, 1.0F, 0.0F, false);
        rightTorsoTube.texOffs(18, 75).addBox(-0.5F, -2.0F, -0.5F, 2.0F, 5.0F, 1.0F, 0.0F, false);

        leftTorsoTube = new ModelRenderer(this);
        leftTorsoTube.setPos(0.75F, 9.0F, 0.0F);
        torso.addChild(leftTorsoTube);
        setRotationAngle(leftTorsoTube, 0.0F, 0.0F, 0.3491F);
        leftTorsoTube.texOffs(9, 74).addBox(-1.5F, -3.0F, -0.5F, 3.0F, 6.0F, 1.0F, 0.0F, true);
        leftTorsoTube.texOffs(25, 75).addBox(-1.5F, -2.0F, -0.5F, 2.0F, 5.0F, 1.0F, 0.0F, true);

        leftArm = convertLimb(new ModelRenderer(this));
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);
        leftArm.texOffs(32, 108).addBox(-2.0F, -2.1F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        leftArm.texOffs(44, 120).addBox(2.0F, 2.9F, -0.5F, 1.0F, 1.0F, 1.0F, 0.1F, true);

        leftShoulder = new ModelRenderer(this);
        leftShoulder.setPos(2.0F, 0.0F, 0.0F);
        leftArm.addChild(leftShoulder);
        setRotationAngle(leftShoulder, 0.7854F, 0.0F, 0.0F);
        leftShoulder.texOffs(48, 113).addBox(-0.5F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.0F, true);
        leftShoulder.texOffs(48, 121).addBox(0.0F, 2.0F, -1.5F, 1.0F, 4.0F, 3.0F, 0.0F, true);
        leftShoulder.texOffs(56, 108).addBox(0.0F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        leftShoulder.texOffs(48, 106).addBox(0.0F, -0.5F, -3.0F, 1.0F, 1.0F, 6.0F, 0.0F, true);

        ModelRenderer bone6;
        ModelRenderer bone5;
        
        bone6 = new ModelRenderer(this);
        bone6.setPos(0.5F, -2.0F, -2.0F);
        leftShoulder.addChild(bone6);
        setRotationAngle(bone6, 0.7854F, 0.0F, 0.0F);
        bone6.texOffs(50, 110).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone5 = new ModelRenderer(this);
        bone5.setPos(0.5F, -2.0F, 2.0F);
        leftShoulder.addChild(bone5);
        setRotationAngle(bone5, -0.7854F, 0.0F, 0.0F);
        bone5.texOffs(56, 110).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftForeArm);
        leftForeArm.texOffs(32, 118).addBox(-2.0F, 0.1F, -2.0F, 4.0F, 6.0F, 4.0F, 0.099F, false);

        rightArm = convertLimb(new ModelRenderer(this));
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);
        rightArm.texOffs(0, 108).addBox(-2.0F, -2.1F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        rightArm.texOffs(12, 120).addBox(-3.0F, 2.9F, -0.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);

        rightShoulder = new ModelRenderer(this);
        rightShoulder.setPos(-2.5F, 0.0F, 0.0F);
        rightArm.addChild(rightShoulder);
        setRotationAngle(rightShoulder, 0.7854F, 0.0F, 0.0F);
        rightShoulder.texOffs(16, 113).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.0F, false);
        rightShoulder.texOffs(16, 121).addBox(-0.5F, 2.0F, -1.5F, 1.0F, 4.0F, 3.0F, 0.0F, false);
        rightShoulder.texOffs(24, 108).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        rightShoulder.texOffs(16, 106).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 6.0F, 0.0F, false);
        
        ModelRenderer bone3;
        ModelRenderer bone4;

        bone3 = new ModelRenderer(this);
        bone3.setPos(0.0F, -2.0F, -2.0F);
        rightShoulder.addChild(bone3);
        setRotationAngle(bone3, 0.7854F, 0.0F, 0.0F);
        bone3.texOffs(24, 110).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone4 = new ModelRenderer(this);
        bone4.setPos(0.0F, -2.0F, 2.0F);
        rightShoulder.addChild(bone4);
        setRotationAngle(bone4, -0.7854F, 0.0F, 0.0F);
        bone4.texOffs(18, 110).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightForeArm);
        rightForeArm.texOffs(0, 118).addBox(-2.0F, 0.1F, -2.0F, 4.0F, 6.0F, 4.0F, 0.099F, false);

        leftLeg = convertLimb(new ModelRenderer(this));
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        body.addChild(leftLeg);
        leftLeg.texOffs(96, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        leftLeg.texOffs(112, 108).addBox(1.85F, 0.0F, -1.0F, 1.0F, 4.0F, 2.0F, 0.0F, true);
        leftLeg.texOffs(112, 114).addBox(1.85F, 1.0F, -2.0F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);
        leftLowerLeg.texOffs(96, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.099F, false);

        rightLeg = convertLimb(new ModelRenderer(this));
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        body.addChild(rightLeg);
        rightLeg.texOffs(64, 108).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        rightLeg.texOffs(80, 108).addBox(-2.85F, 0.0F, -1.0F, 1.0F, 4.0F, 2.0F, 0.0F, false);
        rightLeg.texOffs(80, 114).addBox(-2.85F, 1.0F, -2.0F, 1.0F, 2.0F, 4.0F, 0.0F, false);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);
        rightLowerLeg.texOffs(64, 118).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.099F, false);
    }
    
}
