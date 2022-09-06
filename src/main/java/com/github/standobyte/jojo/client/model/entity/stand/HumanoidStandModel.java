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
import com.github.standobyte.jojo.client.model.pose.XRotationModelRenderer;
import com.github.standobyte.jojo.client.model.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.client.model.pose.anim.barrage.TwoHandedBarrageAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.power.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.util.utils.MathUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;

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
            case NONE:
                leftArm.visible = false;
                rightArm.visible = false;
                
            case ALL:
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

    @Override
    protected void initActionPoses() {
        super.initActionPoses();
        
        RotationAngle[] jabRightAngles1 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, -15, 0),
                RotationAngle.fromDegrees(leftArm, -7.5F, 0, -15),
                RotationAngle.fromDegrees(leftForeArm, -100, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 22.5F),
                RotationAngle.fromDegrees(rightForeArm, -105, 0, -15)
        };
        RotationAngle[] jabRightAngles2 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -20F, 0),
                RotationAngle.fromDegrees(leftArm, 30F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -107.5F, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, 5.941F, 8.4211F, 69.059F),
                RotationAngle.fromDegrees(rightForeArm, -75, 0, 0)
        };
        RotationAngle[] jabRightAngles3 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -12.5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -17.5F, 0),
                RotationAngle.fromDegrees(leftArm, 37.5F, 0, -15F),
                RotationAngle.fromDegrees(leftForeArm, -115, 15, 7.5F),
                RotationAngle.fromDegrees(rightArm, -81.9244F, 11.0311F, 70.2661F),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        RotationAngle[] jabRightAngles4 = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -3.75F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -3.75F, 0),
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
        
        IModelPose<T> jabStart = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles1)),
                new ModelPose<T>(jabRightAngles1));
        
        IModelPose<T> jabArmTurn = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles2)).setAdditionalAnim(armsRotation),
                new ModelPose<T>(jabRightAngles2).setAdditionalAnim(armsRotation));
        
        IModelPose<T> jabImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles3)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(jabRightAngles3).setAdditionalAnim(armsRotationFull)).setEasing(x -> x * x * x);
        
        IModelPose<T> jabArmTurnBack = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(jabRightAngles4)).setAdditionalAnim(armsRotationBack),
                new ModelPose<T>(jabRightAngles4).setAdditionalAnim(armsRotationBack)).setEasing(x -> x * x * x);
        
        IModelPose<T> jabEnd = new ModelPoseSided<>(
                new ModelPose<T>(jabRightAngles1),
                new ModelPose<T>(mirrorAngles(jabRightAngles1)));
        
        actionAnim.putIfAbsent(StandPose.LIGHT_ATTACK, 
                new PosedActionAnimation.Builder<T>()
                
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

        
        
        RotationAngle[] heavyRightStart = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 15, 0),
                RotationAngle.fromDegrees(upperPart, 0, 15, 0),
                RotationAngle.fromDegrees(leftArm, -90, 0, -90),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 60),
                RotationAngle.fromDegrees(rightForeArm, -135, 0, 0)
        };
        RotationAngle[] heavyRightBackswing = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 26.25F, 0),
                RotationAngle.fromDegrees(upperPart, 0, 26.25F, 0),
                RotationAngle.fromDegrees(leftArm, -67.5F, 0, -90),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rightArm, 30, 0, 60),
                RotationAngle.fromDegrees(rightForeArm, -120, 0, 0)
        };
        RotationAngle[] heavyRightImpact = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -26.25F, 0),
                RotationAngle.fromDegrees(upperPart, 0, -26.25F, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -60),
                RotationAngle.fromDegrees(leftForeArm, -135, 3.75F, 13.13F),
                RotationAngle.fromDegrees(rightArm, -67.5F, 0, 90),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        
        IModelPose<T> heavyStart = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightStart)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightStart).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> heavyBackswing = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightBackswing)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightBackswing).setAdditionalAnim(armsRotationFull)).setEasing(sw -> sw * sw);
        
        IModelPose<T> heavyImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(heavyRightImpact)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(heavyRightImpact).setAdditionalAnim(armsRotationFull)).setEasing(sw -> sw * sw * sw);
        
        PosedActionAnimation<T> heavyAttackAnim = new PosedActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<T>(heavyStart)
                        .addPose(0.95F, heavyBackswing)
                        .build(heavyImpact))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransition<T>(heavyImpact, idlePose)
                        .setEasing(pr -> Math.max(2F * (pr - 1) + 1, 0F)))
                .build(idlePose);
        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK, heavyAttackAnim);
        
        actionAnim.putIfAbsent(StandPose.HEAVY_ATTACK_COMBO, heavyAttackAnim);
        
        
        
        actionAnim.putIfAbsent(StandPose.BLOCK, new PosedActionAnimation.Builder<T>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<T>(new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                        RotationAngle.fromDegrees(rightForeArm, -90, 30, -90),
                        RotationAngle.fromDegrees(leftForeArm, -90, -30, 90)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                    float blockXRot = MathHelper.clamp(xRotation, -60, 60) * MathUtil.DEG_TO_RAD / 2;
                    rightArm.xRot = -1.5708F + blockXRot;
                    leftArm.xRot = rightArm.xRot;

                    rightArm.yRot = -blockXRot / 2;
                    leftArm.yRot = -rightArm.yRot;

                    rightArm.zRot = -Math.abs(blockXRot) / 2 + 0.7854F;
                    leftArm.zRot = -rightArm.zRot;
                }))
                .build(idlePose));
        
        

        RotationAngle[] barrageRightImpact = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, -30, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -60),
                RotationAngle.fromDegrees(leftForeArm, -135, 0, 0),
                RotationAngle.fromDegrees(rightArm, -90, 0, 90),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0)
        };
        
        IModelPose<T> barrageHitStart = new ModelPoseSided<>(
                new ModelPose<T>(barrageRightImpact).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(mirrorAngles(barrageRightImpact)).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> barrageHitImpact = new ModelPoseSided<>(
                new ModelPose<T>(mirrorAngles(barrageRightImpact)).setAdditionalAnim(armsRotationFull),
                new ModelPose<T>(barrageRightImpact).setAdditionalAnim(armsRotationFull));
        
        IModelPose<T> barrageRecovery = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 0, -22.5F),
                RotationAngle.fromDegrees(leftForeArm, -75, 7.5F, 22.5F),
                RotationAngle.fromDegrees(rightArm, 22.5F, 0, 22.5F),
                RotationAngle.fromDegrees(rightForeArm, -75, -7.5F, -22.5F)
        });
        
        actionAnim.putIfAbsent(StandPose.BARRAGE, new TwoHandedBarrageAnimation<T>(this, 
                new ModelPoseTransition<T>(barrageHitStart, barrageHitImpact).setEasing(HumanoidStandModel::barrageHitEasing), 
                new ModelPoseTransitionMultiple.Builder<T>(new ModelPose<T>(
                        RotationAngle.fromDegrees(body, 0, 0, 0),
                        RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                        RotationAngle.fromDegrees(leftArm, -33.75F, 0, -75),
                        RotationAngle.fromDegrees(leftForeArm, -67.5F, 0, 0),
                        RotationAngle.fromDegrees(rightArm, -33.75F, 0, 75),
                        RotationAngle.fromDegrees(rightForeArm, -67.5F, 0, 0)).setAdditionalAnim(armsRotationFull))
                .addPose(0.25F, barrageRecovery)
                .addPose(0.5F, barrageRecovery)
                .build(idlePose)));
    }
    
    public static float barrageHitEasing(float loopProgress) {
        if (loopProgress < 0.5F) {
            return loopProgress * loopProgress * loopProgress * 8;
        }
        if (loopProgress < 1.0F) {
            float halfSw = 2 * loopProgress - 1;
            return 1 - halfSw * halfSw * halfSw;
        }
        return 0F;
    }
    
    protected RotationAngle[] mirrorAngles(RotationAngle[] angles) {
        RotationAngle[] mirrored = new RotationAngle[angles.length];
        for (int i = 0; i < angles.length; i++) {
            RotationAngle angle = angles[i];
            mirrored[i] = new RotationAngle(getOppositeHandside(angle.modelRenderer), angle.angleX, -angle.angleY, -angle.angleZ);
        }
        return mirrored;
    }
    
    @Override
    public XRotationModelRenderer getArm(HandSide side) {
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
    protected void initOpposites() {
        super.initOpposites();
        oppositeHandside.put(leftArm, rightArm);
        oppositeHandside.put(leftForeArm, rightForeArm);
        oppositeHandside.put(leftLeg, rightLeg);
        oppositeHandside.put(leftLowerLeg, rightLowerLeg);
    }
    
    @Override
    public void translateToHand(HandSide handSide, MatrixStack matrixStack) {
        matrixStack.translate(handSide == HandSide.LEFT ? -0.0625 : 0.0625, 0, 0);
        body.translateAndRotate(matrixStack);
        upperPart.translateAndRotate(matrixStack);
        
        ModelRenderer arm = getArm(handSide);
        arm.translateAndRotate(matrixStack);

        ModelRenderer foreArm = getForeArm(handSide);
        foreArm.translateAndRotate(matrixStack);
        matrixStack.translate(
                (double)(-foreArm.x / 16.0F), 
                (double)(-foreArm.y / 16.0F), 
                (double)(-foreArm.z / 16.0F));
    }
}