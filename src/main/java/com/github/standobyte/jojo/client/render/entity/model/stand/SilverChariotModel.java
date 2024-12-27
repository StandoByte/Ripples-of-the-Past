package com.github.standobyte.jojo.client.render.entity.model.stand;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.pose.ConditionalModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseSided;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.render.entity.pose.RigidModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.IActionAnimation;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.client.render.entity.pose.anim.barrage.StandOneHandedBarrageAnimation;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Hand;

// Made with Blockbench 3.9.2


public class SilverChariotModel extends HumanoidStandModel<SilverChariotEntity> {
    protected ModelRenderer backCord;
    protected ModelRenderer rapier;
    protected ModelRenderer rapierBlade;
    
//    protected SilverChariotModel(Function<ResourceLocation, RenderType> renderType, int textureWidth, int textureHeight) {
//        super(renderType, textureWidth, textureHeight);
//    }
    
    public SilverChariotModel() {
        super();

        root = new ModelRenderer(this);
        root.setPos(0.0F, 24.0F, 0.0F);
        

        head = new ModelRenderer(this);
        head.setPos(0.0F, -24.0F, 0.0F);
        root.addChild(head);
        head.texOffs(26, 22).addBox(-4.0F, -5.1F, -4.0F, 8.0F, 3.0F, 3.0F, -0.2F, false);
        head.texOffs(0, 21).addBox(-4.0F, -4.25F, -1.05F, 8.0F, 2.0F, 5.0F, -0.15F, false);
        head.texOffs(28, 28).addBox(-4.0F, -4.85F, -4.0F, 8.0F, 3.0F, 1.0F, 0.15F, false);
        head.texOffs(0, 28).addBox(-3.5F, -3.1F, -2.75F, 7.0F, 3.0F, 6.0F, -0.1F, false);
        head.texOffs(2, 37).addBox(-1.0F, -2.15F, -4.0F, 2.0F, 2.0F, 2.0F, -0.1F, false);
        head.texOffs(26, 32).addBox(-4.5F, -4.25F, -2.9F, 1.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(0, 37).addBox(-3.5F, -1.5F, -3.0F, 1.0F, 1.0F, 1.0F, 0.15F, false);
        head.texOffs(8, 37).addBox(2.5F, -1.5F, -3.0F, 1.0F, 1.0F, 1.0F, 0.15F, false);
        head.texOffs(32, 32).addBox(3.5F, -4.25F, -2.9F, 1.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(16, 10).addBox(-0.5F, -7.85F, -3.5F, 1.0F, 4.0F, 7.0F, 0.1F, false);
        head.texOffs(7, 0).addBox(-2.5F, -6.9F, -4.2F, 5.0F, 1.0F, 1.0F, -0.433F, false);
        head.texOffs(7, 6).addBox(-2.5F, -6.4F, -4.2F, 5.0F, 1.0F, 1.0F, -0.433F, false);
        head.texOffs(15, 2).addBox(1.5F, -6.9F, -4.07F, 1.0F, 1.0F, 3.0F, -0.433F, false);
        head.texOffs(16, 8).addBox(1.5F, -6.4F, -4.07F, 1.0F, 1.0F, 3.0F, -0.433F, false);
        head.texOffs(7, 2).addBox(-2.5F, -6.9F, -4.07F, 1.0F, 1.0F, 3.0F, -0.433F, false);
        head.texOffs(7, 8).addBox(-2.5F, -6.4F, -4.07F, 1.0F, 1.0F, 3.0F, -0.433F, false);
        head.texOffs(12, 37).addBox(-3.5F, -1.5F, -4.25F, 7.0F, 1.0F, 1.0F, -0.433F, false);
        head.texOffs(18, 39).addBox(2.5F, -1.5F, -4.117F, 1.0F, 1.0F, 2.0F, -0.433F, false);
        head.texOffs(12, 39).addBox(-3.5F, -1.5F, -4.117F, 1.0F, 1.0F, 2.0F, -0.433F, false);
        head.texOffs(13, 12).addBox(-2.0F, -8.3F, -0.5F, 4.0F, 1.0F, 1.0F, -0.433F, false);
        head.texOffs(17, 14).addBox(1.0F, -8.167F, -0.5F, 1.0F, 2.0F, 1.0F, -0.433F, false);
        head.texOffs(13, 14).addBox(-2.0F, -8.167F, -0.5F, 1.0F, 2.0F, 1.0F, -0.433F, false);

        backCord = new ModelRenderer(this);
        backCord.setPos(0.0F, -4.0F, 2.55F);
        head.addChild(backCord);
        setRotationAngle(backCord, 0.0873F, 0.0F, 0.0F);
        backCord.texOffs(25, 11).addBox(-2.5F, -2.55F, -0.5F, 5.0F, 1.0F, 1.0F, -0.433F, false);
        backCord.texOffs(29, 13).addBox(1.5F, -2.417F, -0.5F, 1.0F, 3.0F, 1.0F, -0.433F, false);
        backCord.texOffs(25, 13).addBox(-2.5F, -2.417F, -0.5F, 1.0F, 3.0F, 1.0F, -0.433F, false);

        ModelRenderer cube_r1;
        ModelRenderer cube_r2;
        
        cube_r1 = new ModelRenderer(this);
        cube_r1.setPos(4.25F, -4.45F, -0.25F);
        head.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 0.0F, -0.1309F);
        cube_r1.texOffs(32, 13).addBox(-4.0F, -3.25F, -2.0F, 4.0F, 4.0F, 4.0F, -0.35F, true);

        cube_r2 = new ModelRenderer(this);
        cube_r2.setPos(-4.25F, -4.45F, -0.25F);
        head.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 0.0F, 0.1309F);
        cube_r2.texOffs(0, 13).addBox(0.0F, -3.25F, -2.0F, 4.0F, 4.0F, 4.0F, -0.35F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, -24.0F, 0.0F);
        root.addChild(body);
        

        upperPart = new ModelRenderer(this);
        upperPart.setPos(0.0F, 12.0F, 0.0F);
        body.addChild(upperPart);
        

        torso = new ModelRenderer(this);
        torso.setPos(0.0F, -12.0F, 0.0F);
        upperPart.addChild(torso);
        torso.texOffs(0, 64).addBox(-4.0F, 1.0F, -1.5F, 8.0F, 5.0F, 3.0F, 0.0F, false);
        torso.texOffs(4, 72).addBox(-1.5F, 0.5F, -2.5F, 3.0F, 3.0F, 5.0F, -0.6F, false);
        torso.texOffs(0, 72).addBox(-0.5F, -0.1F, -0.5F, 1.0F, 12.0F, 1.0F, 0.1F, false);
        torso.texOffs(0, 58).addBox(-2.5F, -0.1F, -2.0F, 1.0F, 2.0F, 4.0F, -0.2F, false);
        torso.texOffs(11, 58).addBox(1.5F, -0.1F, -2.0F, 1.0F, 2.0F, 4.0F, -0.2F, false);
        torso.texOffs(0, 85).addBox(-3.0F, 10.0F, -2.0F, 6.0F, 2.0F, 4.0F, 0.1F, false);

        leftArm = convertLimb(new ModelRenderer(this));
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        upperPart.addChild(leftArm);
        leftArm.texOffs(32, 114).addBox(-2.0F, 2.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, true);
        leftArm.texOffs(32, 106).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 0.0F, true);

