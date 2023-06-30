package com.github.standobyte.jojo.client.render.entity.model.mob;

import com.github.standobyte.jojo.client.ClientTicking;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.IEntityAnimApplier;
import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class HamonMasterModel extends BipedModel<HamonMasterEntity> implements ClientTicking.ITicking {
    private IEntityAnimApplier<HamonMasterEntity, HamonMasterModel> sittingAnim;
    private boolean animInit = false;
    
    private final ModelRenderer rightCapeBinding;
    private final ModelRenderer rightCapeBinding_r1;
    private final ModelRenderer rightCape;
    private final ModelRenderer rightCape_r1;
    private final ModelRenderer lowRightCape;
    private final ModelRenderer lowRightCape_r1;
    private final ModelRenderer leftCapeBinding;
    private final ModelRenderer leftCapeBinding_r1;
    private final ModelRenderer leftCape;
    private final ModelRenderer leftCape_r1;
    private final ModelRenderer lowLeftCape;
    private final ModelRenderer lowLeftCape_r1;
    private final ModelRenderer rightArm_r1;
    private final ModelRenderer rightArm_r2;
    private final ModelRenderer leftArm_r1;
    private final ModelRenderer leftArm_r2;
    public HamonMasterModel() {
        super(0, 0, 64, 64);
        
        ClientUtil.clearCubes(hat);
        
        body.texOffs(32, 44).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 3.0F, 4.0F, 0.2F, false);
        body.texOffs(37, 61).addBox(-3.0F, 2.0F, -2.1F, 6.0F, 3.0F, 0.0F, 0.0F, false);
        body.texOffs(32, 51).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 6.0F, 4.0F, 0.25F, false);
        
        rightCapeBinding = new ModelRenderer(this);
        rightCapeBinding.setPos(-2.0F, 12.0F, 0.0F);
        body.addChild(rightCapeBinding);
        
        
        rightCapeBinding_r1 = new ModelRenderer(this);
        rightCapeBinding_r1.setPos(2.0F, 11.4F, 0.0F);
        rightCapeBinding.addChild(rightCapeBinding_r1);
        ClientUtil.setRotationAngle(rightCapeBinding_r1, 0.0F, 0.0F, 0.0873F);
        rightCapeBinding_r1.texOffs(17, 57).addBox(-5.0F, -12.0F, -2.0F, 4.0F, 3.0F, 4.0F, 0.225F, false);
        
        rightCape = new ModelRenderer(this);
        rightCape.setPos(-2.0F, 12.0F, 1.999F);
        body.addChild(rightCape);
        
        
        rightCape_r1 = new ModelRenderer(this);
        rightCape_r1.setPos(-2.0F, 0.0F, -0.3F);
        rightCape.addChild(rightCape_r1);
        ClientUtil.setRotationAngle(rightCape_r1, 0.2182F, 0.0F, 0.1745F);
        rightCape_r1.texOffs(12, 47).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        rightCape_r1.texOffs(11, 38).addBox(0.0F, 0.0F, -5.0F, 0.0F, 4.0F, 5.0F, 0.0F, false);
        
        lowRightCape = new ModelRenderer(this);
        lowRightCape.setPos(-1.0F, 4.0F, -0.3F);
        rightCape.addChild(lowRightCape);
        
        
        lowRightCape_r1 = new ModelRenderer(this);
        lowRightCape_r1.setPos(-1.0F, -4.0F, 0.0F);
        lowRightCape.addChild(lowRightCape_r1);
        ClientUtil.setRotationAngle(lowRightCape_r1, 0.2182F, 0.0F, 0.1745F);
        lowRightCape_r1.texOffs(11, 46).addBox(0.0F, 4.0F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, false);
        lowRightCape_r1.texOffs(23, 50).addBox(0.0F, 4.0F, 0.0F, 4.0F, 7.0F, 0.0F, 0.0F, false);
        
        leftCapeBinding = new ModelRenderer(this);
        leftCapeBinding.setPos(3.0F, 12.0F, 0.0F);
        body.addChild(leftCapeBinding);
        
        
        leftCapeBinding_r1 = new ModelRenderer(this);
        leftCapeBinding_r1.setPos(-3.0F, -0.6F, 0.0F);
        leftCapeBinding.addChild(leftCapeBinding_r1);
        ClientUtil.setRotationAngle(leftCapeBinding_r1, 0.0F, 0.0F, -0.0873F);
        leftCapeBinding_r1.texOffs(17, 57).addBox(-0.0459F, -0.0457F, -2.0F, 4.0F, 3.0F, 4.0F, 0.225F, true);
        
        leftCape = new ModelRenderer(this);
        leftCape.setPos(2.0F, 12.0F, 1.899F);
        body.addChild(leftCape);
        
        
        leftCape_r1 = new ModelRenderer(this);
        leftCape_r1.setPos(2.0F, 0.0F, -0.3F);
        leftCape.addChild(leftCape_r1);
        ClientUtil.setRotationAngle(leftCape_r1, 0.2182F, 0.0F, -0.1745F);
        leftCape_r1.texOffs(0, 38).addBox(0.0F, 0.0F, -4.9F, 0.0F, 4.0F, 5.0F, 0.0F, false);
        leftCape_r1.texOffs(1, 47).addBox(-4.0F, 0.0F, 0.1F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        
        lowLeftCape = new ModelRenderer(this);
        lowLeftCape.setPos(1.0F, 4.0F, -0.3F);
        leftCape.addChild(lowLeftCape);
        
        
        lowLeftCape_r1 = new ModelRenderer(this);
        lowLeftCape_r1.setPos(1.0F, -4.0F, 0.0F);
        lowLeftCape.addChild(lowLeftCape_r1);
        ClientUtil.setRotationAngle(lowLeftCape_r1, 0.2182F, 0.0F, -0.1745F);
        lowLeftCape_r1.texOffs(0, 46).addBox(0.0F, 4.0F, -4.9F, 0.0F, 7.0F, 5.0F, 0.0F, false);
        lowLeftCape_r1.texOffs(23, 43).addBox(-4.0F, 4.0F, 0.1F, 4.0F, 7.0F, 0.0F, 0.0F, false);
        
        rightArm_r1 = new ModelRenderer(this);
        rightArm_r1.setPos(5.0F, 30.0F, 0.0F);
        rightArm.addChild(rightArm_r1);
        ClientUtil.setRotationAngle(rightArm_r1, 0.0F, 0.0F, 0.0436F);
        rightArm_r1.texOffs(40, 32).addBox(-9.0F, -23.7F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
        
        rightArm_r2 = new ModelRenderer(this);
        rightArm_r2.setPos(5.0F, 22.2F, 0.0F);
        rightArm.addChild(rightArm_r2);
        ClientUtil.setRotationAngle(rightArm_r2, 0.0F, 0.0F, -0.0873F);
        rightArm_r2.texOffs(40, 4).addBox(-5.9F, -24.6F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
        
        leftArm_r1 = new ModelRenderer(this);
        leftArm_r1.setPos(-5.0F, 30.0F, 0.0F);
        leftArm.addChild(leftArm_r1);
        ClientUtil.setRotationAngle(leftArm_r1, 0.0F, 0.0F, -0.0436F);
        leftArm_r1.texOffs(40, 38).addBox(5.0F, -23.7F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
        
        leftArm_r2 = new ModelRenderer(this);
        leftArm_r2.setPos(-5.0F, 22.2F, 0.0F);
        leftArm.addChild(leftArm_r2);
        ClientUtil.setRotationAngle(leftArm_r2, 0.0F, 0.0F, 0.0873F);
        leftArm_r2.texOffs(40, 10).addBox(1.9F, -24.6F, -2.0F, 4.0F, 2.0F, 4.0F, 0.25F, false);
        
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
        ClientUtil.setRotationAngleDegrees(leftCapeBinding, 30, 0, 0);
        ClientUtil.setRotationAngleDegrees(leftCape, 50, 0, 0);
        ClientUtil.setRotationAngleDegrees(lowLeftCape, 23, 0, 0);
        ClientUtil.setRotationAngleDegrees(rightCapeBinding, 30, 0, 0);
        ClientUtil.setRotationAngleDegrees(rightCape, 50, 0, 0);
        ClientUtil.setRotationAngleDegrees(lowRightCape, 24, 0, 0);
        sittingAnim.setEmote();
    }

    private void setDefaultPivot(){
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.rightArm.z = 0.0F;
        this.rightArm.x = - 5.0F;
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
    
    @Override
    public void tick() {
        if (sittingAnim != null) {
            sittingAnim.tick();
        }
    }
}
