package com.github.standobyte.jojo.client.render.entity.model.mob;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.IEntityAnimApplier;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class HamonMasterModel extends BipedModel<HamonMasterEntity> {
    private IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> sittingAnim;
    private boolean animInit = false;
    
    public final ModelRenderer leftSleeve;
    public final ModelRenderer rightSleeve;
    public final ModelRenderer leftPants;
    public final ModelRenderer rightPants;
    public final ModelRenderer jacket;
    
    private final ModelRenderer rightCapeBinding;
    private final ModelRenderer rightCape;
    private final ModelRenderer lowRightCape;
    private final ModelRenderer leftCapeBinding;
    private final ModelRenderer leftCape;
    private final ModelRenderer lowLeftCape;
    
    public HamonMasterModel(boolean isExtraLayer) {
        super(0, 0, 64, 64);
        
        
        // emulating PlayerModel
        // subclassing PlayerModel wouldn't work with the animations
        leftArm = new ModelRenderer(this, 32, 48);
        leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftSleeve = new ModelRenderer(this, 48, 48);
        leftSleeve.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F);
        leftSleeve.setPos(5.0F, 2.0F, 0.0F);
        rightSleeve = new ModelRenderer(this, 40, 32);
        rightSleeve.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F);
        rightSleeve.setPos(-5.0F, 2.0F, 10.0F);
        leftLeg = new ModelRenderer(this, 16, 48);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftPants = new ModelRenderer(this, 0, 48);
        leftPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F);
        leftPants.setPos(1.9F, 12.0F, 0.0F);
        rightPants = new ModelRenderer(this, 0, 32);
        rightPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F);
        rightPants.setPos(-1.9F, 12.0F, 0.0F);
        jacket = new ModelRenderer(this, 16, 32);
        jacket.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F);
        jacket.setPos(0.0F, 0.0F, 0.0F);
        
        
        rightCapeBinding = new ModelRenderer(this);
        rightCapeBinding.setPos(-2.0F, 12.0F, 0.0F);
        body.addChild(rightCapeBinding);
        ClientUtil.setRotationAngle(rightCapeBinding, 0.0F, 0.0F, 0.0873F);
        
        rightCape = new ModelRenderer(this);
        rightCape.setPos(-2.0F, 12.0F, 2.0F);
        body.addChild(rightCape);
        ClientUtil.setRotationAngle(rightCape, 0.2182F, 0.0F, 0.1745F);

        lowRightCape = new ModelRenderer(this);
        lowRightCape.setPos(-2.0F, 4.0F, -0.35F);
        rightCape.addChild(lowRightCape);

        leftCapeBinding = new ModelRenderer(this);
        leftCapeBinding.setPos(2.0F, 12.0F, 0.0F);
        body.addChild(leftCapeBinding);
        ClientUtil.setRotationAngle(leftCapeBinding, 0.0F, 0.0F, -0.0873F);

        leftCape = new ModelRenderer(this);
        leftCape.setPos(2.0F, 12.0F, 2.0F);
        body.addChild(leftCape);
        ClientUtil.setRotationAngle(leftCape, 0.2182F, 0.0F, -0.1745F);
        
        lowLeftCape = new ModelRenderer(this);
        lowLeftCape.setPos(2.0F, 4.0F, -0.35F);
        leftCape.addChild(lowLeftCape);
        
        if (isExtraLayer) {
            ClientUtil.clearCubes(head);
            ClientUtil.clearCubes(hat);
            ClientUtil.clearCubes(body);
            ClientUtil.clearCubes(jacket);
            ClientUtil.clearCubes(leftArm);
            ClientUtil.clearCubes(leftSleeve);
            ClientUtil.clearCubes(rightArm);
            ClientUtil.clearCubes(rightSleeve);
            ClientUtil.clearCubes(leftLeg);
            ClientUtil.clearCubes(leftPants);
            ClientUtil.clearCubes(rightLeg);
            ClientUtil.clearCubes(rightPants);
            
//            body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 2.0F, 4.0F, 0.2F, false);
//            body.texOffs(21, 22).addBox(-3.0F, 1.9F, -2.1F, 6.0F, 3.0F, 0.0F, 0.0F, false);
            
            rightCapeBinding.texOffs(36, 36).addBox(-2.0F, -0.8F, -2.0F, 4.0F, 3.0F, 4.0F, 0.225F, false);
            
            rightCape.texOffs(36, 45).addBox(-2.0F, 0.0F, -0.35F, 4.0F, 4.0F, 0.0F, 0.0F, false);
            rightCape.texOffs(44, 40).addBox(-2.0F, 0.0F, -5.35F, 0.0F, 4.0F, 5.0F, 0.0F, false);
            
            lowRightCape.texOffs(44, 44).addBox(0.0F, 0.0F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, false);
            lowRightCape.texOffs(36, 49).addBox(0.0F, 0.0F, 0.0F, 4.0F, 7.0F, 0.0F, 0.0F, false);
            
            leftCapeBinding.texOffs(4, 36).addBox(-2.0F, -0.8F, -2.0F, 4.0F, 3.0F, 4.0F, 0.225F, false);
            
            leftCape.texOffs(2, 40).addBox(2.0F, 0.0F, -5.35F, 0.0F, 4.0F, 5.0F, 0.0F, false);
            leftCape.texOffs(12, 45).addBox(-2.0F, 0.0F, -0.35F, 4.0F, 4.0F, 0.0F, 0.0F, false);
            
            lowLeftCape.texOffs(2, 44).addBox(0.0F, 0.0F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, false);
            lowLeftCape.texOffs(12, 49).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 7.0F, 0.0F, 0.0F, false);
            
