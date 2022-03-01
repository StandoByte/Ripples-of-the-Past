package com.github.standobyte.jojo.client.model.entity.stand;

import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose;
import com.github.standobyte.jojo.client.model.pose.RotationAngle;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.util.MathUtil;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 3.9.2


public abstract class HumanoidStandModel<T extends StandEntity> extends StandEntityModel<T> {
    protected ModelRenderer head;
    protected ModelRenderer body;
    protected ModelRenderer upperPart;
    protected ModelRenderer torso;
    protected ModelRenderer leftArm;
    protected ModelRenderer leftArmForearmOnly;
    protected ModelRenderer leftArmJoint;
    protected ModelRenderer leftForeArm;
    protected ModelRenderer rightArm;
    protected ModelRenderer rightArmForearmOnly;
    protected ModelRenderer rightArmJoint;
    protected ModelRenderer rightForeArm;
    protected ModelRenderer leftLeg;
    protected ModelRenderer leftLegJoint;
    protected ModelRenderer leftLowerLeg;
    protected ModelRenderer rightLeg;
    protected ModelRenderer rightLegJoint;
    protected ModelRenderer rightLowerLeg;

    public HumanoidStandModel() {
        this(64, 64);
    }

    public HumanoidStandModel(int textureWidth, int textureHeight) {
        super(true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
        this.texWidth = textureWidth;
        this.texHeight = textureHeight;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);


        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        body.addChild(upperPart);


        torso = new ModelRenderer(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftArmJoint);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftForeArm);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightArmJoint);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightForeArm);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        body.addChild(leftLeg);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLegJoint);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        body.addChild(rightLeg);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLegJoint);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);

        addBaseBoxes();
    }

    public void afterInit() {
        super.afterInit();

        leftArmForearmOnly = new ModelRenderer(this);
        leftArmForearmOnly.setPos(leftArm.x, leftArm.y, leftArm.z);
        upperPart.addChild(leftArmForearmOnly);
        leftArmForearmOnly.addChild(leftForeArm);

        rightArmForearmOnly = new ModelRenderer(this);
        rightArmForearmOnly.setPos(rightArm.x, rightArm.y, rightArm.z);
        upperPart.addChild(rightArmForearmOnly);
        rightArmForearmOnly.addChild(rightForeArm);
    }

    protected void addBaseBoxes() {
        head.texOffs(24, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        torso.texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        leftArm.texOffs(16, 44).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        leftArmJoint.texOffs(0, 38).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftForeArm.texOffs(16, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);

        rightArm.texOffs(0, 44).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        rightArmJoint.texOffs(0, 38).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightForeArm.texOffs(0, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);

        leftLeg.texOffs(48, 44).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        leftLegJoint.texOffs(52, 38).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, true);

        leftLowerLeg.texOffs(48, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);

        rightLeg.texOffs(32, 44).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false);

        rightLegJoint.texOffs(52, 38).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.1F, false);

        rightLowerLeg.texOffs(32, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false);
    }

    @Override
    protected void updatePartsVisibility(VisibilityMode mode, boolean forearmOnly) {
        if (mode == VisibilityMode.ALL) {
            head.visible = true;
            torso.visible = true;
            leftLeg.visible = true;
            rightLeg.visible = true;
            leftArm.visible = true;
            rightArm.visible = true;
            leftArmForearmOnly.visible = false;
            rightArmForearmOnly.visible = false;
        }
        else {
            head.visible = false;
            torso.visible = false;
            leftLeg.visible = false;
            rightLeg.visible = false;
            ModelRenderer leftArmRenderer;
            ModelRenderer rightArmRenderer;
            if (forearmOnly) {
                leftArm.visible = false;
                rightArm.visible = false;
                leftArmRenderer = leftArmForearmOnly;
                rightArmRenderer = rightArmForearmOnly;
            }
            else {
                leftArmForearmOnly.visible = false;
                rightArmForearmOnly.visible = false;
                leftArmRenderer = leftArm;
                rightArmRenderer = rightArm;
            }
            switch (mode) {
            case ARMS_ONLY:
                leftArmRenderer.visible = true;
                rightArmRenderer.visible = true;
                break;
            case LEFT_ARM_ONLY:
                leftArmRenderer.visible = true;
                rightArmRenderer.visible = false;
                break;
            case RIGHT_ARM_ONLY:
                leftArmRenderer.visible = false;
                rightArmRenderer.visible = true;
                break;
            default:
                break;
            }
        }
    }

    @Override
    protected void swingArm(T entity, float swingAmount, float xRotation, HandSide swingingHand, float recovery) {
        entity.setYBodyRot(entity.yRot);
        ModelRenderer punchingArm = getArm(swingingHand);
        ModelRenderer punchingForeArm = getForeArm(swingingHand);
        ModelRenderer otherArm = getArm(swingingHand.getOpposite());
        ModelRenderer otherForeArm = getForeArm(swingingHand.getOpposite());
        if (swingingHand == HandSide.LEFT) {
            punchingArm = this.leftArm;
            punchingForeArm = this.leftForeArm;
            otherArm = this.rightArm;
            otherForeArm = this.rightForeArm;
        }
        else {
            punchingArm = this.rightArm;
            punchingForeArm = this.rightForeArm;
            otherArm = this.leftArm;
            otherForeArm = this.leftForeArm;
        }    

        float swing = swingAmount * swingAmount * swingAmount;

        upperPart.yRot = 0.5236F - 1.0472F * swing;
        if (swingingHand == HandSide.LEFT) {
            this.upperPart.yRot *= -1.0F;
        }

        swing = MathHelper.clamp(swing, 0, 1);

        punchingArm.zRot = 1.0472F + swing * 0.5236F;
        otherArm.zRot = 1.5708F - swing * 0.5236F;
        leftArm.zRot *= -1.0F;

        rightArm.yRot = -xRotation * MathUtil.DEG_TO_RAD;
        leftArm.yRot = xRotation * MathUtil.DEG_TO_RAD;

        punchingArm.xRot = 0.3927F - swing * 1.9635F;
        otherArm.xRot = -1.5708F + swing * 1.9635F;

        punchingForeArm.xRot = 2.3562F * (swing - 1);
        otherForeArm.xRot = -2.3562F * swing;

        recoveryAnim(recovery, upperPart, leftArm, leftForeArm, rightArm, rightForeArm);
    }

    protected void recoveryAnim(float recoveryProgress, ModelRenderer... modelRenderers) {
        if (recoveryProgress > 0) {
            float anim = 1F - recoveryProgress;
            for (ModelRenderer renderer : modelRenderers) {
                if (renderer != null) {
                    renderer.xRot *= anim;
                    renderer.yRot *= anim;
                    renderer.zRot *= anim;
                }
            }
        }
    }

    protected ModelRenderer getArm(HandSide side) {
        switch (side) {
        case LEFT:
            return leftArm;
        case RIGHT:
            return rightArm;
        }
        return null;
    }

    protected ModelRenderer getForeArm(HandSide side) {
        switch (side) {
        case LEFT:
            return leftForeArm;
        case RIGHT:
            return rightForeArm;
        }
        return null;
    }

    @Override
    protected void rotateAdditionalArmSwings() {
        leftArmForearmOnly.copyFrom(leftArm);
        rightArmForearmOnly.copyFrom(rightArm);
    }

    // FIXME //
    //    @Override
    //    protected void blockingPose(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
    //        this.upperPart.yRot = 0.0F;
    //        this.rightForeArm.xRot = 0.0F;
    //        this.leftForeArm.xRot = 0.0F;
    //        
    //        float blockXRot = MathHelper.clamp(xRotation, -60, 60) * MathUtil.DEG_TO_RAD / 2;
    //        rightArm.xRot = -1.5708F + blockXRot;
    //        leftArm.xRot = rightArm.xRot;
    //
    //        rightArm.yRot = blockXRot / 2;
    //        leftArm.yRot = -rightArm.yRot;
    //        
    //        rightArm.zRot = Math.abs(blockXRot) / 2 - 0.7854F;
    //        leftArm.zRot = -rightArm.zRot;
    //        
    //        rightForeArm.zRot = -1.0472F;
    //        leftForeArm.zRot = -rightForeArm.zRot;
    //    }
    //
    //    @Override
    //    protected void lightAttackPose(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
    //        float progress = entity.getCurrentTaskCompletion(ticks - entity.tickCount);
    //        float swing = phase == Phase.WINDUP ? progress : 1F;
    //        swingArm(entity, swing, xRotation,
    //                entity.swingingArm == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite(), 
    //                        phase == Phase.RECOVERY ? Math.max(4F * (progress - 1) + 1, 0F) : 0F);
    //    }
    //
    //    @Override
    //    protected void heavyAttackPose(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
    //        float progress = entity.getCurrentTaskCompletion(ticks - entity.tickCount);
    //        float swing = 0F;
    //        switch (phase) {
    //        case WINDUP:
    //            swing = -progress * 0.75F;
    //            break;
    //        case PERFORM:
    //            swing = 1.75F * progress - 0.75F;
    //            break;
    //        case RECOVERY:
    //            swing = 1F;
    //            break;
    //        default:
    //            break;
    //        }
    //        swingArm(entity, swing, xRotation,
    //                entity.swingingArm == Hand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite(), 
    //                        phase == Phase.RECOVERY ? Math.max(2F * (progress - 1) + 1, 0F) : 0F);
    //    }
    //
    //    @Override
    //    protected void heavyAttackComboPose(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation, Phase phase) {
    //        heavyAttackPose(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation, phase);
    //    }

    @Override
    protected ModelPose<T> initPoseReset() {
        return new ModelPose<T>(
                new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0, 0, 0),
                        new RotationAngle(rightArm, 0, 0, 0),
                        new RotationAngle(rightForeArm, 0, 0, 0),
                        new RotationAngle(leftArm, 0, 0, 0),
                        new RotationAngle(leftForeArm, 0, 0, 0),
                        new RotationAngle(rightLeg, 0, 0, 0),
                        new RotationAngle(rightLowerLeg, 0, 0, 0),
                        new RotationAngle(leftLeg, 0, 0, 0),
                        new RotationAngle(leftLowerLeg, 0, 0, 0)
                });
    }

    @Override
    protected IModelPose<T> blockPose() {
        xRotation = MathHelper.clamp(xRotation, -60, 60) * MathUtil.DEG_TO_RAD / 2;
        return new ModelPose<T>(new RotationAngle[] {
                new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                new RotationAngle(rightForeArm, 0.0F, 0.0F, -1.0472F),
                new RotationAngle(leftForeArm, 0.0F, 0.0F, 1.0472F)
        })
        .setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
            float blockXRot = MathHelper.clamp(xRotation, -60, 60) * MathUtil.DEG_TO_RAD / 2;
            rightArm.xRot = -1.5708F + blockXRot;
            leftArm.xRot = rightArm.xRot;

            rightArm.yRot = blockXRot / 2;
            leftArm.yRot = -rightArm.yRot;

            rightArm.zRot = Math.abs(blockXRot) / 2 - 0.7854F;
            leftArm.zRot = -rightArm.zRot;
        })
        .createRigid();
    }

    @Override
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        rotateJoint(leftArmJoint, leftForeArm);
        rotateJoint(rightArmJoint, rightForeArm);
        rotateJoint(leftLegJoint, leftLowerLeg);
        rotateJoint(rightLegJoint, rightLowerLeg);
        actionAnim.put(StandPose.HEAVY_ATTACK, initHeavyAttackAnim(false));
    }

    protected void rotateJoint(ModelRenderer joint, ModelRenderer limbPart) {
        if (joint != null) {
            joint.xRot = limbPart.xRot / 2;
            joint.yRot = limbPart.yRot / 2;
            joint.zRot = limbPart.zRot / 2;
        }
    }

    @Override
    protected Iterable<ModelRenderer> headParts() {
        return ImmutableList.of(head);
    }

    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return ImmutableList.of(body);
    }

    @Override
    public ModelRenderer armModel(HandSide side) {
        return side == HandSide.LEFT ? leftArm : rightArm;
    }
}