        leftArmJoint = new ModelRenderer(this);
        leftArmJoint.setPos(-0.5F, 4.0F, 0.0F);
        leftArm.addChild(leftArmJoint);
        leftArmJoint.texOffs(32, 101).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, true);

        leftForeArm = new ModelRenderer(this);
        leftForeArm.setPos(0.0F, 4.0F, 0.0F);
        leftArm.addChild(leftForeArm);
        leftForeArm.texOffs(32, 119).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, true);

        rightArm = convertLimb(new ModelRenderer(this));
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        upperPart.addChild(rightArm);
        rightArm.texOffs(0, 114).addBox(-1.0F, 2.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, false);
        rightArm.texOffs(0, 106).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 4.0F, 4.0F, 0.0F, false);

        rightArmJoint = new ModelRenderer(this);
        rightArmJoint.setPos(0.5F, 4.0F, 0.0F);
        rightArm.addChild(rightArmJoint);
        rightArmJoint.texOffs(0, 101).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, false);

        rightForeArm = new ModelRenderer(this);
        rightForeArm.setPos(0.0F, 4.0F, 0.0F);
        rightArm.addChild(rightForeArm);
        rightForeArm.texOffs(0, 119).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rapier = new ModelRenderer(this);
        rapier.setPos(0.25F, 4.5F, 0.0F);
        rightForeArm.addChild(rapier);
        rapier.texOffs(31, 79).addBox(-1.5F, -1.5F, -3.0F, 3.0F, 3.0F, 5.0F, 0.25F, false);
        rapier.texOffs(49, 80).addBox(-0.5F, -0.5F, -3.25F, 1.0F, 1.0F, 6.0F, 0.0F, false);

        rapierBlade = new ModelRenderer(this);
        rapierBlade.setPos(0.0F, 0.0F, 0.0F);
        rapier.addChild(rapierBlade);
        rapierBlade.texOffs(32, 72).addBox(-0.5F, -0.5F, -17.0F, 1.0F, 1.0F, 15.0F, -0.3F, false);

        leftLeg = convertLimb(new ModelRenderer(this));
        leftLeg.setPos(1.4F, 12.0F, 0.0F);
        body.addChild(leftLeg);
        leftLeg.texOffs(96, 110).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        leftLegJoint = new ModelRenderer(this);
        leftLegJoint.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLegJoint);
        leftLegJoint.texOffs(96, 105).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, true);

        leftLowerLeg = new ModelRenderer(this);
        leftLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        leftLeg.addChild(leftLowerLeg);
        leftLowerLeg.texOffs(96, 119).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rightLeg = convertLimb(new ModelRenderer(this));
        rightLeg.setPos(-1.4F, 12.0F, 0.0F);
        body.addChild(rightLeg);
        rightLeg.texOffs(64, 110).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        rightLegJoint = new ModelRenderer(this);
        rightLegJoint.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLegJoint);
        rightLegJoint.texOffs(64, 105).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, -0.1F, false);

        rightLowerLeg = new ModelRenderer(this);
        rightLowerLeg.setPos(0.0F, 6.0F, 0.0F);
        rightLeg.addChild(rightLowerLeg);
        rightLowerLeg.texOffs(64, 119).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, 0.0F, false);
    }

    @Override
    public void afterInit() {
        super.afterInit();
        putNamedModelPart("rapier", rapier);
    }

    @Override
    public void prepareMobModel(SilverChariotEntity entity, float walkAnimPos, float walkAnimSpeed, float partialTick) {
        super.prepareMobModel(entity, walkAnimPos, walkAnimSpeed, partialTick);
        if (rapierBlade != null) {
            rapierBlade.visible = entity == null || entity.hasRapier();
        }
    }

    // TODO remove allat, we're gonna parse the gecko animations now
    @Override
    protected ModelPose<SilverChariotEntity> initPoseReset() {
        return super.initPoseReset()
                .putRotation(RotationAngle.fromDegrees(rapier, 45F, 0.0F, 0.0F));
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

    protected final Map<StandPose, IActionAnimation<SilverChariotEntity>> rapierAnim = new HashMap<>();
    @Override
    protected void initActionPoses() {
        ModelAnim<SilverChariotEntity> armsRotation = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            setSecondXRot(leftArm, xRotRad);
            setSecondXRot(rightArm, xRotRad);
        };
        
        RotationAngle[] barrageRightStart = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, -45, 0),
                RotationAngle.fromDegrees(leftArm, 45, 0, -60),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rightArm, 89, 0, 90),
                RotationAngle.fromDegrees(rightForeArm, -135, 0, 0),
                RotationAngle.fromDegrees(rapier, 90, 0, 0)
        };
        
        RotationAngle[] barrageRightImpact = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, -45, 0),
                RotationAngle.fromDegrees(leftArm, 45, 0, -60),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rightArm, -90, 45, 0),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, 0),
                RotationAngle.fromDegrees(rapier, 90, 0, 0)
        };
        
        IModelPose<SilverChariotEntity> barrageStabStart = new ModelPoseSided<>(
                new ModelPose<SilverChariotEntity>(mirrorAngles(barrageRightStart)).setAdditionalAnim(armsRotation),
                new ModelPose<SilverChariotEntity>(barrageRightStart).setAdditionalAnim(armsRotation));
        
        IModelPose<SilverChariotEntity> barrageStabImpact = new ModelPoseSided<>(
                new ModelPose<SilverChariotEntity>(mirrorAngles(barrageRightImpact)).setAdditionalAnim(armsRotation),
                new ModelPose<SilverChariotEntity>(barrageRightImpact).setAdditionalAnim(armsRotation));
        
        rapierAnim.put(StandPose.LIGHT_ATTACK, new PosedActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<SilverChariotEntity>(barrageStabStart, barrageStabImpact)
                        .setEasing(sw -> sw < 0.75F ? 
                                16 / 9  * sw * sw    - 8 / 3 * sw    + 1
                                : 16    * sw * sw    - 24    * sw    + 9))
                .addPose(StandEntityAction.Phase.PERFORM, new RigidModelPose<>(barrageStabImpact))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransition<>(barrageStabImpact, idlePose)
                        .setEasing(pr -> Math.max(4F * (pr - 1) + 1, 0F)))
                .build(idlePose));
        

        rapierAnim.putIfAbsent(StandPose.HEAVY_ATTACK, new PosedActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                        new RotationAngle(body, 0.0F, -0.7854F, 0.0F),
                        new RotationAngle(upperPart, 0.0F, -0.7854F, 0.0F),
                        new RotationAngle(leftArm, 0.2618F, 0.0F, -0.1309F),
                        new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.0F),
                        new RotationAngle(rightArm, 0.0F, 1.5708F, 1.5708F),
                        new RotationAngle(rightForeArm, 0.0F, 0.0F, 0.0F),
                        new RotationAngle(rapier, 1.5708F, 0.0F, 0.0F)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
                    rightArm.zRot -= xRotRad;
                })).build(idlePose));


        ModelPose<SilverChariotEntity> sweepPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0F, -45F, 0F), 
                RotationAngle.fromDegrees(body, 0F, -90F, 0F),
                RotationAngle.fromDegrees(upperPart, 0F, -30F, 0F),
                RotationAngle.fromDegrees(rightArm, 0F, 45F, 75F),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, -120F),
                RotationAngle.fromDegrees(rapier, 82.5F, 0F, 0F)
        });
        ModelPose<SilverChariotEntity> sweepPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0F, -36F, 0F), 
                RotationAngle.fromDegrees(body, 0F, -69F, 0F),
                RotationAngle.fromDegrees(upperPart, 0F, -24F, 0F),
                RotationAngle.fromDegrees(rightArm, 2.5F, 36F, 84F),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, 0F),
                RotationAngle.fromDegrees(rapier, 90F, 0F, 0F)
        });
        ModelPose<SilverChariotEntity> sweepPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0F, 0F, 0F), 
                RotationAngle.fromDegrees(body, 0F, 15F, 0F),
                RotationAngle.fromDegrees(upperPart, 0F, 0F, 0F),
                RotationAngle.fromDegrees(leftArm, 45F, -30F, -75F),
                RotationAngle.fromDegrees(leftForeArm, 0F, 0F, 30F),
                RotationAngle.fromDegrees(rightArm, 12.5F, 0F, 120F)
        });
        rapierAnim.putIfAbsent(StandPose.HEAVY_ATTACK_FINISHER, new PosedActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<>(idlePose, sweepPose1))
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransitionMultiple.Builder<>(sweepPose1)
                        .addPose(0.2F, sweepPose2)
                        .build(sweepPose3))
                .build(idlePose));
        
        rapierAnim.putIfAbsent(StandPose.RANGED_ATTACK, new PosedActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                        new RotationAngle(body, 0.0F, -0.7854F, 0.0F),
                        new RotationAngle(upperPart, 0.0F, -0.7854F, 0.0F),
                        new RotationAngle(leftArm, 0.2618F, 0.0F, -0.1309F),
                        new RotationAngle(rightArm, 0.0F, 1.5708F, 1.5708F),
                        new RotationAngle(rapier, 1.5708F, 0.0F, 0.0F)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
                    rightArm.zRot -= xRotRad;
                })).build(idlePose));
        
        rapierAnim.putIfAbsent(StandPose.BLOCK, new PosedActionAnimation.Builder<SilverChariotEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<>(new RotationAngle[] {
                        new RotationAngle(leftArm, -0.8727F, 0.0F, -0.1745F),
                        new RotationAngle(leftForeArm, -1.5708F, 0.2618F, 0.0F),
                        new RotationAngle(rightArm, 0.5236F, 0.0F, 0.1746F),
                        new RotationAngle(rightForeArm, -1.9199F, 0.0F, 0.0F),
                        new RotationAngle(rapier, 0.829F, 0.0F, -1.1781F)
                })).build(idlePose));
        
        IModelPose<SilverChariotEntity> stabLoop = new ModelPoseTransition<SilverChariotEntity>(barrageStabStart, barrageStabImpact).setEasing(sw -> {
            float halfSwing = sw < 0.4F ? sw * 20 / 8 : sw > 0.6F ? (1 - sw) * 20 / 8 : 1F;
            return halfSwing * halfSwing * halfSwing;
        });
        
        rapierAnim.putIfAbsent(StandPose.BARRAGE, new StandOneHandedBarrageAnimation<SilverChariotEntity>(this, 
                stabLoop, 
                idlePose, 
                Hand.MAIN_HAND));
        
        super.initActionPoses();
    }

    @Override
    protected IModelPose<SilverChariotEntity> initBaseIdlePose() {
        return new ConditionalModelPose<SilverChariotEntity>()
                .addPose(chariot -> chariot != null && !chariot.hasRapier(), 
                        new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                                new RotationAngle(body, 0.0F, -0.2618F, 0.0F),
                                new RotationAngle(torso, 0.0F, 0.0F, 0.0F),
                                new RotationAngle(leftArm, 0.9163F, -0.2618F, -0.2182F),
                                new RotationAngle(leftForeArm, -1.309F, 0.0F, 0.0F),
                                new RotationAngle(rightArm, 0.5236F, 0.0F, 0.1309F),
                                new RotationAngle(rightForeArm, -0.7854F, 0.0F, 0.0F),
                                new RotationAngle(rapier, 0.0F, 0.0F, 0.0F),
                                new RotationAngle(leftLeg, 0.1309F, -0.1309F, 0.0F),
                                new RotationAngle(leftLowerLeg, 0.1309F, 0.0873F, 0.0F),
                                new RotationAngle(rightLeg, -0.1745F, 0.1309F, 0.0F),
                                new RotationAngle(rightLowerLeg, 0.2618F, 0.0F, 0.0F),
                        }).setAdditionalAnim(HEAD_ROTATION))
                .addPose(chariot -> chariot == null || chariot.hasRapier() && chariot.hasArmor(), 
                        new ModelPose<SilverChariotEntity>(new RotationAngle[] {
                                new RotationAngle(body, 0.0F, -0.2618F, 0.0F),
                                new RotationAngle(torso, 0.0F, 0.0F, 0.0F),
                                new RotationAngle(leftArm, 0.9163F, -0.2618F, 0.1309F),
                                new RotationAngle(leftForeArm, -1.309F, 0.0F, 0.0F),
                                new RotationAngle(rightArm, -0.5236F, 0.0F, -0.2618F),
                                new RotationAngle(rightForeArm, 0.0F, 0.0F, 0.0F),
                                new RotationAngle(rapier, -0.3927F, 0.1309F, 0.0F),
                                new RotationAngle(leftLeg, 0.1309F, -0.1309F, 0.0F),
                                new RotationAngle(leftLowerLeg, 0.1309F, 0.0873F, 0.0F),
                                new RotationAngle(rightLeg, -0.1745F, 0.1309F, 0.0F),
                                new RotationAngle(rightLowerLeg, 0.2618F, 0.0F, 0.0F),
                        }).setAdditionalAnim(HEAD_ROTATION))
                .addPose(chariot -> chariot != null && chariot.hasRapier() && !chariot.hasArmor(), 
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
                .addPose(chariot -> chariot != null && !chariot.hasRapier(), 
                        new ModelPose<>(new RotationAngle[] {
                                new RotationAngle(leftArm, 0.9599F, -0.2618F, -0.2618F),
                                new RotationAngle(leftForeArm, -1.3526F, 0.0F, 0.0F),
                                new RotationAngle(rightArm, 0.6109F, 0.0F, 0.1309F),
                                new RotationAngle(rightForeArm, -0.8727F, 0.0F, 0.0F)
                        }))
                .addPose(chariot -> chariot == null || chariot.hasRapier() && chariot.hasArmor(), 
                        new ModelPose<>(new RotationAngle[] {
                                new RotationAngle(leftArm, 0.9599F, -0.2182F, 0.1309F),
                                new RotationAngle(leftForeArm, -1.3526F, 0.0F, 0.0F),
                                new RotationAngle(rightArm, -0.5672F, 0.0F, -0.3491F)
                        }))
                .addPose(chariot -> chariot != null && chariot.hasRapier() && !chariot.hasArmor(), 
                        new ModelPose<>(new RotationAngle[] {
                                new RotationAngle(leftArm, -0.6545F, 0.3927F, -1.0472F),
                                new RotationAngle(leftForeArm, -1.4399F, 0.2618F, 0.2618F),
                                new RotationAngle(rightArm, 0.5672F, 0.0F, 1.3963F),
                                new RotationAngle(rightForeArm, -0.5236F, 1.0472F, 1.0906F)
                                })
                        );
    }
    
    @Override
    protected IActionAnimation<SilverChariotEntity> getActionAnim(@Nullable SilverChariotEntity entity, StandPose poseType) {
        if (entity == null || entity.hasRapier() && rapierAnim.containsKey(poseType)) {
            return rapierAnim.get(poseType);
        }
        return super.getActionAnim(entity, poseType);
    }
}
