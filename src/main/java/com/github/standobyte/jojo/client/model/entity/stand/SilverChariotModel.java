package com.github.standobyte.jojo.client.model.entity.stand;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.client.model.pose.ConditionalModelPose;
import com.github.standobyte.jojo.client.model.pose.IModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPose;
import com.github.standobyte.jojo.client.model.pose.ModelPoseSided;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.model.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.model.pose.RigidModelPose;
import com.github.standobyte.jojo.client.model.pose.RotationAngle;
import com.github.standobyte.jojo.client.model.pose.StandActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;

// Made with Blockbench 3.9.2


public class SilverChariotModel extends HumanoidStandModel<SilverChariotEntity> {
    private ModelRenderer backCord;
    private ModelRenderer bone1;
    private ModelRenderer bone2;
    private ModelRenderer rapier;
    private ModelRenderer rapierBlade;

    public SilverChariotModel() {
        this(64, 64);
    }

    public SilverChariotModel(int textureWidth, int textureHeight) {
        super(textureWidth, textureHeight);
        addLayerSpecificBoxes();
    }

    protected void addLayerSpecificBoxes() {
        head.texOffs(22, 0).addBox(-4.0F, -5.1F, -4.0F, 8.0F, 3.0F, 3.0F, -0.1F, false);
        head.texOffs(0, 8).addBox(-4.0F, -4.25F, -1.05F, 8.0F, 2.0F, 5.0F, -0.05F, false);
        head.texOffs(18, 18).addBox(-4.0F, -4.85F, -4.0F, 8.0F, 3.0F, 1.0F, 0.25F, false);
        head.texOffs(38, 10).addBox(-3.5F, -3.1F, -2.75F, 7.0F, 3.0F, 6.0F, 0.0F, false);
        head.texOffs(50, 6).addBox(-1.0F, -2.15F, -4.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(58, 4).addBox(-4.5F, -4.25F, -3.0F, 1.0F, 2.0F, 2.0F, 0.1F, false);
        head.texOffs(58, 14).addBox(-3.5F, -1.5F, -3.0F, 1.0F, 1.0F, 1.0F, 0.25F, false);
        head.texOffs(58, 12).addBox(2.5F, -1.5F, -3.0F, 1.0F, 1.0F, 1.0F, 0.25F, false);
        head.texOffs(58, 8).addBox(3.5F, -4.25F, -3.0F, 1.0F, 2.0F, 2.0F, 0.1F, false);
        head.texOffs(9, 16).addBox(-0.5F, -7.85F, -3.5F, 1.0F, 4.0F, 7.0F, 0.2F, false);
        head.texOffs(22, 6).addBox(-2.5F, -6.9F, -4.4F, 5.0F, 1.0F, 1.0F, -0.333F, false);
        head.texOffs(22, 8).addBox(-2.5F, -6.4F, -4.4F, 5.0F, 1.0F, 1.0F, -0.333F, false);
        head.texOffs(26, 10).addBox(1.5F, -6.9F, -4.07F, 1.0F, 1.0F, 3.0F, -0.333F, false);
        head.texOffs(26, 10).addBox(1.5F, -6.4F, -4.07F, 1.0F, 1.0F, 3.0F, -0.333F, false);
        head.texOffs(26, 10).addBox(-2.5F, -6.9F, -4.07F, 1.0F, 1.0F, 3.0F, -0.333F, false);
        head.texOffs(26, 10).addBox(-2.5F, -6.4F, -4.07F, 1.0F, 1.0F, 3.0F, -0.333F, false);
        head.texOffs(48, 0).addBox(-3.5F, -1.5F, -4.25F, 7.0F, 1.0F, 1.0F, -0.333F, false);
        head.texOffs(54, 2).addBox(2.5F, -1.5F, -3.917F, 1.0F, 1.0F, 2.0F, -0.333F, false);
        head.texOffs(48, 2).addBox(-3.5F, -1.5F, -3.917F, 1.0F, 1.0F, 2.0F, -0.333F, false);
        head.texOffs(34, 6).addBox(-2.0F, -8.3F, -0.5F, 4.0F, 1.0F, 1.0F, -0.333F, false);
        head.texOffs(38, 8).addBox(1.0F, -7.967F, -0.5F, 1.0F, 2.0F, 1.0F, -0.333F, false);
        head.texOffs(34, 8).addBox(-2.0F, -7.967F, -0.5F, 1.0F, 2.0F, 1.0F, -0.333F, false);

        backCord = new ModelRenderer(this);
        backCord.setPos(0.0F, -4.0F, 2.55F);
        head.addChild(backCord);
        setRotationAngle(backCord, 0.0873F, 0.0F, 0.0F);
        backCord.texOffs(26, 16).addBox(-2.5F, -2.75F, -0.5F, 5.0F, 1.0F, 1.0F, -0.333F, false);
        backCord.texOffs(36, 12).addBox(1.5F, -2.417F, -0.5F, 1.0F, 3.0F, 1.0F, -0.333F, false);
        backCord.texOffs(40, 12).addBox(-2.5F, -2.417F, -0.5F, 1.0F, 3.0F, 1.0F, -0.333F, false);

        bone1 = new ModelRenderer(this);
        bone1.setPos(4.25F, -4.45F, -0.25F);
        head.addChild(bone1);
        setRotationAngle(bone1, 0.0F, 0.0F, -0.1309F);
        bone1.texOffs(0, 15).addBox(-4.0F, -3.25F, -2.0F, 4.0F, 4.0F, 4.0F, -0.25F, true);

        bone2 = new ModelRenderer(this);
        bone2.setPos(-4.25F, -4.45F, -0.25F);
        head.addChild(bone2);
        setRotationAngle(bone2, 0.0F, 0.0F, 0.1309F);
        bone2.texOffs(0, 15).addBox(0.0F, -3.25F, -2.0F, 4.0F, 4.0F, 4.0F, -0.25F, false);

        torso.texOffs(0, 0).addBox(-4.0F, 1.0F, -1.5F, 8.0F, 5.0F, 3.0F, 0.0F, false);
        torso.texOffs(33, 29).addBox(-1.5F, 0.5F, -2.5F, 3.0F, 3.0F, 5.0F, -0.6F, false);
        torso.texOffs(29, 30).addBox(-0.5F, -0.1F, -0.5F, 1.0F, 12.0F, 1.0F, 0.1F, false);
        torso.texOffs(0, 27).addBox(-2.5F, -0.1F, -2.0F, 1.0F, 2.0F, 4.0F, -0.2F, false);
        torso.texOffs(0, 33).addBox(1.5F, -0.1F, -2.0F, 1.0F, 2.0F, 4.0F, -0.2F, false);
        torso.texOffs(33, 37).addBox(-3.0F, 10.0F, -2.0F, 6.0F, 2.0F, 4.0F, 0.1F, false);

        leftArm.texOffs(52, 59).addBox(-2.0F, 2.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, true);
        leftArm.texOffs(14, 47).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 0.0F, true);

        leftArmJoint.setPos(-0.5F, 4.0F, 0.0F);
        leftArmJoint.texOffs(2, 39).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, true);

