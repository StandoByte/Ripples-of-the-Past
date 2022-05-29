package com.github.standobyte.jojo.client.model.entity.stand;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class SilverChariotArmorLayerModel extends SilverChariotModel {
    private ModelRenderer rightTorsoTube;
    private ModelRenderer leftTorsoTube;
    private ModelRenderer leftShoulder;
    private ModelRenderer thorn1;
    private ModelRenderer thorn2;
    private ModelRenderer rightShoulder;
    private ModelRenderer thorn3;
    private ModelRenderer thorn4;
    
    public SilverChariotArmorLayerModel() {
        this(64, 64);
    }
    
    public SilverChariotArmorLayerModel(int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);
    }
    
    @Override
    protected void addLayerSpecificBoxes() {
        head.texOffs(24, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.1F, false);
        head.texOffs(12, 10).addBox(-1.0F, -8.0F, -4.0F, 2.0F, 2.0F, 8.0F, 0.25F, false);
        head.texOffs(32, 16).addBox(-2.0F, -9.0F, -4.25F, 4.0F, 4.0F, 1.0F, -0.75F, false);
        head.texOffs(44, 56).addBox(-0.5F, -6.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        torso.texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 6.0F, 4.0F, 0.1F, false);
        torso.texOffs(0, 40).addBox(-3.5F, 0.75F, -2.5F, 7.0F, 2.0F, 1.0F, 0.1F, false);
        torso.texOffs(0, 38).addBox(-3.5F, 2.9F, -2.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);
        torso.texOffs(4, 38).addBox(2.5F, 2.9F, -2.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);
        torso.texOffs(8, 38).addBox(-3.5F, 2.9F, 1.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);
        torso.texOffs(12, 38).addBox(2.5F, 2.9F, 1.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);
        torso.texOffs(0, 35).addBox(-3.5F, 0.75F, 1.5F, 7.0F, 2.0F, 1.0F, 0.1F, false);
        torso.texOffs(28, 38).addBox(-4.0F, 10.0F, -2.0F, 8.0F, 2.0F, 4.0F, 0.2F, false);

        rightTorsoTube = new ModelRenderer(this);
        rightTorsoTube.setPos(-0.75F, 9.0F, 0.5F);
        torso.addChild(rightTorsoTube);
        setRotationAngle(rightTorsoTube, 0.0F, 0.0F, -0.3491F);
        rightTorsoTube.texOffs(52, 30).addBox(-1.5F, -3.0F, -0.5F, 3.0F, 6.0F, 1.0F, 0.0F, false);

        leftTorsoTube = new ModelRenderer(this);
        leftTorsoTube.setPos(0.75F, 9.0F, 0.5F);
        torso.addChild(leftTorsoTube);
        setRotationAngle(leftTorsoTube, 0.0F, 0.0F, 0.3491F);
        leftTorsoTube.texOffs(52, 30).addBox(-1.5F, -3.0F, -0.5F, 3.0F, 6.0F, 1.0F, 0.0F, true);

		leftArm.texOffs(16, 44).addBox(-2.0F, -2.1F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
		leftArm.texOffs(0, 46).addBox(2.0F, 2.9F, -0.5F, 1.0F, 1.0F, 1.0F, 0.1F, true);

        leftShoulder = new ModelRenderer(this);
        leftShoulder.setPos(2.0F, 0.1F, 0.0F);
        leftArm.addChild(leftShoulder);
        setRotationAngle(leftShoulder, 0.7854F, 0.0F, 0.0F);
        leftShoulder.texOffs(48, 0).addBox(-0.5F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.0F, true);
        leftShoulder.texOffs(56, 8).addBox(0.0F, 2.0F, -1.5F, 1.0F, 4.0F, 3.0F, 0.0F, true);
        leftShoulder.texOffs(58, 19).addBox(0.0F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        leftShoulder.texOffs(50, 15).addBox(0.0F, -0.5F, -3.0F, 1.0F, 1.0F, 6.0F, 0.0F, true);

        leftForeArm.texOffs(16, 54).addBox(-2.0F, 0.1F, -2.0F, 4.0F, 6.0F, 4.0F, 0.099F, false);
        
        thorn1 = new ModelRenderer(this);
        thorn1.setPos(0.5F, -2.0F, -2.0F);
        leftShoulder.addChild(thorn1);
        setRotationAngle(thorn1, 0.7854F, 0.0F, 0.0F);
        thorn1.texOffs(58, 19).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        thorn2 = new ModelRenderer(this);
        thorn2.setPos(0.5F, -2.0F, 2.0F);
        leftShoulder.addChild(thorn2);
        setRotationAngle(thorn2, -0.7854F, 0.0F, 0.0F);
        thorn2.texOffs(58, 19).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        rightArm.texOffs(0, 44).addBox(-2.0F, -2.1F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        rightArm.texOffs(0, 46).addBox(-3.0F, 2.9F, -0.5F, 1.0F, 1.0F, 1.0F, 0.1F, false);

        rightShoulder = new ModelRenderer(this);
        rightShoulder.setPos(-2.5F, 0.1F, 0.0F);
        rightArm.addChild(rightShoulder);
        setRotationAngle(rightShoulder, 0.7854F, 0.0F, 0.0F);
        rightShoulder.texOffs(48, 0).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, 0.0F, false);
        rightShoulder.texOffs(56, 8).addBox(-0.5F, 2.0F, -1.5F, 1.0F, 4.0F, 3.0F, 0.0F, false);
        rightShoulder.texOffs(58, 19).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        rightShoulder.texOffs(50, 15).addBox(-0.5F, -0.5F, -3.0F, 1.0F, 1.0F, 6.0F, 0.0F, false);
        
        rightForeArm.texOffs(0, 54).addBox(-2.0F, 0.1F, -2.0F, 4.0F, 6.0F, 4.0F, 0.099F, false);

        thorn3 = new ModelRenderer(this);
        thorn3.setPos(0.0F, -2.0F, -2.0F);
        rightShoulder.addChild(thorn3);
        setRotationAngle(thorn3, 0.7854F, 0.0F, 0.0F);
        thorn3.texOffs(58, 19).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        thorn4 = new ModelRenderer(this);
        thorn4.setPos(0.0F, -2.0F, 2.0F);
        rightShoulder.addChild(thorn4);
        setRotationAngle(thorn4, -0.7854F, 0.0F, 0.0F);
        thorn4.texOffs(58, 19).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        leftLeg.texOffs(48, 44).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        leftLeg.texOffs(58, 22).addBox(1.85F, 0.0F, -1.0F, 1.0F, 4.0F, 2.0F, 0.0F, true);
        leftLeg.texOffs(48, 22).addBox(1.85F, 1.0F, -2.0F, 1.0F, 2.0F, 4.0F, 0.0F, true);

        leftLowerLeg.texOffs(48, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.099F, false);

        rightLeg.texOffs(32, 44).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.1F, false);
        rightLeg.texOffs(58, 22).addBox(-2.85F, 0.0F, -1.0F, 1.0F, 4.0F, 2.0F, 0.0F, false);
        rightLeg.texOffs(48, 22).addBox(-2.85F, 1.0F, -2.0F, 1.0F, 2.0F, 4.0F, 0.0F, false);

        rightLowerLeg.texOffs(32, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.099F, false);
    }
}
