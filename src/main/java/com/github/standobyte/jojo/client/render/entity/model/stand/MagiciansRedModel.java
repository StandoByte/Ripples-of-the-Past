package com.github.standobyte.jojo.client.render.entity.model.stand;

import java.util.function.Function;

import com.github.standobyte.jojo.action.stand.MagiciansRedFlameBurst;
import com.github.standobyte.jojo.action.stand.MagiciansRedRedBind;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

// Made with Blockbench 3.9.2


public class MagiciansRedModel extends HumanoidStandModel<MagiciansRedEntity> {
    private ModelRenderer headLeft;
    private ModelRenderer headRight;
    private ModelRenderer beakUpper;
    private ModelRenderer beakUpperLeft;
    private ModelRenderer beakUpperRight;
    private ModelRenderer beakUpper2;
    private ModelRenderer beakUpper3;
    private ModelRenderer beakLower;
    private ModelRenderer beakLowerLeft;
    private ModelRenderer beakLowerRight;
    private ModelRenderer feather;
    private ModelRenderer feather2;

    public MagiciansRedModel() {
        super();
        addLayerSpecificBoxes();
    }

    protected MagiciansRedModel(Function<ResourceLocation, RenderType> renderType, int textureWidth, int textureHeight) {
        super(renderType, textureWidth, textureHeight);
        addLayerSpecificBoxes();
    }

    protected void addLayerSpecificBoxes() {
        head.texOffs(0, 0).addBox(-3.5F, -7.0F, -4.0F, 7.0F, 7.0F, 8.0F, 0.0F, false);
        addHumanoidBaseBoxes(part -> part != head);
        
        head.texOffs(0, 15).addBox(-4.1F, -7.4F, -4.4F, 1.0F, 7.0F, 2.0F, -0.4F, false);
        head.texOffs(0, 24).addBox(-4.1F, -1.6F, -4.4F, 1.0F, 2.0F, 2.0F, -0.4F, false);
        head.texOffs(20, 15).addBox(3.1F, -7.4F, -4.4F, 1.0F, 7.0F, 2.0F, -0.4F, true);
        head.texOffs(20, 24).addBox(3.1F, -1.6F, -4.4F, 1.0F, 2.0F, 2.0F, -0.4F, true);
        head.texOffs(80, 26).addBox(-2.5F, -1.0F, -4.5F, 5.0F, 1.0F, 2.0F, 0.25F, false);

        headLeft = new ModelRenderer(this);
        headLeft.setPos(3.7F, -3.5F, -2.8F);
        head.addChild(headLeft);
        setRotationAngle(headLeft, 0.0F, -0.0478F, 0.0F);
        headLeft.texOffs(26, 15).addBox(-0.6F, -3.9F, -0.4F, 1.0F, 3.0F, 5.0F, -0.4F, true);
        headLeft.texOffs(26, 23).addBox(-0.6F, -0.5F, -0.4F, 1.0F, 2.0F, 5.0F, -0.4F, true);
        headLeft.texOffs(26, 30).addBox(-0.6F, 1.9F, -0.4F, 1.0F, 2.0F, 5.0F, -0.4F, true);

        headRight = new ModelRenderer(this);
        headRight.setPos(-3.7F, -3.5F, -2.8F);
        head.addChild(headRight);
        setRotationAngle(headRight, 0.0F, 0.0478F, 0.0F);
        headRight.texOffs(6, 15).addBox(-0.4F, -3.9F, -0.4F, 1.0F, 3.0F, 5.0F, -0.4F, false);
        headRight.texOffs(6, 23).addBox(-0.4F, -0.5F, -0.4F, 1.0F, 2.0F, 5.0F, -0.4F, false);
        headRight.texOffs(6, 30).addBox(-0.4F, 1.9F, -0.4F, 1.0F, 2.0F, 5.0F, -0.4F, false);

        beakUpper = new ModelRenderer(this);
        beakUpper.setPos(0.0F, -1.0F, -4.0F);
        head.addChild(beakUpper);
        setRotationAngle(beakUpper, 0.1745F, 0.0F, 0.0F);
        beakUpper.texOffs(78, 7).addBox(-1.0F, -1.0F, -5.5F, 2.0F, 1.0F, 7.0F, 0.25F, false);
        beakUpper.texOffs(66, 12).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 1.0F, 2.0F, 0.25F, false);

        beakUpperLeft = new ModelRenderer(this);
        beakUpperLeft.setPos(1.25F, -0.5F, -5.75F);
        beakUpper.addChild(beakUpperLeft);
        setRotationAngle(beakUpperLeft, 0.0F, 0.2618F, 0.0F);
        beakUpperLeft.texOffs(89, 8).addBox(-1.25F, -0.5F, 0.25F, 1.0F, 1.0F, 7.0F, 0.25F, true);

