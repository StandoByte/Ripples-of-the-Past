package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.client.render.entity.model.stand.bb.BlockbenchStandModelHelper;
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
        
        BlockbenchStandModelHelper.partsFromBlockbenchExport(new GoldExperienceModelExported(), this);

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
    
//    @Override
//    protected ModelPose<CrazyDiamondEntity> initIdlePose() {
//    }
//
//    @Override
//    protected ModelPose<CrazyDiamondEntity> initIdlePose2Loop() {
//    }


    
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