//            ModelRenderer rightShoulder = new ModelRenderer(this);
//            rightShoulder.setPos(-1.0F, -1.0F, 0.0F);
//            rightArm.addChild(rightShoulder);
//            ClientUtil.setRotationAngle(rightShoulder, 0.0F, 0.0F, -0.0873F);
//            rightShoulder.texOffs(40, 16).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
//
//            ModelRenderer rightCuff = new ModelRenderer(this);
//            rightCuff.setPos(-1.0F, 7.0F, 0.0F);
//            rightArm.addChild(rightCuff);
//            ClientUtil.setRotationAngle(rightCuff, 0.0F, 0.0F, 0.0436F);
//            rightCuff.texOffs(40, 24).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
//
//            ModelRenderer leftShoulder = new ModelRenderer(this);
//            leftShoulder.setPos(1.0F, -1.0F, 0.0F);
//            leftArm.addChild(leftShoulder);
//            ClientUtil.setRotationAngle(leftShoulder, 0.0F, 0.0F, 0.0873F);
//            leftShoulder.texOffs(0, 16).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
//
//            ModelRenderer leftCuff = new ModelRenderer(this);
//            leftCuff.setPos(1.0F, 7.0F, 0.0F);
//            leftArm.addChild(leftCuff);
//            ClientUtil.setRotationAngle(leftCuff, 0.0F, 0.0F, -0.0436F);
//            leftCuff.texOffs(0, 24).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
        }
    }
    
    public void initPose() {
        if (!animInit) {
            sittingAnim = PlayerAnimationHandler.getPlayerAnimator().initHamonMasterPose(this);
            animInit = true;
        }
    }
    
    public void setupPoseRotations(MatrixStack matrixStack, float partialTick) {
        if (sittingAnim != null) {
            sittingAnim.applyBodyTransforms(matrixStack, partialTick);
        }
    }
    
    // FIXME !!! (hamon master model) pose without bendy-lib
    // FIXME !!! (hamon master model) pose without playerAnimator
    @Override
    public void setupAnim(HamonMasterEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        setDefaultPivot();
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        if (PlayerAnimationHandler.getPlayerAnimator().kosmXAnimatorInstalled()) {
            ClientUtil.setRotationAngleDegrees(leftCapeBinding,     35.298F,            2.2454F,    4.6285F - 5);
            ClientUtil.setRotationAngleDegrees(leftCape,            56.8726F + 12.5F,   6.8027F,    9.3505F - 10);
            ClientUtil.setRotationAngleDegrees(lowLeftCape,         14.85F,             0F,         0F);
            ClientUtil.setRotationAngleDegrees(rightCapeBinding,    37.5939F,           -0.8553F,   -3.8097F + 5);
            ClientUtil.setRotationAngleDegrees(rightCape,           52.5177F + 12.5F,   -9.1032F,   -9.1693F + 10);
            ClientUtil.setRotationAngleDegrees(lowRightCape,        21.39F,             0F,         0F);
            if (sittingAnim != null) {
                sittingAnim.setEmote();
            }
        }
        setupOuterLayer();
    }
    
    public void copyPropertiesTo(HamonMasterModel model) {
        super.copyPropertiesTo(model);
        model.leftCapeBinding.copyFrom(this.leftCapeBinding);
        model.leftCape.copyFrom(this.leftCape);
        model.lowLeftCape.copyFrom(this.lowLeftCape);
        model.rightCapeBinding.copyFrom(this.rightCapeBinding);
        model.rightCape.copyFrom(this.rightCape);
        model.lowRightCape.copyFrom(this.lowRightCape);
    }
    
    @Override
    public void setAllVisible(boolean pVisible) {
        super.setAllVisible(pVisible);
        leftSleeve.visible = pVisible;
        rightSleeve.visible = pVisible;
        leftPants.visible = pVisible;
        rightPants.visible = pVisible;
        jacket.visible = pVisible;
    }
    
    
    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(leftPants, rightPants, leftSleeve, rightSleeve, jacket));
    }
    
    private void setupOuterLayer() {
        leftPants.copyFrom(leftLeg);
        rightPants.copyFrom(rightLeg);
        leftSleeve.copyFrom(leftArm);
        rightSleeve.copyFrom(rightArm);
        jacket.copyFrom(body);
    }
    
    private void setDefaultPivot() {
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        head.setPos(0.0F, 0.0F, 0.0F);
        rightArm.z = 0.0F;
        rightArm.x = -5.0F;
        leftArm.z = 0.0F;
        leftArm.x = 5.0F;
        body.xRot = 0.0F;
        rightLeg.z = 0.1F;
        leftLeg.z = 0.1F;
        rightLeg.y = 12.0F;
        leftLeg.y = 12.0F;
        head.y = 0.0F;
        head.zRot = 0f;
        body.y = 0.0F;
        body.x = 0f;
        body.z = 0f;
        body.yRot = 0;
        body.zRot = 0;
    }
}
