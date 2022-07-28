package com.github.standobyte.jojo.client.model.entity.stand;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.model.pose.ModelPoseSided;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.model.pose.RotationAngle;
import com.github.standobyte.jojo.client.model.pose.StandActionAnimation;
import com.github.standobyte.jojo.client.model.pose.XRotationModelRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.utils.MathUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 3.9.2


public abstract class HumanoidStandModel<T extends StandEntity> extends StandEntityModel<T> {
    protected ModelRenderer head;
    protected ModelRenderer body;
    protected ModelRenderer upperPart;
    protected ModelRenderer torso;
    protected XRotationModelRenderer leftArm;
    protected ModelRenderer leftArmJoint;
    protected ModelRenderer leftForeArm;
    protected XRotationModelRenderer rightArm;
    protected ModelRenderer rightArmJoint;
    protected ModelRenderer rightForeArm;
    protected XRotationModelRenderer leftLeg;
    protected ModelRenderer leftLegJoint;
    protected ModelRenderer leftLowerLeg;
    protected XRotationModelRenderer rightLeg;
    protected ModelRenderer rightLegJoint;
    protected ModelRenderer rightLowerLeg;
    

    public HumanoidStandModel() {
        this(128, 128);
    }
    
    public HumanoidStandModel(int textureWidth, int textureHeight) {
    	this(RenderType::entityTranslucent, textureWidth, textureHeight);
    }
    
    public HumanoidStandModel(Function<ResourceLocation, RenderType> renderType, int textureWidth, int textureHeight) {
        super(renderType, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
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

        leftArm = new XRotationModelRenderer(this);
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftArmJoint);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftForeArm);