        beakUpperRight = new ModelRenderer(this);
        beakUpperRight.setPos(-1.25F, -0.5F, -5.75F);
        beakUpper.addChild(beakUpperRight);
        setRotationAngle(beakUpperRight, 0.0F, -0.2618F, 0.0F);
        beakUpperRight.texOffs(69, 8).addBox(0.25F, -0.5F, 0.25F, 1.0F, 1.0F, 7.0F, 0.25F, false);

        beakUpper2 = new ModelRenderer(this);
        beakUpper2.setPos(0.0F, -1.25F, -5.75F);
        beakUpper.addChild(beakUpper2);
        setRotationAngle(beakUpper2, 0.7854F, 0.0F, 0.0F);
        beakUpper2.texOffs(87, 4).addBox(-0.5F, 0.25F, 0.25F, 1.0F, 1.0F, 1.0F, 0.25F, false);

        beakUpper3 = new ModelRenderer(this);
        beakUpper3.setPos(0.0F, 0.0F, 1.5F);
        beakUpper2.addChild(beakUpper3);
        setRotationAngle(beakUpper3, -0.7854F, 0.0F, 0.0F);
        beakUpper3.texOffs(79, 0).addBox(-0.5F, 0.25F, 0.25F, 1.0F, 1.0F, 6.0F, 0.25F, false);

        beakLower = new ModelRenderer(this);
        beakLower.setPos(0.0F, -0.75F, -4.75F);
        head.addChild(beakLower);
        beakLower.texOffs(79, 17).addBox(-1.0F, 0.0F, -4.75F, 2.0F, 1.0F, 6.0F, 0.0F, false);
        beakLower.texOffs(65, 20).addBox(-1.5F, 0.0F, -1.75F, 3.0F, 1.0F, 3.0F, 0.0F, false);
        beakLower.texOffs(72, 25).addBox(-1.0F, -0.3F, -3.75F, 2.0F, 1.0F, 4.0F, 0.0F, false);

        beakLowerLeft = new ModelRenderer(this);
        beakLowerLeft.setPos(1.0F, 0.5F, -4.75F);
        beakLower.addChild(beakLowerLeft);
        setRotationAngle(beakLowerLeft, 0.0F, 0.2618F, 0.0F);
        beakLowerLeft.texOffs(90, 19).addBox(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F, 0.0F, true);

        beakLowerRight = new ModelRenderer(this);
        beakLowerRight.setPos(-1.0F, 0.5F, -4.75F);
        beakLower.addChild(beakLowerRight);
        setRotationAngle(beakLowerRight, 0.0F, -0.2618F, 0.0F);
        beakLowerRight.texOffs(72, 19).addBox(0.0F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F, 0.0F, false);

