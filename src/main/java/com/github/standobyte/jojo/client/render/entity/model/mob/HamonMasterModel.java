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
    
    public HamonMasterModel() {
        super(0, 0, 128, 128);
        
        // imitating the player model
        leftArm = new ModelRenderer(this, 32, 48);
        leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftLeg = new ModelRenderer(this, 16, 48);
        leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftSleeve = new ModelRenderer(this, 48, 48);
        leftSleeve.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F);
        leftSleeve.setPos(5.0F, 2.0F, 0.0F);
        rightSleeve = new ModelRenderer(this, 40, 32);
        rightSleeve.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F);
        rightSleeve.setPos(-5.0F, 2.0F, 10.0F);
        leftPants = new ModelRenderer(this, 0, 48);
        leftPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F);
        leftPants.setPos(1.9F, 12.0F, 0.0F);
        rightPants = new ModelRenderer(this, 0, 32);
        rightPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F);
        rightPants.setPos(-1.9F, 12.0F, 0.0F);
        jacket = new ModelRenderer(this, 16, 32);
        jacket.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F);
        jacket.setPos(0.0F, 0.0F, 0.0F);
        
        // FIXME !!! (hamon master model) cubes don't render when torso is bent
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(80, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 3.0F, 4.0F, 0.2F, false);
        body.texOffs(85, 33).addBox(-3.0F, 2.0F, -2.1F, 6.0F, 3.0F, 0.0F, 0.0F, false);
        body.texOffs(80, 23).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 6.0F, 4.0F, 0.25F, false);
        
        // FIXME !!! (hamon master model) child doesn't render when torso is bent
        rightCapeBinding = new ModelRenderer(this);
        rightCapeBinding.setPos(-2.0F, 12.0F, 0.0F);
        body.addChild(rightCapeBinding);
        ClientUtil.setRotationAngle(rightCapeBinding, 0.0F, 0.0F, 0.0873F);
        rightCapeBinding.texOffs(100, 36).addBox(-2.0F, -0.8F, -2.0F, 4.0F, 3.0F, 4.0F, 0.225F, false);
        
        // FIXME !!! (hamon master model) child doesn't render when torso is bent
        rightCape = new ModelRenderer(this);
        rightCape.setPos(-2.0F, 12.0F, 2.0F);
        body.addChild(rightCape);
        ClientUtil.setRotationAngle(rightCape, 0.2182F, 0.0F, 0.1745F);
        rightCape.texOffs(96, 45).addBox(-2.0F, 0.0F, -0.35F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        rightCape.texOffs(104, 40).addBox(-2.0F, 0.0F, -5.35F, 0.0F, 4.0F, 5.0F, 0.0F, false);
        
        lowRightCape = new ModelRenderer(this);
        lowRightCape.setPos(-2.0F, 4.0F, -0.35F);
        rightCape.addChild(lowRightCape);
        lowRightCape.texOffs(104, 44).addBox(0.0F, 0.0F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, false);
        lowRightCape.texOffs(96, 49).addBox(0.0F, 0.0F, 0.0F, 4.0F, 7.0F, 0.0F, 0.0F, false);
        
        // FIXME !!! (hamon master model) child doesn't render when torso is bent
        leftCapeBinding = new ModelRenderer(this);
        leftCapeBinding.setPos(2.0F, 12.0F, 0.0F);
        body.addChild(leftCapeBinding);
        ClientUtil.setRotationAngle(leftCapeBinding, 0.0F, 0.0F, -0.0873F);
        leftCapeBinding.texOffs(68, 36).addBox(-2.0F, -0.8F, -2.0F, 4.0F, 3.0F, 4.0F, 0.225F, false);
        
        // FIXME !!! (hamon master model) child doesn't render when torso is bent
        leftCape = new ModelRenderer(this);
        leftCape.setPos(2.0F, 12.0F, 2.0F);
        body.addChild(leftCape);
        ClientUtil.setRotationAngle(leftCape, 0.2182F, 0.0F, -0.1745F);
        leftCape.texOffs(70, 40).addBox(2.0F, 0.0F, -5.35F, 0.0F, 4.0F, 5.0F, 0.0F, false);
        leftCape.texOffs(80, 45).addBox(-2.0F, 0.0F, -0.35F, 4.0F, 4.0F, 0.0F, 0.0F, true);
        
        lowLeftCape = new ModelRenderer(this);
        lowLeftCape.setPos(2.0F, 4.0F, -0.35F);
        leftCape.addChild(lowLeftCape);
        lowLeftCape.texOffs(70, 44).addBox(0.0F, 0.0F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, false);
        lowLeftCape.texOffs(80, 49).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 7.0F, 0.0F, 0.0F, true);
        
        // FIXME !!! (hamon master model) child doesn't render when right arm is bent
        ModelRenderer rightShoulder = new ModelRenderer(this);
        rightShoulder.setPos(5.0F, 22.2F, 0.0F);
        rightArm.addChild(rightShoulder);
        ClientUtil.setRotationAngle(rightShoulder, 0.0F, 0.0F, -0.0873F);
        rightShoulder.texOffs(104, 16).addBox(-5.9F, -24.6F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
        
        // FIXME !!! (hamon master model) child doesn't render when right arm is bent
        ModelRenderer rightCuff = new ModelRenderer(this);
        rightCuff.setPos(5.0F, 30.0F, 0.0F);
        rightArm.addChild(rightCuff);
        ClientUtil.setRotationAngle(rightCuff, 0.0F, 0.0F, 0.0436F);
        rightCuff.texOffs(104, 24).addBox(-9.0F, -23.7F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
        
        // FIXME !!! (hamon master model) child doesn't render when left arm is bent
        ModelRenderer leftShoulder = new ModelRenderer(this);
        leftShoulder.setPos(-5.0F, 22.2F, 0.0F);
        leftArm.addChild(leftShoulder);
        ClientUtil.setRotationAngle(leftShoulder, 0.0F, 0.0F, 0.0873F);
        leftShoulder.texOffs(64, 16).addBox(1.9F, -24.6F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
        
        // FIXME !!! (hamon master model) child doesn't render when left arm is bent
        ModelRenderer leftCuff = new ModelRenderer(this);
        leftCuff.setPos(-5.0F, 30.0F, 0.0F);
        leftArm.addChild(leftCuff);
        ClientUtil.setRotationAngle(leftCuff, 0.0F, 0.0F, -0.0436F);
        leftCuff.texOffs(64, 24).addBox(5.0F, -23.7F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
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

    // FIXME !!! (hamon master model) pose without bendy-lib
    // FIXME !!! (hamon master model) pose without playerAnimator
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
        setupOuterLayer();
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
    
    private void setupOuterLayer() {
        leftPants.copyFrom(leftLeg);
        rightPants.copyFrom(rightLeg);
        leftSleeve.copyFrom(leftArm);
        rightSleeve.copyFrom(rightArm);
        jacket.copyFrom(body);
    }

    @Override
    public void setAllVisible(boolean isVisible) {
        super.setAllVisible(isVisible);
        leftSleeve.visible = isVisible;
        rightSleeve.visible = isVisible;
        leftPants.visible = isVisible;
        rightPants.visible = isVisible;
        jacket.visible = isVisible;
    }
    
    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(leftPants, rightPants, leftSleeve, rightSleeve, jacket));
    }
}
