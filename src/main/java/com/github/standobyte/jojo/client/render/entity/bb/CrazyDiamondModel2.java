package com.github.standobyte.jojo.client.render.entity.bb;

import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockBullet;
import com.github.standobyte.jojo.action.stand.CrazyDiamondRepairItem;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.pose.ConditionalModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.IModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose.ModelAnim;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransition;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPoseTransitionMultiple;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.client.render.entity.pose.anim.CopyBipedUserPose;
import com.github.standobyte.jojo.client.render.entity.pose.anim.PosedActionAnimation;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.entity.stand.stands.CrazyDiamondEntity;

import net.minecraft.util.HandSide;

public class CrazyDiamondModel2 extends HumanoidStandModel<CrazyDiamondEntity> {

    public CrazyDiamondModel2() {
        super();
        
        // copies all ModelRenderer fields (AKA model parts) from the model file created by Blockbench (CrazyDiamondModelConvertExample)
        BlockbenchStandModelHelper.fillFromBlockbenchExport(new CrazyDiamondModelConvertExample(), this);
    }
    
    
    
    
    
    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head, -32.5F, 52.5F, -5),
                    RotationAngle.fromDegrees(body, -7.5F, 37.5F, 0),
                    RotationAngle.fromDegrees(upperPart, 0, 12.5F, 0),
                    RotationAngle.fromDegrees(leftArm, 25.1426F, 18.0217F, -54.0834F),
                    RotationAngle.fromDegrees(leftForeArm, -37.1572F, -2.0551F, 44.1678F),
                    RotationAngle.fromDegrees(rightArm, -60.3335F, -8.4521F, 68.4237F),
                    RotationAngle.fromDegrees(rightForeArm, -135, 22.5F, -90),
                    RotationAngle.fromDegrees(leftLeg, -10.1402F, 10.7145F, 3.1723F),
                    RotationAngle.fromDegrees(leftLowerLeg, 45, 0, 0),
                    RotationAngle.fromDegrees(rightLeg, 27.9546F, 33.9337F, 7.8335F),
                    RotationAngle.fromDegrees(rightLowerLeg, 29.6217F, 4.9809F, -8.6822F)
            },
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head, 0, 0, 0),
                    RotationAngle.fromDegrees(body, 15, 0, -10),
                    RotationAngle.fromDegrees(upperPart, 0, -12.5F, 0),
                    RotationAngle.fromDegrees(leftArm, -49.8651F, 9.3787F, -53.3701F),
                    RotationAngle.fromDegrees(leftForeArm, -93.6597F, -5.1369F, 84.6506F),
                    RotationAngle.fromDegrees(rightArm, 30.2141F, -5.5863F, 54.7232F),
                    RotationAngle.fromDegrees(rightForeArm, 0, 0, 0),
                    RotationAngle.fromDegrees(leftLeg, -20, 0, -45),
                    RotationAngle.fromDegrees(leftLowerLeg, 30, 0, 37.5F),
                    RotationAngle.fromDegrees(rightLeg, -74.6528F, 12.0675F, 3.284F),
                    RotationAngle.fromDegrees(rightLowerLeg, 100, 0, 0)
            },
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head, -4.3644F, 30.3695F, -8.668F),
                    RotationAngle.fromDegrees(body, -35, -25, 7),
                    RotationAngle.fromDegrees(upperPart, 0, 10, 0),
                    RotationAngle.fromDegrees(leftArm, 12.5F, 0, -20),
                    RotationAngle.fromDegrees(leftForeArm, 7.5F, 0, -10),
                    RotationAngle.fromDegrees(rightArm, 37.5F, 7.5F, -7.5F),
                    RotationAngle.fromDegrees(rightForeArm, -7.5F, 0, 0),
                    RotationAngle.fromDegrees(leftLeg, -43.5572F, 57.6471F, -22.5553F),
                    RotationAngle.fromDegrees(leftLowerLeg, 77.4538F, 4.8812F, -1.0848F),
                    RotationAngle.fromDegrees(rightLeg, -65, 60, 20),
                    RotationAngle.fromDegrees(rightLowerLeg, 107.7531F, 9.5327F, 3.0351F)
            },
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head, 0, 75, 0),
                    RotationAngle.fromDegrees(body, -10, 60, 0),
                    RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                    RotationAngle.fromDegrees(leftArm, 16.7363F, 5.188F, -39.2363F),
                    RotationAngle.fromDegrees(leftForeArm, -52.4165F, 42.9971F, 31.9928F),
                    RotationAngle.fromDegrees(rightArm, -60, 0, 75),
                    RotationAngle.fromDegrees(rightForeArm, -154.2444F, 12.7F, -103.0794F),
                    RotationAngle.fromDegrees(leftLeg, 12.5F, 0, 0),
                    RotationAngle.fromDegrees(leftLowerLeg, 22.5F, 0, 0),
                    RotationAngle.fromDegrees(rightLeg, -56.6544F, 29.5657F, 5.3615F),
                    RotationAngle.fromDegrees(rightLowerLeg, 112.5F, 0, 0)
            }
        };
    }
    
    @Override
    protected void initActionPoses() {
        ModelPose<CrazyDiamondEntity> heavyPunchPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 385, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, 7.5F, 382.5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, 0, 0),
                RotationAngle.fromDegrees(leftArm, 0, 0, -22.5F),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 10),
                RotationAngle.fromDegrees(rightArm, 0, 0, 80),
                RotationAngle.fromDegrees(rightForeArm, -70, -22.5F, -30),
                RotationAngle.fromDegrees(leftLeg, 7.5F, 0, -7.5F),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, -7.5F, 0, 5),
                RotationAngle.fromDegrees(rightLowerLeg, 17.5F, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 10, 400.5F, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, 30, 390, 0),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 7.5F, 0, -45),
                RotationAngle.fromDegrees(leftForeArm, 0, 0, 17.5F),
                RotationAngle.fromDegrees(rightArm, 15, -5, 100),
                RotationAngle.fromDegrees(rightForeArm, -90, -10, -70),
                RotationAngle.fromDegrees(leftLeg, 15, 0, -7.5F),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, -12.5F, 0, 10),
                RotationAngle.fromDegrees(rightLowerLeg, 25, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 345, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, -7.5F, 337.5F, -7.5F),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, -22.5F, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -45, -15, 75),
                RotationAngle.fromDegrees(rightArm, 0, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, 0, 90, 0),
                RotationAngle.fromDegrees(leftLeg, 20, 0, -5),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, 1.6667F, -1.6667F, 34.1667F),
                RotationAngle.fromDegrees(rightLowerLeg, 26.67F, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose4 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -5, 322.5F, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, -21.25F, 282.5F, -13.75F),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, -5.62F, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -45, -15, 75),
                RotationAngle.fromDegrees(rightArm, 30, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, 0, 90, 0),
                RotationAngle.fromDegrees(leftLeg, 25, 0, -2.5F),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, 15.8334F, -3.3334F, 58.3334F),
                RotationAngle.fromDegrees(rightLowerLeg, 28.34F, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose5 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -17.5F, 300, -22.5F).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, -30, 225, -15),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 11.25F, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -45, -15, 75),
                RotationAngle.fromDegrees(rightArm, 60, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, -20, 60, -20),
                RotationAngle.fromDegrees(leftLeg, 30, 0, 0),
                RotationAngle.fromDegrees(leftLowerLeg, 0, 0, 0),
                RotationAngle.fromDegrees(rightLeg, 30, -5, 82.5F),
                RotationAngle.fromDegrees(rightLowerLeg, 30, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose6 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 15, 60, 0).noDegreesWrapping(), 
                RotationAngle.fromDegrees(body, 15, 102.5F, 0),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 45, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -45, -15, 75),
                RotationAngle.fromDegrees(rightArm, 60, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, -60, 0, -60),
                RotationAngle.fromDegrees(leftLeg, -3.3333F, 6.6667F, -6.6667F),
                RotationAngle.fromDegrees(leftLowerLeg, 45, 0, 0),
                RotationAngle.fromDegrees(rightLeg, -49.1667F, 10.8333F, 36.6667F),
                RotationAngle.fromDegrees(rightLowerLeg, 80, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose7 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 15, -7.5F), 
                RotationAngle.fromDegrees(body, 22.5F, 36.25F, -7.5F),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, -15, -90),
                RotationAngle.fromDegrees(leftForeArm, -67.5F, 0, 86.25F),
                RotationAngle.fromDegrees(rightArm, 45, 0, 75),
                RotationAngle.fromDegrees(rightForeArm, -45, -5, -120),
                RotationAngle.fromDegrees(leftLeg, -20, 10, -10),
                RotationAngle.fromDegrees(leftLowerLeg, 67.5F, 0, 0),
                RotationAngle.fromDegrees(rightLeg, -88.75F, 18.75F, 13.75F),
                RotationAngle.fromDegrees(rightLowerLeg, 105, 0, 0)
        });
        ModelPose<CrazyDiamondEntity> heavyPunchPose8 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 0, 0, 0), 
                RotationAngle.fromDegrees(body, 30, -30, -15),
                RotationAngle.fromDegrees(upperPart, 0, 7.5F, 0),
                RotationAngle.fromDegrees(leftArm, 22.5F, 15, -60),
                RotationAngle.fromDegrees(leftForeArm, -90, 15, 97.5F),
                RotationAngle.fromDegrees(rightArm, -45, -10, 75),
                RotationAngle.fromDegrees(rightForeArm, -165.665F, -4.4638F, -133.0616F),
                RotationAngle.fromDegrees(leftLeg, -76.5527F, 15.4535F, -3.9853F),
                RotationAngle.fromDegrees(leftLowerLeg, 90, 0, 0),
                RotationAngle.fromDegrees(rightLeg, 15, 30, 0),
                RotationAngle.fromDegrees(rightLowerLeg, 30, 0, 0)
        });
        actionAnim.put(StandPose.HEAVY_ATTACK, new PosedActionAnimation.Builder<CrazyDiamondEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransitionMultiple.Builder<>(heavyPunchPose1)
                        .addPose(0.2222F, heavyPunchPose2)
                        .addPose(0.3333F, heavyPunchPose3)
                        .addPose(0.4444F, heavyPunchPose4)
                        .addPose(0.5555F, heavyPunchPose5)
                        .addPose(0.7777F, heavyPunchPose6)
                        .addPose(0.8888F, heavyPunchPose7)
                        .build(heavyPunchPose8))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<>(heavyPunchPose8)
                        .addPose(0.5F, heavyPunchPose8)
                        .build(idlePose))
                .build(idlePose));
        
        
        
        IModelPose<CrazyDiamondEntity> heavyFinisherPose1 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, 5, 0, 6.36F), 
                RotationAngle.fromDegrees(body, 4.2341F, 39.7845F, 6.5861F), 
                RotationAngle.fromDegrees(upperPart, 0F, 5F, 0F), 
                RotationAngle.fromDegrees(leftArm, 15F, -10F, -52.5F),
                RotationAngle.fromDegrees(leftForeArm, -88.6703F, -3.8472F, 87.0901F),
                RotationAngle.fromDegrees(rightArm, 10.1762F, 16.6443F, 93.1445F), 
                RotationAngle.fromDegrees(rightForeArm, -77.4892F, -4.7192F, -74.0538F),
                RotationAngle.fromDegrees(leftLeg, -52.5F, -37.5F, 0),
                RotationAngle.fromDegrees(leftLowerLeg, 97.447F, -7.3536F, -2.2681F),
                RotationAngle.fromDegrees(rightLeg, 8.2781F, -2.4033F, -0.0432F),
                RotationAngle.fromDegrees(rightLowerLeg, 10, -5, 0)
        });
        IModelPose<CrazyDiamondEntity> heavyFinisherPose2 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(head, -6.9176F, 15.7939F, 16.6495F), 
                RotationAngle.fromDegrees(body, 16.7396F, 58.5251F, 19.4254F), 
                RotationAngle.fromDegrees(upperPart, 0F, 15F, 0F), 
                RotationAngle.fromDegrees(leftArm, -11.0864F, -27.2098F, -49.134F),
                RotationAngle.fromDegrees(leftForeArm, -98.9572F, -21.4891F, 114.4737F),
                RotationAngle.fromDegrees(rightArm, 37.9264F, 14.6364F, 103.3191F), 
                RotationAngle.fromDegrees(rightForeArm, -89.3397F, -34.9867F, -92.8194F),
                RotationAngle.fromDegrees(leftLeg, -36.5212F, -38.7805F, -7.0481F),
                RotationAngle.fromDegrees(leftLowerLeg, 111.7619F, 4.0651F, 10.1255F),
                RotationAngle.fromDegrees(rightLeg, 24.8305F, -0.7714F, 0),
                RotationAngle.fromDegrees(rightLowerLeg, 0.7594F, -5, 0)
        });
        IModelPose<CrazyDiamondEntity> heavyFinisherPose3 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(rightArm, -38.3F, 20.47F, 63.55F), 
                RotationAngle.fromDegrees(rightForeArm, -67.5782F, 1.503F, -72.9104F),
        });
        IModelPose<CrazyDiamondEntity> heavyFinisherPose4 = new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, -30, 0), 
                RotationAngle.fromDegrees(upperPart, 0F, -15F, 0F), 
                RotationAngle.fromDegrees(leftArm, 17.8981F, 11.9128F, -21.186F),
                RotationAngle.fromDegrees(leftForeArm, -83.3352F, 3.9942F, 28.1685F),
                RotationAngle.fromDegrees(rightArm, -72.6819F, 35.6647F, 53.5229F), 
                RotationAngle.fromDegrees(rightForeArm, 0, 0, -12.5F),
                RotationAngle.fromDegrees(leftLeg, -48.4357F, 19.6329F, 0.1075F),
                RotationAngle.fromDegrees(leftLowerLeg, 71.8824F, 15.9537F, 11.2591F),
                RotationAngle.fromDegrees(rightLeg, 33.6661F, 23.9013F, 7.2025F),
                RotationAngle.fromDegrees(rightLowerLeg, 22.5F, -5, 0)
        });

        actionAnim.put(StandPose.HEAVY_ATTACK_FINISHER, new PosedActionAnimation.Builder<CrazyDiamondEntity>()
                .addPose(StandEntityAction.Phase.WINDUP, new ModelPoseTransition<>(heavyFinisherPose1, heavyFinisherPose2))
                .addPose(StandEntityAction.Phase.PERFORM, new ModelPoseTransitionMultiple.Builder<>(heavyFinisherPose2)
                        .addPose(0.5F, heavyFinisherPose3)
                        .build(heavyFinisherPose4))
                .addPose(StandEntityAction.Phase.RECOVERY, new ModelPoseTransitionMultiple.Builder<>(heavyFinisherPose4)
                        .addPose(0.5F, heavyFinisherPose4)
                        .build(idlePose))
                .build(idlePose));
        
        
        
        RotationAngle[] itemFixRotations = new RotationAngle[] {
                RotationAngle.fromDegrees(head, 31.301F, 27.0408F, 3.6059F),
                RotationAngle.fromDegrees(body, 5.7686F, 29.8742F, 5.3807F),
                RotationAngle.fromDegrees(upperPart, 0.0F, 6.0F, 0.0F),
                RotationAngle.fromDegrees(leftArm, -33.6218F, 25.82F, -22.9983F),
                RotationAngle.fromDegrees(leftForeArm, -53.621F, -34.2195F, 50.6576F),
                RotationAngle.fromDegrees(rightArm, -45.3923F, -27.0377F, 10.4828F),
                RotationAngle.fromDegrees(rightForeArm, -38.0639F, -35.8085F, 4.6156F)
        };
        actionAnim.put(CrazyDiamondRepairItem.ITEM_FIX_POSE, new PosedActionAnimation.Builder<CrazyDiamondEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ConditionalModelPose<CrazyDiamondEntity>()
                        .addPose(stand -> !stand.isArmsOnlyMode() && stand.getUser() != null && stand.getUser().getMainArm() == HandSide.RIGHT, 
                                new ModelPose<CrazyDiamondEntity>(itemFixRotations))
                        .addPose(stand -> !stand.isArmsOnlyMode() && stand.getUser() != null && stand.getUser().getMainArm() == HandSide.LEFT, 
                                new ModelPose<CrazyDiamondEntity>(mirrorAngles(itemFixRotations)))
                        .addPose(stand -> stand.isArmsOnlyMode(), 
                                new CopyBipedUserPose<>(this))
                        )
                .build(idlePose));
        

        
        ModelAnim<CrazyDiamondEntity> armsRotationFull = (rotationAmount, entity, ticks, yRotOffsetRad, xRotRad) -> {
            float xRot = Math.min(xRotRad, 1.0467F);
            setSecondXRot(leftArm, xRot);
            setSecondXRot(rightArm, xRot);
        };
        
        RotationAngle[] blockBulletRotations = new RotationAngle[] {
                RotationAngle.fromDegrees(body, 30.7167F, 25.4083F, 17.1091F),
                RotationAngle.fromDegrees(upperPart, 0.0F, -4.0F, 0.0F),
                RotationAngle.fromDegrees(leftArm, -88.9066F, 18.1241F, -39.2738F),
                RotationAngle.fromDegrees(leftForeArm, -45.6386F, -43.0305F, 61.5635F),
                RotationAngle.fromDegrees(rightArm, -65.0702F, -23.5085F, 5.5623F),
                RotationAngle.fromDegrees(rightForeArm, -97.8419F, 36.1268F, -102.0079F),
                RotationAngle.fromDegrees(leftLeg, -50.8435F, -8.788F, -8.0132F),
                RotationAngle.fromDegrees(leftLowerLeg, 97.5F, 10, 0),
                RotationAngle.fromDegrees(rightLeg, 7.76F, -2.1895F, -3.2001F),
                RotationAngle.fromDegrees(rightLowerLeg, 10, -5, 0)
        };
        actionAnim.put(CrazyDiamondBlockBullet.BLOCK_BULLET_SHOT_POSE, new PosedActionAnimation.Builder<CrazyDiamondEntity>()
                .addPose(StandEntityAction.Phase.BUTTON_HOLD, new ConditionalModelPose<CrazyDiamondEntity>()
                        .addPose(stand -> !stand.isArmsOnlyMode() && stand.getUser() != null && stand.getUser().getMainArm() == HandSide.RIGHT, 
                                new ModelPose<CrazyDiamondEntity>(mirrorAngles(blockBulletRotations))
                                .setAdditionalAnim(armsRotationFull))
                        .addPose(stand -> !stand.isArmsOnlyMode() && stand.getUser() != null && stand.getUser().getMainArm() == HandSide.LEFT, 
                                new ModelPose<CrazyDiamondEntity>(blockBulletRotations)
                                .setAdditionalAnim(armsRotationFull))
                        .addPose(stand -> stand.isArmsOnlyMode(), 
                                new CopyBipedUserPose<>(this))
                        )
                .build(idlePose));
        
        super.initActionPoses();
    }
    
    @Override
    protected ModelPose<CrazyDiamondEntity> initIdlePose() {
        return new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(body, 5.7686F, 29.8742F, 5.3807F),
                RotationAngle.fromDegrees(upperPart, 0, 6, 0),
                RotationAngle.fromDegrees(leftArm, 3.25F, -6.25F, -42.5F),
                RotationAngle.fromDegrees(leftForeArm, -75, -15, 92.5F),
                RotationAngle.fromDegrees(rightArm, 35, -15, 40),
                RotationAngle.fromDegrees(rightForeArm, -85, -5, -20),
                RotationAngle.fromDegrees(leftLeg, -52.5F, -15, 0),
                RotationAngle.fromDegrees(leftLowerLeg, 97.5F, 10, 0),
                RotationAngle.fromDegrees(rightLeg, 7.9315F, -12.0964F, -4.5742F),
                RotationAngle.fromDegrees(rightLowerLeg, 10, -5, 0)
        });
    }

    @Override
    protected ModelPose<CrazyDiamondEntity> initIdlePose2Loop() {
        return new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(leftArm, 7.9708F, -6.7104F, -40.0269F),
                RotationAngle.fromDegrees(leftForeArm, -74.8671F, -9.523F, 91.3614F),
                RotationAngle.fromDegrees(rightArm, 40.9054F, -11.7546F, 36.0897F),
                RotationAngle.fromDegrees(rightForeArm, -92.4423F, -9.9808F, -20.4419F)
        });
    }
}