        feather = new ModelRenderer(this);
        feather.setPos(0.0F, -6.5F, 4.5F);
        head.addChild(feather);
        setRotationAngle(feather, 0.3927F, 0.0F, 0.0F);
        feather.texOffs(30, 8).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 6.0F, 0.0F, false);
        feather.texOffs(40, 12).addBox(-1.0F, 0.5F, 4.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        feather2 = new ModelRenderer(this);
        feather2.setPos(0.0F, -1.0F, -2.5F);
        feather.addChild(feather2);
        setRotationAngle(feather2, 0.3491F, 0.0F, 0.0F);
        feather2.texOffs(30, 0).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 6.0F, 0.0F, false);
        feather2.texOffs(40, 4).addBox(-1.0F, 0.5F, 5.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);

        torso.texOffs(20, 64).addBox(-3.5F, 1.1F, -2.0F, 7.0F, 3.0F, 1.0F, 0.4F, false);
        torso.texOffs(24, 73).addBox(-2.5F, 4.0F, -2.3F, 5.0F, 6.0F, 1.0F, 0.0F, false);
    }

    @Override
    protected ModelPose<MagiciansRedEntity> initPoseReset() {
        return super.initPoseReset()
                .putRotation(RotationAngle.fromDegrees(beakUpper, 10F, 0.0F, 0.0F))
                .putRotation(new RotationAngle(beakLower, 0.0F, 0.0F, 0.0F));
    }

    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {
                    new RotationAngle(head, 0.0F, 0.1309F, 0.0F),
                    new RotationAngle(beakUpper, -0.3491F, 0.0F, 0.0F),
                    new RotationAngle(beakLower, 0.5236F, 0.0F, 0.0F),
                    new RotationAngle(body, 0.0F, -0.3927F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 0.0F, -2.3562F),
                    new RotationAngle(rightArm, 0.0F, 0.0F, 2.3562F),
                    new RotationAngle(leftLeg, 0.1745F, -0.7854F, 0.0F),
                    new RotationAngle(rightLeg, -1.5708F, -0.7854F, 0.0F),
                    new RotationAngle(rightLowerLeg, 2.3562F, 0.0F, 0.0F)
            },
            new RotationAngle[] {
                    new RotationAngle(head, -0.7854F, 0.0F, 0.0F),
                    new RotationAngle(beakUpper, -0.3491F, 0.0F, 0.0F),
                    new RotationAngle(beakLower, 0.5236F, 0.0F, 0.0F),
                    new RotationAngle(leftArm, 0.0F, 2.3562F, -2.8798F),
                    new RotationAngle(leftForeArm, 0.0F, 0.0F, 2.3562F),
                    new RotationAngle(rightArm, 0.0F, -2.3562F, 2.8798F),
                    new RotationAngle(rightForeArm, 0.0F, 0.0F, -2.3562F),
                    new RotationAngle(leftLeg, 0.2182F, 0.0F, 0.1309F),
                    new RotationAngle(rightLeg, 0.5236F, 0.0F, -0.1309F)
            }
        };
    }

    @Override
    public void setupAnim(MagiciansRedEntity entity, float walkAnimPos, float walkAnimSpeed, float ticks, float yRotationOffset, float xRotation) {
        leftArm.setPos(6.0F, -10.0F, 0.0F);
        rightArm.setPos(-6.0F, -10.0F, 0.0F);
        super.setupAnim(entity, walkAnimPos, walkAnimSpeed, ticks, yRotationOffset, xRotation);
    }
    
    @Override
    protected void initActionPoses() {
        actionAnim.put(MagiciansRedFlameBurst.FLAME_BURST_POSE, new PosedActionAnimation.Builder<MagiciansRedEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<MagiciansRedEntity>(new RotationAngle[] {
                        RotationAngle.fromDegrees(beakUpper, -20F, 0.0F, 0.0F),
                        RotationAngle.fromDegrees(beakLower, 30F, 0.0F, 0.0F),
                        RotationAngle.fromDegrees(leftArm, -90F, 60F, 12.5F),
                        new RotationAngle(leftForeArm, -0.2618F, 0.0F, 0.5236F),
                        new RotationAngle(rightArm, -1.3963F, -0.9163F, -0.1745F),
                        new RotationAngle(rightForeArm, -0.5236F, 0.0F, -0.6981F),
                        new RotationAngle(leftLeg, -0.3927F, 0.0F, 0.0873F),
                        new RotationAngle(leftLowerLeg, 0.7854F, 0.0F, 0.0F),
                        new RotationAngle(rightLeg, 0.2618F, 0.0F, -0.0873F)
                }).setAdditionalAnim((rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
                    leftArm.setPos(4.0F, -10.0F, 0.0F);
                    rightArm.setPos(-4.0F, -10.0F, 0.0F);
                    setRotationAngle(head, xRotRad - 0.3927F, 0.0F, 0.0F);
                }).createRigid())
                .build(idlePose));

        ModelPose<MagiciansRedEntity> kickPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -15F, 0F, 0F), 
                RotationAngle.fromDegrees(body, -49.1066F, -20.7048F, 22.2077F),
                RotationAngle.fromDegrees(leftLeg, 45F, 30F, 0F),
                RotationAngle.fromDegrees(rightLeg, -75F, 30F, 30F),
                RotationAngle.fromDegrees(rightLowerLeg, 60F, 0F, 0F)
        });
        ModelPose<MagiciansRedEntity> kickPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -30F, -15F, 0F), 
                RotationAngle.fromDegrees(body, -54.7356F, -30F, 35.2644F),
                new RotationAngle(beakUpper, -0.3491F, 0.0F, 0.0F),
                new RotationAngle(beakLower, 0.5236F, 0.0F, 0.0F),
                RotationAngle.fromDegrees(leftArm, -60F, 0.0F, -45F),
                RotationAngle.fromDegrees(leftForeArm, 0.0F, 0.0F, 50F),
                RotationAngle.fromDegrees(rightArm, 45F, -10F, 10F),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, 0F),
                RotationAngle.fromDegrees(leftLeg, 45F, 45F, 0F),
                RotationAngle.fromDegrees(rightLeg, -105F, 30F, 30F),
                RotationAngle.fromDegrees(rightLowerLeg, 90F, 0F, 0F)
        });
        ModelPose<MagiciansRedEntity> kickPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -45F, -10F, 0F), 
                RotationAngle.fromDegrees(body, -59.3179F, -27.034F, 37.4537F),
                RotationAngle.fromDegrees(leftArm, -135F, -15F, 30F),
                RotationAngle.fromDegrees(rightArm, -180F, 30F, -45F).noDegreesWrapping(),
                RotationAngle.fromDegrees(rightForeArm, 0F, 0F, -45F),
                RotationAngle.fromDegrees(leftLeg, 50F, 45F, 0F),
                RotationAngle.fromDegrees(rightLeg, -42.6168F, 9.6867F, 44.9391F),
                RotationAngle.fromDegrees(rightLowerLeg, 0F, 0F, 0F)
        });
        ModelPose<MagiciansRedEntity> kickRecoveryArmHackyFix = ((ModelPose<MagiciansRedEntity>) idlePose).copy()
                .putRotation(new RotationAngle(rightArm, 0.1309F, 0.0F, 0.4363F).noDegreesWrapping());
        actionAnim.put(StandPose.HEAVY_ATTACK_FINISHER, new PosedActionAnimation.Builder<MagiciansRedEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<>(kickPose1, kickPose2))
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransition<>(kickPose2, kickPose3))
                .build(kickRecoveryArmHackyFix));
        
        actionAnim.put(MagiciansRedRedBind.RED_BIND_POSE, new PosedActionAnimation.Builder<MagiciansRedEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ModelPose<>(new RotationAngle[] {
                        new RotationAngle(leftArm, -1.4708F, 0.4712F, 0.0F),
                        new RotationAngle(leftForeArm, 0.0F, 0.0F, 0.0F),
                        new RotationAngle(rightArm, -1.6708F, -0.4712F, 0.0F),
                        new RotationAngle(rightForeArm, 0.0F, 0.0F, 0.0F),
                }))
                .build(idlePose));
        
        super.initActionPoses();
    }

    @Override
    protected ModelPose<MagiciansRedEntity> initIdlePose() {
        return new ModelPose<>(new RotationAngle[] {
                new RotationAngle(beakUpper, 0.1745F, 0.0F, 0.0F),
                new RotationAngle(beakLower, 0.0F, 0.0F, 0.0F),
                new RotationAngle(body, 0.0F, 0.1309F, 0.0F),
                new RotationAngle(upperPart, 0.0F, 0.0F, 0.0F),
                new RotationAngle(leftArm, 0.3054F, 0.0F, -0.2182F),
                new RotationAngle(leftForeArm, -2.0944F, -0.2618F, 1.0472F),
                new RotationAngle(rightArm, 0.1309F, 0.0F, 0.4363F),
                new RotationAngle(rightForeArm, -2.3562F, 0.2618F, -1.8326F),
                new RotationAngle(leftLeg, 0.1309F, -0.1309F, 0.0F),
                new RotationAngle(leftLowerLeg, 0.2618F, 0.0F, 0.0F),
                new RotationAngle(rightLeg, 0.0F, -0.1309F, 0.2182F),
                new RotationAngle(rightLowerLeg, 0.1309F, 0.0F, 0.0F),
        });
    }

    @Override
    protected ModelPose<MagiciansRedEntity> initIdlePose2Loop() {
        return new ModelPose<>(new RotationAngle[] {
                new RotationAngle(leftArm, 0.3927F, 0.0F, -0.1745F),
                new RotationAngle(leftForeArm, -1.9635F, -0.1309F, 1.0472F),
                new RotationAngle(rightArm, 0.1309F, 0.0F, 0.3054F),
                new RotationAngle(rightForeArm, -2.3562F, 0.2618F, -1.7017F)
        });
    }
    
    @Override
    public void setupFirstPersonRotations(MatrixStack matrixStack, MagiciansRedEntity entity, float xRot, float yRot, float yBodyRot) {
        if (standPose == MagiciansRedFlameBurst.FLAME_BURST_POSE) {
            float xRotRad = xRot * MathUtil.DEG_TO_RAD;
            matrixStack.mulPose(Vector3f.XP.rotation(xRotRad));
            
            matrixStack.translate(0, -entity.getBbHeight() * 0.25, 0.25);
            matrixStack.mulPose(Vector3f.XP.rotation(head.xRot - xRotRad));
            matrixStack.translate(0, entity.getBbHeight() * 0.25, -0.25);
            
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180 + yBodyRot));
            matrixStack.translate(0, -entity.getEyeHeight(), 0);
        }
        else {
            super.setupFirstPersonRotations(matrixStack, entity, xRot, yRot, yBodyRot);
        }
    }
}
