package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.action.stand.HierophantGreenGrapple;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

// Made with Blockbench 3.9.2


public class HierophantGreenModel extends HumanoidStandModel<HierophantGreenEntity> {
    private final ModelRenderer bone7;
    private final ModelRenderer bone8;
    private final ModelRenderer bone9;
    private final ModelRenderer bone10;
    private final ModelRenderer bone11;
    private final ModelRenderer bone12;

    public HierophantGreenModel() {
        super();
        
        addHumanoidBaseBoxes(null);
        
        head.texOffs(36, 0).addBox(-4.5F, -4.5F, -1.0F, 9.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(24, 0).addBox(-1.5F, -8.5F, -1.5F, 3.0F, 1.0F, 3.0F, 0.0F, false);
        head.texOffs(24, 4).addBox(-1.5F, -2.1F, -4.5F, 3.0F, 2.0F, 2.0F, 0.0F, false);
        head.texOffs(56, 0).addBox(-1.0F, -6.0F, -4.3F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        head.texOffs(0, 0).addBox(-3.0F, -4.0F, -4.15F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        head.texOffs(0, 2).addBox(1.0F, -4.0F, -4.15F, 2.0F, 1.0F, 1.0F, 0.0F, true);
        
        torso.texOffs(27, 80).addBox(-1.0F, 3.0F, -2.8F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        torso.texOffs(33, 80).addBox(-1.0F, 3.0F, 1.5F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        torso.texOffs(24, 82).addBox(-2.0F, -0.5F, -2.5F, 4.0F, 4.0F, 5.0F, 0.0F, false);
        torso.texOffs(27, 91).addBox(-2.5F, 4.5F, -2.6F, 5.0F, 5.0F, 1.0F, -0.4F, false);

        bone7 = new ModelRenderer(this);
        bone7.setPos(-1.5F, 3.5F, 0.0F);
        torso.addChild(bone7);
        setRotationAngle(bone7, 0.0F, 0.0F, -0.5236F);
        bone7.texOffs(12, 81).addBox(-0.5F, -4.5F, -2.5F, 1.0F, 5.0F, 5.0F, 0.0F, false);

        bone8 = new ModelRenderer(this);
        bone8.setPos(1.5F, 3.5F, 0.0F);
        torso.addChild(bone8);
        setRotationAngle(bone8, 0.0F, 0.0F, 0.5236F);
        bone8.texOffs(42, 81).addBox(-0.5F, -4.5F, -2.5F, 1.0F, 5.0F, 5.0F, 0.0F, true);

        bone9 = new ModelRenderer(this);
        bone9.setPos(-1.5F, 3.5F, 0.0F);
        torso.addChild(bone9);
        setRotationAngle(bone9, 0.0F, 0.0F, -0.8727F);
        bone9.texOffs(0, 80).addBox(-0.5F, -5.5F, -2.5F, 1.0F, 6.0F, 5.0F, 0.0F, false);

        bone10 = new ModelRenderer(this);
        bone10.setPos(1.5F, 3.5F, 0.0F);
        torso.addChild(bone10);
        setRotationAngle(bone10, 0.0F, 0.0F, 0.8727F);
        bone10.texOffs(54, 80).addBox(-0.5F, -5.5F, -2.5F, 1.0F, 6.0F, 5.0F, 0.0F, false);

        bone11 = new ModelRenderer(this);
        bone11.setPos(-1.5F, 3.5F, -0.25F);
        torso.addChild(bone11);
        setRotationAngle(bone11, 0.0F, 0.0F, -2.5307F);
        bone11.texOffs(14, 91).addBox(-0.5F, -4.5F, -1.5F, 1.0F, 5.0F, 4.0F, 0.0F, false);

        bone12 = new ModelRenderer(this);
        bone12.setPos(1.5F, 3.5F, -0.25F);
        torso.addChild(bone12);
        setRotationAngle(bone12, 0.0F, 0.0F, 2.5307F);
        bone12.texOffs(42, 91).addBox(-0.5F, -4.5F, -1.5F, 1.0F, 5.0F, 4.0F, 0.0F, true);
    }

    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] { 
                    new RotationAngle(head, -0.5236F, 0.0F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 0.0F, -0.5236F),
                    new RotationAngle(leftForeArm, -0.5236F, 0.0F, 1.3963F),
                    new RotationAngle(rightArm, -1.2217F, 0.0F, 0.0F),
                    new RotationAngle(leftLeg, -1.8326F, 0.0F, 0.2618F),
                    new RotationAngle(leftLowerLeg, 1.8326F, 0.0F, 0.0F),
                    new RotationAngle(rightLeg, 0.0F, 0.0F, -0.2618F),
                    new RotationAngle(rightLowerLeg, 1.5708F, 0.0F, 0.0F)
            }
        };
    }
    
    @Override
    protected void initActionPoses() {
        ModelPose<HierophantGreenEntity> esPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(leftArm, -55, 0, 15),
                RotationAngle.fromDegrees(leftForeArm, 0, -90, 45),
                RotationAngle.fromDegrees(rightArm, -90, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, -52.5F, 0, 0)
        });
        ModelPose<HierophantGreenEntity> esPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(leftArm, -90, 0, 0),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 42.5F),
                RotationAngle.fromDegrees(rightArm, -90, 0, 0),
                RotationAngle.fromDegrees(rightForeArm, 0, 0, -42.5F)
        });
        ModelPose<HierophantGreenEntity> esPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 22.5F, 0),
                RotationAngle.fromDegrees(body, 0, 60F, 0),
                RotationAngle.fromDegrees(upperPart, 0, 30F, 0),
                RotationAngle.fromDegrees(leftArm, -90, 0, -56.25F),
                RotationAngle.fromDegrees(leftForeArm, -39.38F, 0, 10.62F),
                RotationAngle.fromDegrees(rightArm, -63.75F, 0, -11.25F),
                RotationAngle.fromDegrees(rightForeArm, 0, 67.5F, -44.37F),
                RotationAngle.fromDegrees(leftLeg, 0.0F, 0.0F, -42.5F),
                RotationAngle.fromDegrees(leftLowerLeg, 0.0F, 0.0F, 120F),
                RotationAngle.fromDegrees(rightLeg, 0.0F, 0.0F, 15F),
                new RotationAngle(rightLowerLeg, 0.0F, 0.0F, 0.0F)
        });
        ModelPose<HierophantGreenEntity> esPose4 = new ModelPose<HierophantGreenEntity>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 0, 0),
                RotationAngle.fromDegrees(body, 0, 0, 0),
                RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                RotationAngle.fromDegrees(leftArm, -90, 0, -75),
                RotationAngle.fromDegrees(rightForeArm, -52.5F, 0, 0),
                RotationAngle.fromDegrees(rightArm, -55, 0, -15),
                RotationAngle.fromDegrees(rightForeArm, 0, 90, -45)
        }).setAdditionalAnim(HEAD_ROTATION);
        
        actionAnim.put(StandPose.RANGED_ATTACK, new PosedActionAnimation.Builder<HierophantGreenEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<>(esPose1)
                        .addPose(0.6F, esPose2)
                        .addPose(0.85F, esPose3)
                        .addPose(0.9F, esPose3)
                        .build(esPose4))
                .addPose(StandEntityAction.Phase.RECOVERY, idlePose)
                .build(idlePose));

        actionAnim.put(HierophantGreenGrapple.GRAPPLE_POSE, new PosedActionAnimation.Builder<HierophantGreenEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<>(new RotationAngle[] {
                        new RotationAngle(rightArm, -1.5708F, 0.0F, 0.0F), 
                        new RotationAngle(rightForeArm, 0.0F, 0.0F, 0.0F), 
                        new RotationAngle(body, 0.0F, 0.0F, 0.0F), 
                }))
                .build(idlePose));
        
        super.initActionPoses();
    }
    
    
    @Override
    protected ModelPose<HierophantGreenEntity> initIdlePose() {
        return new ModelPose<>(new RotationAngle[] {
                new RotationAngle(body, 0.0F, 0.3491F, 0.0F),
                new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                new RotationAngle(leftArm, 0.0F, 0.0F, -0.2182F),
                new RotationAngle(leftForeArm, -0.4363F, 0.0F, 0.0F),
                new RotationAngle(rightArm, -0.0873F, 0.0F, 0.7854F),
                new RotationAngle(rightForeArm, -1.9199F, -0.5672F, 0.9599F),
                new RotationAngle(leftLeg, -0.5672F, -0.6109F, -0.1309F),
                new RotationAngle(leftLowerLeg, 1.309F, -0.1309F, 0.0F),
                new RotationAngle(rightLeg, 0.2182F, 0.1309F, 0.1309F),
                new RotationAngle(rightLowerLeg, 0.2182F, 0.1309F, 0.0F)
        });
    }

    @Override
    protected ModelPose<HierophantGreenEntity> initIdlePose2Loop() {
        return new ModelPose<>(new RotationAngle[] {
                new RotationAngle(leftArm, 0.0873F, 0.0F, -0.2182F),
                new RotationAngle(leftForeArm, -0.5236F, 0.0F, 0.0F),
                new RotationAngle(rightArm, 0.0698F, 0.0F, 0.7854F),
                new RotationAngle(rightForeArm, -2.0071F, -0.4363F, 0.9599F)
        });
    }
}
