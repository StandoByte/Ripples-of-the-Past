package com.github.standobyte.jojo.client.render.entity.model.mob;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.IEntityAnimApplier;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class HamonMasterModel extends BipedModel<HamonMasterEntity> {
    private IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> sittingAnim;
    private boolean animInit = false;
    
    private final ModelRenderer rightCapeBinding;
    private final ModelRenderer rightCape;
    private final ModelRenderer lowRightCape;
    private final ModelRenderer leftCapeBinding;
    private final ModelRenderer leftCape;
    private final ModelRenderer lowLeftCape;
    
    public HamonMasterModel() {
        super(0, 0, 64, 64);
        ClientUtil.clearCubes(hat);
        
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(32, 44).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 3.0F, 4.0F, 0.2F, false);
        body.texOffs(37, 61).addBox(-3.0F, 2.0F, -2.1F, 6.0F, 3.0F, 0.0F, 0.0F, false);
        body.texOffs(32, 51).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 6.0F, 4.0F, 0.25F, false);

        rightCapeBinding = new ModelRenderer(this);
        rightCapeBinding.setPos(-2.0F, 12.0F, 0.0F);
        body.addChild(rightCapeBinding);
        
        ModelRenderer rightCapeBindingCube = new ModelRenderer(this);
        rightCapeBinding.addChild(rightCapeBindingCube);
        ClientUtil.setRotationAngle(rightCapeBindingCube, 0.0F, 0.0F, 0.0873F);
        rightCapeBindingCube.texOffs(17, 57).addBox(-2.0F, -0.8F, -2.0F, 4.0F, 3.0F, 4.0F, 0.225F, false);

        rightCape = new ModelRenderer(this);
        rightCape.setPos(-2.0F, 12.0F, 2.0F);
        body.addChild(rightCape);
        ClientUtil.setRotationAngle(rightCape, 0.2182F, 0.0F, 0.1745F);
        rightCape.texOffs(12, 47).addBox(-2.0F, 0.0F, -0.35F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        rightCape.texOffs(11, 38).addBox(-2.0F, 0.0F, -5.35F, 0.0F, 4.0F, 5.0F, 0.0F, false);

        lowRightCape = new ModelRenderer(this);
        lowRightCape.setPos(-2.0F, 4.0F, -0.35F);
        rightCape.addChild(lowRightCape);
        lowRightCape.texOffs(11, 46).addBox(0.0F, 0.0F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, false);
        lowRightCape.texOffs(23, 50).addBox(0.0F, 0.0F, 0.0F, 4.0F, 7.0F, 0.0F, 0.0F, false);

        leftCapeBinding = new ModelRenderer(this);
        leftCapeBinding.setPos(2.0F, 12.0F, 0.0F);
        body.addChild(leftCapeBinding);
        
        ModelRenderer leftCapeBindingCube = new ModelRenderer(this);
        leftCapeBinding.addChild(leftCapeBindingCube);
        ClientUtil.setRotationAngle(leftCapeBindingCube, 0.0F, 0.0F, -0.0873F);
        leftCapeBindingCube.texOffs(17, 57).addBox(-2.0F, -0.8F, -2.0F, 4.0F, 3.0F, 4.0F, 0.225F, true);

        leftCape = new ModelRenderer(this);
        leftCape.setPos(2.0F, 12.0F, 2.0F);
        body.addChild(leftCape);
        ClientUtil.setRotationAngle(leftCape, 0.2182F, 0.0F, -0.1745F);
        leftCape.texOffs(0, 38).addBox(2.0F, 0.0F, -5.35F, 0.0F, 4.0F, 5.0F, 0.0F, false);
        leftCape.texOffs(1, 47).addBox(-2.0F, 0.0F, -0.35F, 4.0F, 4.0F, 0.0F, 0.0F, false);

        lowLeftCape = new ModelRenderer(this);
        lowLeftCape.setPos(2.0F, 4.0F, -0.35F);
        leftCape.addChild(lowLeftCape);
        lowLeftCape.texOffs(0, 46).addBox(0.0F, 0.0F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, false);
        lowLeftCape.texOffs(23, 43).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 7.0F, 0.0F, 0.0F, false);

        ModelRenderer rightShoulder = new ModelRenderer(this);
        rightShoulder.setPos(5.0F, 22.2F, 0.0F);
        rightArm.addChild(rightShoulder);
        ClientUtil.setRotationAngle(rightShoulder, 0.0F, 0.0F, -0.0873F);
        rightShoulder.texOffs(40, 4).addBox(-5.9F, -24.6F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);

        ModelRenderer rightCuff = new ModelRenderer(this);
        rightCuff.setPos(5.0F, 30.0F, 0.0F);
        rightArm.addChild(rightCuff);
        ClientUtil.setRotationAngle(rightCuff, 0.0F, 0.0F, 0.0436F);
        rightCuff.texOffs(40, 32).addBox(-9.0F, -23.7F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);

        ModelRenderer leftShoulder = new ModelRenderer(this);
        leftShoulder.setPos(-5.0F, 22.2F, 0.0F);
        leftArm.addChild(leftShoulder);
        ClientUtil.setRotationAngle(leftShoulder, 0.0F, 0.0F, 0.0873F);
        leftShoulder.texOffs(40, 10).addBox(1.9F, -24.6F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);

        ModelRenderer leftCuff = new ModelRenderer(this);
        leftCuff.setPos(-5.0F, 30.0F, 0.0F);
        leftArm.addChild(leftCuff);
        ClientUtil.setRotationAngle(leftCuff, 0.0F, 0.0F, -0.0436F);
        leftCuff.texOffs(40, 38).addBox(5.0F, -23.7F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);

        rightLeg.texOffs(16, 32).addBox(-2.1F, 4.0F, -2.0F, 4.0F, 7.0F, 4.0F, 0.25F, false);

        leftLeg.texOffs(0, 32).addBox(-1.9F, 4.0F, -2.0F, 4.0F, 7.0F, 4.0F, 0.25F, false);
    }
    
    public void initPose() {
        if (!animInit) {
            sittingAnim = PlayerAnimationHandler.getPlayerAnimator().initHamonMasterPose(this);
            animInit = true;
        }
    }
    
    public void setupPoseRotations(MatrixStack matrixStack, float partialTick) {
        sittingAnim.applyBodyTransforms(matrixStack, partialTick);
    }

    @Override
    public void setupAnim(HamonMasterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        setDefaultPivot();
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        ClientUtil.setRotationAngleDegrees(leftCapeBinding,     35.298F,            2.2454F,    4.6285F - 5);
        ClientUtil.setRotationAngleDegrees(leftCape,            56.8726F + 12.5F,   6.8027F,    9.3505F - 10);
        ClientUtil.setRotationAngleDegrees(lowLeftCape,         14.85F,             0F,         0F);
        ClientUtil.setRotationAngleDegrees(rightCapeBinding,    37.5939F,           -0.8553F,   -3.8097F + 5);
        ClientUtil.setRotationAngleDegrees(rightCape,           52.5177F + 12.5F,   -9.1032F,   -9.1693F + 10);
        ClientUtil.setRotationAngleDegrees(lowRightCape,        21.39F,             0F,         0F);
        sittingAnim.setEmote();
    }

    private void setDefaultPivot(){
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.rightArm.z = 0.0F;
        this.rightArm.x = -5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        this.body.xRot = 0.0F;
        this.rightLeg.z = 0.1F;
        this.leftLeg.z = 0.1F;
        this.rightLeg.y = 12.0F;
        this.leftLeg.y = 12.0F;
        this.head.y = 0.0F;
        this.head.zRot = 0f;
        this.body.y = 0.0F;
        this.body.x = 0f;
        this.body.z = 0f;
        this.body.yRot = 0;
        this.body.zRot = 0;
    }
}