        leftForeArm.texOffs(12, 55).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, true);

        rightArm.texOffs(52, 54).addBox(-1.0F, 2.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, false);
        rightArm.texOffs(0, 47).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 0.0F, false);

        rightArmJoint.setPos(0.5F, 4.0F, 0.0F);
        rightArmJoint.texOffs(2, 39).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, false);

        rightForeArm.texOffs(0, 55).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rapier = new ModelRenderer(this);
        rapier.setPos(0.25F, 4.5F, 0.0F);
        rightForeArm.addChild(rapier);
        setRotationAngle(rapier, 0.7854F, 0.0F, 0.0F);
        rapier.texOffs(11, 35).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 5.0F, 0.25F, false);
        rapier.texOffs(13, 28).addBox(-0.5F, -0.5F, -3.25F, 1.0F, 1.0F, 6.0F, 0.0F, false);

        rapierBlade = new ModelRenderer(this);
        rapierBlade.setPos(0.0F, 0.0F, 0.0F);
        rapier.addChild(rapierBlade);
        rapierBlade.texOffs(12, 28).addBox(-0.5F, -0.5F, -17.0F, 1.0F, 1.0F, 15.0F, -0.3F, false);

        leftLeg.setPos(1.4F, 12.0F, 0.0F);
        leftLeg.texOffs(40, 46).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        leftLegJoint.texOffs(54, 39).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, true);

        leftLowerLeg.texOffs(36, 55).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rightLeg.setPos(-1.4F, 12.0F, 0.0F);
        rightLeg.texOffs(28, 46).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rightLegJoint.texOffs(54, 39).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, false);

        rightLowerLeg.texOffs(24, 55).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);
    }

    @Override
    public void prepareMobModel(SilverChariotEntity entity, float walkAnimPos, float walkAnimSpeed, float partialTick) {
        super.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
        if (rapierBlade != null) {
            rapierBlade.visible = entity.hasRapier();
        }
    }

    @Override
    protected ModelPose<SilverChariotEntity> initPoseReset() {
        return super.initPoseReset()
                .putRotation(new RotationAngle(rapier, 0.7854F, 0.0F, 0.0F));
    }

    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {
                    new RotationAngle(head, -0.1745F, 0.0F, -0.0873F),
                    new RotationAngle(upperPart, 0.0F, -0.2618F, 0.0F),
                    new RotationAngle(leftArm, 0.7854F, -0.2618F, 0.0F),
                    new RotationAngle(rightArm, -1.8326F, -0.4363F, 0.0F),
                    new RotationAngle(leftLeg, 0.2618F, 0.0F, 0.0F),
                    new RotationAngle(rightLeg, 0.2618F, 0.0F, 0.2618F),
                    new RotationAngle(rapier, -0.3491F, 2.3562F, 0.0F)
            },
            new RotationAngle[] {
                    new RotationAngle(body, 0.0F, -0.3927F, 0.0F),
                    new RotationAngle(leftArm, 0.5236F, 0.0F, -0.9599F),
                    new RotationAngle(leftForeArm, 0.0F, 0.0F, 1.9199F),
                    new RotationAngle(rightArm, -1.5708F, 0.3927F, 0.0F),
                    new RotationAngle(rapier, 1.5708F, 0.0F, 0.0F)
            },
            new RotationAngle[] {
                    new RotationAngle(head, 0.0873F, 0.0436F, -0.1745F),
                    new RotationAngle(body, 0.0F, -0.3927F, -0.1309F),
                    new RotationAngle(upperPart, 0.0F, 0.3927F, 0.0F),
                    new RotationAngle(leftArm, 0.3927F, 0.0F, 0.0F),
                    new RotationAngle(rightArm, 0.2618F, -0.2618F, 1.3963F),
                    new RotationAngle(rightForeArm, -2.3562F, 0.0F, 0.0F),
                    new RotationAngle(leftLeg, 0.0F, 0.3927F, 0.0873F),
                    new RotationAngle(rightLeg, 0.0F, 0.3927F, 0.2182F),
                    new RotationAngle(rightLowerLeg, 0.3491F, 0.0F, -0.0873F),
                    new RotationAngle(rapier, 2.0071F, 0.0F, 0.0F)
            }
        };
    }

    protected final Map<StandPose, StandActionAnimation<SilverChariotEntity>> rapierAnim = new HashMap<>();
    @Override
    protected void initPoses() {
        super.initPoses();
        
        // FIXME (!!!!) SC attack animation
//        rapierAnim.put(StandPose.LIGHT_ATTACK, initLightAttackAnim());
        
        IModelPose<SilverChariotEntity> armSwingPose = new ModelPoseSided<>(
                initRapierBarrageSwing(HandSide.LEFT), 
                initRapierBarrageSwing(HandSide.RIGHT));
        rapierAnim.put(StandPose.LIGHT_ATTACK, new StandActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, armSwingPose)
                .addPose(StandEntityAction.Phase.PERFORM, new RigidModelPose<SilverChariotEntity>(armSwingPose))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransition<SilverChariotEntity>(armSwingPose, idlePose)
                        .setEasing(pr -> Math.max(4F * (pr - 1) + 1, 0F)))
                .build(idlePose));
        

        rapierAnim.put(StandPose.HEAVY_ATTACK, new StandActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                        new RotationAngle(body, 0.0F, -0.7854F, 0.0F),
                        new RotationAngle(upperPart, 0.0F, -0.7854F, 0.0F),
                        new RotationAngle(leftArm, 0.2618F, 0.0F, -0.1309F),
                        new RotationAngle(rightArm, 0.0F, 1.5708F, 1.5708F),
                        new RotationAngle(rightForeArm, 0.0F, 0.0F, 0.0F),
                        new RotationAngle(rapier, 1.5708F, 0.0F, 0.0F)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                    rightArm.zRot -= xRotation * MathUtil.DEG_TO_RAD;
                })).build(idlePose));


        ModelPose<SilverChariotEntity> sweepPose1 = new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0F, -45F, 0F), 
                RotationAngle.fromDegrees(body, 0F, -90F, 0F),
                RotationAngle.fromDegrees(upperPart, 0F, -30F, 0F),
                RotationAngle.fromDegrees(rightArm, 0F, 45F, 75F),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, -120F),
                RotationAngle.fromDegrees(rapier, 82.5F, 0F, 0F)
        });
        ModelPose<SilverChariotEntity> sweepPose2 = new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0F, -36F, 0F), 
                RotationAngle.fromDegrees(body, 0F, -69F, 0F),
                RotationAngle.fromDegrees(upperPart, 0F, -24F, 0F),
                RotationAngle.fromDegrees(rightArm, 2.5F, 36F, 84F),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, 0F),
                RotationAngle.fromDegrees(rapier, 90F, 0F, 0F)
        });
        ModelPose<SilverChariotEntity> sweepPose3 = new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0F, 0F, 0F), 
                RotationAngle.fromDegrees(body, 0F, 15F, 0F),
                RotationAngle.fromDegrees(upperPart, 0F, 0F, 0F),
                RotationAngle.fromDegrees(leftArm, 45F, -30F, -75F),
                RotationAngle.fromDegrees(leftForeArm, 0F, 0F, 30F),
                RotationAngle.fromDegrees(rightArm, 12.5F, 0F, 120F)
        });
        rapierAnim.put(StandPose.HEAVY_ATTACK_COMBO, new StandActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<SilverChariotEntity>(idlePose, sweepPose1))
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransitionMultiple.Builder<SilverChariotEntity>(sweepPose1)
                        .addPose(0.2F, sweepPose2)
                        .build(sweepPose3))
                .build(idlePose));
        
        rapierAnim.put(StandPose.RANGED_ATTACK, new StandActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                        new RotationAngle(body, 0.0F, -0.7854F, 0.0F),
                        new RotationAngle(upperPart, 0.0F, -0.7854F, 0.0F),
                        new RotationAngle(leftArm, 0.2618F, 0.0F, -0.1309F),
                        new RotationAngle(rightArm, 0.0F, 1.5708F, 1.5708F),
                        new RotationAngle(rapier, 1.5708F, 0.0F, 0.0F)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                    rightArm.zRot -= xRotation * MathUtil.DEG_TO_RAD;
                })).build(idlePose));
        
        rapierAnim.put(StandPose.BLOCK, new StandActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                        new RotationAngle(leftArm, -0.8727F, 0.0F, -0.1745F),
                        new RotationAngle(leftForeArm, -1.5708F, 0.2618F, 0.0F),
                        new RotationAngle(rightArm, 0.5236F, 0.0F, 0.1746F),
                        new RotationAngle(rightForeArm, -1.9199F, 0.0F, 0.0F),
                        new RotationAngle(rapier, 0.829F, 0.0F, -1.1781F)
                })).build(idlePose));

        // FIXME (!!!!) fix SC barrage animation
        rapierBarrageSwing = new ModelPoseSided<>(
                initRapierBarrageSwing(HandSide.LEFT), 
                initRapierBarrageSwing(HandSide.RIGHT));
    }

    @Override
    protected IModelPose<SilverChariotEntity> initBaseIdlePose() {
        return new ConditionalModelPose<SilverChariotEntity>()
                .addPose(chariot -> !chariot.hasRapier(), 
                        super.initBaseIdlePose())
                .addPose(chariot -> chariot.hasRapier() && chariot.hasArmor(), 
                        super.initBaseIdlePose())
                .addPose(chariot -> chariot.hasRapier() && !chariot.hasArmor(), 
                        new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                                new RotationAngle(body, 0.2618F, 0.7854F, 0.0436F),
                                new RotationAngle(upperPart, 0.0F, -0.5236F, 0.0F),
                                new RotationAngle(leftArm, -0.6109F, 0.3927F, -1.0472F),
                                new RotationAngle(leftForeArm, -1.3963F, 0.2618F, 0.2618F),
                                new RotationAngle(rightArm, 0.5236F, 0.0F, 1.3963F),
                                new RotationAngle(rightForeArm, -0.5236F, 1.0472F, 1.0472F),
                                new RotationAngle(rapier, 0.0F, 0.0F, -0.1309F),
                                new RotationAngle(leftLeg, 0.0F, -0.1309F, 0.0F),
                                new RotationAngle(leftLowerLeg, 0.0873F, 0.0F, 0.0F),
                                new RotationAngle(rightLeg, -0.5236F, 0.3927F, 0.3927F),
                                new RotationAngle(rightLowerLeg, 1.5708F, 0.0F, 0.0F)
                        }).setAdditionalAnim(HEAD_ROTATION));
    }

    @Override
    protected IModelPose<SilverChariotEntity> initIdlePose2Loop() {
        return new ConditionalModelPose<SilverChariotEntity>()
                .addPose(chariot -> !chariot.hasRapier(), 
                        super.initIdlePose2Loop())
                .addPose(chariot -> chariot.hasRapier() && chariot.hasArmor(), 
                        super.initIdlePose2Loop())
                .addPose(chariot -> chariot.hasRapier() && !chariot.hasArmor(), 
                        new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                                new RotationAngle(leftArm, -0.6545F, 0.3927F, -1.0472F),
                                new RotationAngle(leftForeArm, -1.4399F, 0.2618F, 0.2618F),
                                new RotationAngle(rightArm, 0.5672F, 0.0F, 1.3963F),
                                new RotationAngle(rightForeArm, -0.5236F, 1.0472F, 1.0906F)
                                })
                        );
    }
    
    @Override
    protected StandActionAnimation<SilverChariotEntity> getActionAnim(SilverChariotEntity entity, StandPose poseType) {
        if (entity.hasRapier() && rapierAnim.containsKey(poseType)) {
            return rapierAnim.get(poseType);
        }
        return super.getActionAnim(entity, poseType);
    }

    protected ModelPoseSided<SilverChariotEntity> rapierBarrageSwing;
    private ModelPoseTransition<SilverChariotEntity> initRapierBarrageSwing(HandSide swingingHand) {
        ModelRenderer punchingArm = getArm(swingingHand);
        ModelRenderer punchingForeArm = getForeArm(swingingHand);
        ModelRenderer otherArm = getArm(swingingHand.getOpposite());
        ModelRenderer otherForeArm = getForeArm(swingingHand.getOpposite());

        float yRotUpperPart = -0.2618F;
        if (swingingHand == HandSide.LEFT) {
            yRotUpperPart *= -1.0F;
        }
        
        return new ModelPoseTransition<>(
                new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                        new RotationAngle(body, 0, 0, 0),
                        new RotationAngle(upperPart, 0, yRotUpperPart, 0),
                        new RotationAngle(otherArm, -yRotUpperPart * 2, 0, 1.0472F),
                        new RotationAngle(otherForeArm, 0, 0, 0),
                        new RotationAngle(punchingArm, 0.7854F - yRotUpperPart, 0, 1.5708F),
                        new RotationAngle(punchingForeArm, -2.3562F, 0, 0),
                        new RotationAngle(rapier, 1.5708F, 0, 0)
                }), 
                new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                        new RotationAngle(upperPart, 0, yRotUpperPart * 3, 0),
                        new RotationAngle(otherArm, 0, 0, 1.0472F),
                        new RotationAngle(punchingArm, -1.5708F, -yRotUpperPart * 3, 0),
                        new RotationAngle(punchingForeArm, 0, 0, 0)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotationOffset, xRotation) -> {
                    leftArm.zRot *= -1.0F;
                    leftArm.yRot *= -1.0F;
                    punchingArm.xRot += xRotation * MathUtil.DEG_TO_RAD;
                }))
                .setEasing(sw -> {
                    float halfSwing = sw < 0.4F ? sw * 20 / 8 : sw > 0.6F ? (1 - sw) * 20 / 8 : 1F;
                    return halfSwing * halfSwing * halfSwing;
                });
    }
    
    @Override
    protected IModelPose<SilverChariotEntity> getBarrageSwingAnim(SilverChariotEntity entity) {
        return entity.hasRapier() ? rapierBarrageSwing : barrageSwing;
    }
}