        rightArm = new XRotationModelRenderer(this);
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightArmJoint);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightForeArm);

        leftLeg = new XRotationModelRenderer(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        body.addChild(leftLeg);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLegJoint);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);

        rightLeg = new XRotationModelRenderer(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        body.addChild(rightLeg);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLegJoint);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);
        
        
        baseHumanoidBoxGenerators = ImmutableMap.<ModelRenderer, Consumer<ModelRenderer>>builder()
                .put(head, part ->          part.texOffs(0, 0)    .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false))
                .put(torso, part ->         part.texOffs(0, 64)   .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false))
                .put(leftArm, part ->       part.texOffs(32, 108) .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(leftArmJoint, part ->  part.texOffs(32, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, true))
                .put(leftForeArm, part ->   part.texOffs(32, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(rightArm, part ->      part.texOffs(0, 108)  .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(rightArmJoint, part -> part.texOffs(0, 102)  .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, false))
                .put(rightForeArm, part ->  part.texOffs(0, 118)  .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(leftLeg, part ->       part.texOffs(96, 108) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(leftLegJoint, part ->  part.texOffs(96, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, true))
                .put(leftLowerLeg, part ->  part.texOffs(96, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .put(rightLeg, part ->      part.texOffs(64, 108) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, 0.0F, false))
                .put(rightLegJoint, part -> part.texOffs(64, 102) .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, -0.125F, false))
                .put(rightLowerLeg, part -> part.texOffs(64, 118) .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, -0.001F, false))
                .build();
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
    }

    protected final void addHumanoidBaseBoxes(@Nullable Predicate<ModelRenderer> partPredicate) {
        for (Map.Entry<ModelRenderer, Consumer<ModelRenderer>> entry : baseHumanoidBoxGenerators.entrySet()) {
            if (partPredicate == null || partPredicate.test(entry.getKey())) {
                entry.getValue().accept(entry.getKey());
            }
        }
    }
    
    private final Map<ModelRenderer, Consumer<ModelRenderer>> baseHumanoidBoxGenerators;

    @Override
    protected void updatePartsVisibility(VisibilityMode mode) {
        if (mode == VisibilityMode.ALL) {
            head.visible = true;
            torso.visible = true;
            leftLeg.visible = true;
            rightLeg.visible = true;
            leftArm.visible = true;
            rightArm.visible = true;
        }
        else {
            head.visible = false;
            torso.visible = false;
            leftLeg.visible = false;
            rightLeg.visible = false;
            switch (mode) {
            case ARMS_ONLY:
                leftArm.visible = true;
                rightArm.visible = true;
                break;
            case LEFT_ARM_ONLY:
                leftArm.visible = true;
                rightArm.visible = false;
                break;
            case RIGHT_ARM_ONLY:
                leftArm.visible = false;
                rightArm.visible = true;
                break;
            default:
                break;
            }
        }
    }

    @Override
    protected void partMissing(StandPart standPart) {
        switch (standPart) {
        case MAIN_BODY:
            head.visible = false;
            torso.visible = false;
            break;
        case ARMS:
            leftArm.visible = false;
            rightArm.visible = false;
            break;
        case LEGS:
            leftLeg.visible = false;
            rightLeg.visible = false;
            break;
        }
    }

    protected ModelPoseSided<T> barrageSwing;
    @Override
    protected void initActionPoses() {
        super.initActionPoses();
        
        
        if (barrageSwing == null) barrageSwing = new ModelPoseSided<>(
                initArmSwingPose(HandSide.LEFT, 1.0F, SwingPart.SWING), 
                initArmSwingPose(HandSide.RIGHT, 1.0F, SwingPart.SWING));
        
        
        
        RotationAngle[] jabRightAngles1 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -15, 0),
                RotationAngle.fromDegrees(leftArm, -7.5F, 0, -15),
                RotationAngle.fromDegrees(leftForeArm, -100, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 22.5F),
                RotationAngle.fromDegrees(rightForeArm, -105, 0, -15)
        };
        RotationAngle[] jabRightAngles2 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -22.5F, 0),
                RotationAngle.fromDegrees(leftArm, 30F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -107.5F, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        RotationAngle[] jabRightAngles3 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -30, 0),
                RotationAngle.fromDegrees(leftArm, 37.5F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -115, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, -81.9244F, 11.0311F, 70.2661F),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        RotationAngle[] jabRightAngles4 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 5.63F, 0, -20.62F),
                RotationAngle.fromDegrees(leftForeArm, -103.75F, 3.75F, 13.13F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        
        ModelAnim<T> armsRotation = (rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
            leftArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD * rotationAmount;
            rightArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD * rotationAmount;
        };
        
        ModelAnim<T> armsRotationFull = (rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
            leftArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD;
            rightArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD;
        };
        
        ModelAnim<T> armsRotationBack = (rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
            leftArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD * (1 - rotationAmount);
            rightArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD * (1 - rotationAmount);
        };
        
        IModelPose<T> jabStart = new ModelPoseSided<T>(
                new ModelPose<T>(mirrorAngles(jabRightAngles1)),
                new ModelPose<T>(jabRightAngles1));
        
        IModelPose<T> jabArmTurn = new ModelPoseSided<T>(
                new ModelPose<T>(mirrorAngles(jabRightAngles2)).setAdditionalAnim(armsRotation),
                new ModelPose<T>(jabRightAngles2).setAdditionalAnim(armsRotation));
        
        IModelPose<T> jabImpact = new ModelPoseSided<T>(
                new ModelPose<T>(mirrorAngles(jabRightAngles3)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(jabRightAngles3).setAdditionalAnim(armsRotationFull)).setEasing(x -> x * x * x);
        
        IModelPose<T> jabArmTurnBack = new ModelPoseSided<T>(
                new ModelPose<T>(mirrorAngles(jabRightAngles4)).setAdditionalAnim(armsRotationBack),
                new ModelPose<T>(jabRightAngles4).setAdditionalAnim(armsRotationBack)).setEasing(x -> x * x * x);
        
        IModelPose<T> jabEnd = new ModelPoseSided<T>(
                new ModelPose<T>(jabRightAngles1),
                new ModelPose<T>(mirrorAngles(jabRightAngles1)));
        
        actionAnim.putIfAbsent(StandPose.LIGHT_ATTACK, 
                new StandActionAnimation.Builder<T>()
                
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<T>(jabStart)
                        .addPose(0.5F, jabArmTurn)
                        .addPose(0.75F, jabImpact)
                        .build(jabImpact))
                
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransitionMultiple.Builder<T>(jabImpact)
                        .addPose(0.25F, jabImpact)
                        .addPose(0.5F, jabArmTurnBack)
                        .build(jabEnd))
                
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<T>(jabEnd)
                        .addPose(0.75F, jabEnd)
                        .build(idlePose))
                
                .build(idlePose));

        
        
        float backSwing = 1.75F;
        StandActionAnimation<T> heavyAttackAnim = new StandActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseSided<>(
                        initArmSwingPose(HandSide.LEFT, backSwing, SwingPart.BACKSWING), 
                        initArmSwingPose(HandSide.RIGHT, backSwing, SwingPart.BACKSWING)))
                .addPose(StandEntityAction.Phase.PERFORM, initBarrageSwingAnim(backSwing))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransition<T>(initBarrageSwingAnim(backSwing), idlePose)
                        .setEasing(pr -> Math.max(2F * (pr - 1) + 1, 0F)))
                .build(idlePose);
        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK, heavyAttackAnim);

        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK_COMBO, heavyAttackAnim);
        
        
        
        actionAnim.putIfAbsent(StandPose.BLOCK, new StandActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<T>(new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                        new RotationAngle(rightForeArm, 0.0F, 0.0F, -1.0472F),
                        new RotationAngle(leftForeArm, 0.0F, 0.0F, 1.0472F)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                    float blockXRot = MathHelper.clamp(xRotation, -60, 60) * MathUtil.DEG_TO_RAD / 2;
                    rightArm.xRot = -1.5708F + blockXRot;
                    leftArm.xRot = rightArm.xRot;

                    rightArm.yRot = blockXRot / 2;
                    leftArm.yRot = -rightArm.yRot;

                    rightArm.zRot = Math.abs(blockXRot) / 2 - 0.7854F;
                    leftArm.zRot = -rightArm.zRot;
                }))
                .build(idlePose));
    }
    
    protected RotationAngle[] mirrorAngles(RotationAngle[] angles) {
        RotationAngle[] mirrored = new RotationAngle[angles.length];
        for (int i = 0; i < angles.length; i++) {
            RotationAngle angle = angles[i];
            mirrored[i] = new RotationAngle(getOppositeHandside(angle.modelRenderer), angle.angleX, -angle.angleY, -angle.angleZ);
        }
        return mirrored;
    }
    
    protected ModelPoseSided<T> initBarrageSwingAnim(float backSwing) {
        return new ModelPoseSided<>(
                initArmSwingPose(HandSide.LEFT, backSwing, SwingPart.SWING), 
                initArmSwingPose(HandSide.RIGHT, backSwing, SwingPart.SWING));
    }
    
    protected ModelPoseTransition<T> initArmSwingPose(HandSide swingingHand, float backSwingAmount, SwingPart animPart) {
        ModelRenderer punchingArm = getArm(swingingHand);
        ModelRenderer punchingForeArm = getForeArm(swingingHand);
        ModelRenderer otherArm = getArm(swingingHand.getOpposite());
        ModelRenderer otherForeArm = getForeArm(swingingHand.getOpposite());

        float yRotBody = 0.5236F;
        float yRotArm = yRotBody;
        if (swingingHand == HandSide.LEFT) {
            yRotBody *= -1.0F;
        }
        
        ModelPoseTransition<T> anim = null;
        switch (animPart) {
        case BACKSWING:
            anim = new ModelPoseTransition<T>(
                    new ModelPose<T>(new RotationAngle[] {
                            new RotationAngle(body, 0, 0, 0),
                            new RotationAngle(upperPart, 0, yRotBody, 0),
                            new RotationAngle(punchingArm, 0.3927F, 0, 1.0472F),
                            new RotationAngle(punchingForeArm, -2.3562F, 0, 0),
                            new RotationAngle(otherArm, -1.5708F, 0, 1.5708F),
                            new RotationAngle(otherForeArm, 0, 0, 0)
                    }), 
                    new ModelPose<T>(new RotationAngle[] {
                            new RotationAngle(upperPart, 0, yRotBody * backSwingAmount, 0),
                            new RotationAngle(otherArm, -1.5708F + yRotArm * (backSwingAmount - 1), 0, 1.5708F),
                    }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                        leftArm.zRot *= -1.0F;
                        leftArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD;
                        rightArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD;
                    }));
            break;
        case SWING:
            anim = new ModelPoseTransition<T>(
                    new ModelPose<T>(new RotationAngle[] {
                            new RotationAngle(body, 0, 0, 0),
                            new RotationAngle(upperPart, 0, yRotBody * backSwingAmount, 0),
                            new RotationAngle(punchingArm, 0.3927F, 0, 1.0472F),
                            new RotationAngle(punchingForeArm, -2.3562F, 0, 0),
                            new RotationAngle(otherArm, -1.5708F + yRotArm * (backSwingAmount - 1), 0, 1.5708F),
                            new RotationAngle(otherForeArm, 0, 0, 0)
                    }), 
                    new ModelPose<T>(new RotationAngle[] {
                            new RotationAngle(upperPart, 0, -yRotBody * backSwingAmount, 0),
                            new RotationAngle(punchingArm, -1.5708F + yRotArm * (backSwingAmount - 1), 0, 1.5708F),
                            new RotationAngle(punchingForeArm, 0, 0, 0),
                            new RotationAngle(otherArm, 0.3927F, 0, 1.0472F),
                            new RotationAngle(otherForeArm, -2.3562F, 0, 0)
                    }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                        leftArm.zRot *= -1.0F;
                        leftArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD;
                        rightArm.xRotSecond = xRotation * MathUtil.DEG_TO_RAD;
                    }))
                    .setEasing(sw -> sw * sw * sw);
            break;
        }
        return anim;
    }
    
    protected enum SwingPart {
        BACKSWING,
        SWING
    }

    @Override
    protected void swingArmBarrage(T entity, float swingAmount, float yRotation, float xRotation, float ticks, HandSide swingingHand, float recovery) {
        entity.setYBodyRot(entity.yRot);
        getBarrageSwingAnim(entity).poseModel(swingAmount, entity, ticks, yRotation, xRotation, swingingHand);
    }
    
    protected IModelPose<T> getBarrageSwingAnim(T entity) {
        return barrageSwing;
    }

    protected XRotationModelRenderer getArm(HandSide side) {
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
    public void setupAnim(T entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        this.leftArm.xRotSecond = 0;
        this.rightArm.xRotSecond = 0;
        this.leftLeg.xRotSecond = 0;
        this.rightLeg.xRotSecond = 0;
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
        rotateJoint(leftArmJoint, leftForeArm);
        rotateJoint(rightArmJoint, rightForeArm);
        rotateJoint(leftLegJoint, leftLowerLeg);
        rotateJoint(rightLegJoint, rightLowerLeg);
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
        return getArm(side);
    }
    
    @Override
    protected void initOpposites() {
        super.initOpposites();
        oppositeHandside.put(leftArm, rightArm);
        oppositeHandside.put(leftForeArm, rightForeArm);
        oppositeHandside.put(leftLeg, rightLeg);
        oppositeHandside.put(leftLowerLeg, rightLowerLeg);
    }
}