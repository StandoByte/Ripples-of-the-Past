package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.render.entity.pose.ModelPose;
import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.entity.stand.stands.GoldExperienceEntity;

import net.minecraft.client.renderer.model.ModelRenderer;

public class GoldExperienceModel extends HumanoidStandModel<GoldExperienceEntity> {
    private ModelRenderer theThing;
    private ModelRenderer rightString;
    private ModelRenderer leftString;
    private ModelRenderer loincloth;
    private ModelRenderer leftPartLoincloth;
    private ModelRenderer rightPartLoincloth;

    public GoldExperienceModel() {
        super();
        
        BlockbenchStandModelHelper.fillFromBlockbenchExport(new GoldExperienceModelExported(), this);

    }

    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
            new RotationAngle[] {
                    RotationAngle.fromDegrees(head,                 14.0902, -21.0881, -6.187),
                    RotationAngle.fromDegrees(body,                 0, 15, 0),
                    RotationAngle.fromDegrees(leftArm,              -1.1823, 3.2261, -47.328),
                    RotationAngle.fromDegrees(leftForeArm,          -15.1888, 61.175, 81.1746),
                    RotationAngle.fromDegrees(rightArm,             -11.5806, -15.1632, 22.7873),
                    RotationAngle.fromDegrees(rightForeArm,         -16.9047, -32.101, -27.1114),
                    RotationAngle.fromDegrees(loincloth,            7.5, 0, 0),
                    RotationAngle.fromDegrees(leftPartLoincloth,    0, -10, 0),
                    RotationAngle.fromDegrees(rightPartLoincloth,   0, 7.5, 0),
                    RotationAngle.fromDegrees(theThing,             -5.0047, 2.4905, -0.218),
                    RotationAngle.fromDegrees(leftString,           -11.2852, -61.6899, -55.0999),
                    RotationAngle.fromDegrees(rightString,          -13.2247, 50.6876, 54.2581),
                    RotationAngle.fromDegrees(rightLeg,             -47.268, 43.4069, 12.1778),
                    RotationAngle.fromDegrees(rightLowerLeg,        65.4807, 2.3096, -0.9572),
                    RotationAngle.fromDegrees(leftLeg,              -48.9063, -49.5698, -12.3822),
                    RotationAngle.fromDegrees(leftLowerLeg,         82.726, -3.594, -3.7696),
            },
            mirrorAngles(
                    new RotationAngle[] {
                            RotationAngle.fromDegrees(head,                 -10.9804, -0.3135, -5.4927),
                            RotationAngle.fromDegrees(body,                 15, 22.5, -15),
                            RotationAngle.fromDegrees(upperPart,            0, -11, 0),
                            RotationAngle.fromDegrees(leftArm,              -47.5957, -18.3992, -2.2032),
                            RotationAngle.fromDegrees(leftForeArm,          51.967, 69.0992, 114.4655),
                            RotationAngle.fromDegrees(rightArm,             -173.2631, 16.6758, -22.4834),
                            RotationAngle.fromDegrees(rightForeArm,         -93.2512, -40.4911, -89.5878),
                            RotationAngle.fromDegrees(loincloth,            52, 0, 0),
                            RotationAngle.fromDegrees(leftPartLoincloth,    0, -10, 0),
                            RotationAngle.fromDegrees(rightPartLoincloth,   0, 28, 0),
                            RotationAngle.fromDegrees(theThing,             -30.5422, 25.1869, 6.1914),
                            RotationAngle.fromDegrees(leftString,           -36.9094, 19.7491, 8.621),
                            RotationAngle.fromDegrees(rightString,          6.9471, 4.0921, 19.6458),
                            RotationAngle.fromDegrees(rightLeg,             26.5762, 23.7591, 14.3429),
                            RotationAngle.fromDegrees(rightLowerLeg,        16.0563, 1.326, -16.064),
                            RotationAngle.fromDegrees(leftLeg,              -43.0643, 1.8553, -0.9721),
                            RotationAngle.fromDegrees(leftLowerLeg,         120.6111, -0.2917, -3.0041),
                    }
            ),
            
        };
    }
    
    @Override
    protected void initActionPoses() {
        
        super.initActionPoses();
    }
    
    @Override
    protected ModelPose<GoldExperienceEntity> initIdlePose() {
        return new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(body, 0, 15, 0),
                RotationAngle.fromDegrees(upperPart, 0, 5, 0),
                RotationAngle.fromDegrees(leftArm, 53.65728, 76.34635, 8.92854),
                RotationAngle.fromDegrees(leftForeArm, -87.80728, -3.84393, -18.31017),
                RotationAngle.fromDegrees(rightArm, 1.22652, -31.70199, 12.47708),
                RotationAngle.fromDegrees(rightForeArm, -13.89254, 1.92508, 2.30131),
                RotationAngle.fromDegrees(leftLeg, -6.49097, -14.35901, -4.47622),
                RotationAngle.fromDegrees(leftLowerLeg, 14.7095, 0, 0),
                RotationAngle.fromDegrees(leftString, -1.49286, -10.37279, -9.54087),
                RotationAngle.fromDegrees(rightLeg, -25.10307, 44.08314, 12.38262),
                RotationAngle.fromDegrees(rightLowerLeg, 77.50415, 0, 0),
                RotationAngle.fromDegrees(rightString, -9.2073, 20.60715, 29.6771),
                RotationAngle.fromDegrees(theThing, -10.01153, 2.4407, -0.54143),
                RotationAngle.fromDegrees(loincloth, 24, 0, 0),
                RotationAngle.fromDegrees(leftPartLoincloth, 0, -4, 0),
                RotationAngle.fromDegrees(rightPartLoincloth, 0, 12, 0),
        });
    }
    
    @Override
    protected ModelPose<GoldExperienceEntity> initIdlePose2Loop() {
        return new ModelPose<>(new RotationAngle[] {
                RotationAngle.fromDegrees(upperPart, 0, 7.5, 0),
                RotationAngle.fromDegrees(leftArm, 42.9155, 78.27327, -9.53462),
                RotationAngle.fromDegrees(leftForeArm, -99.00384, 2.77468, -2.9641),
                RotationAngle.fromDegrees(rightArm, -0.34201, -26.72472, 15.95522),
                RotationAngle.fromDegrees(rightForeArm, -7.96995, -0.695, -4.95158),
                RotationAngle.fromDegrees(leftLeg, -1.96205, -20.53064, -4.59351),
                RotationAngle.fromDegrees(leftLowerLeg, 6.4346, 0, 0),
                RotationAngle.fromDegrees(leftString, -1.49286, -10.37279, -9.54087),
                RotationAngle.fromDegrees(rightLeg, -30.2726, 46.96286, 14.64503),
                RotationAngle.fromDegrees(rightLowerLeg, 90, 0, 0),
                RotationAngle.fromDegrees(rightString, -13.99938, 26.59254, 32.3964),
                RotationAngle.fromDegrees(theThing, -13.20976, -6.90155, -1.70244),
                RotationAngle.fromDegrees(loincloth, 25, 0, 0),
                RotationAngle.fromDegrees(leftPartLoincloth, 0, -14, 0),
                RotationAngle.fromDegrees(rightPartLoincloth, 0, 14, 0),
        });
    }
    
    
    
    @Override
    protected void initOpposites() {
        super.initOpposites();
        oppositeHandside.put(leftPartLoincloth, rightPartLoincloth);
        oppositeHandside.put(leftString, rightString);
    }
    
    @Override
    protected ModelPose<GoldExperienceEntity> initPoseReset() {
        return super.initPoseReset()
                .putRotation(new RotationAngle(loincloth, 0, 0, 0))
                .putRotation(new RotationAngle(leftPartLoincloth, 0, 0, 0))
                .putRotation(new RotationAngle(rightPartLoincloth, 0, 0, 0))
                .putRotation(new RotationAngle(theThing, 0, 0, 0))
                .putRotation(new RotationAngle(leftString, 0, 0, 0))
                .putRotation(new RotationAngle(rightString, 0, 0, 0));
    }
}