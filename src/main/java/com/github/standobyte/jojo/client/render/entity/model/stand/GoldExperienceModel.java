package com.github.standobyte.jojo.client.render.entity.model.stand;

import com.github.standobyte.jojo.client.render.entity.pose.RotationAngle;
import com.github.standobyte.jojo.entity.stand.stands.GoldExperienceEntity;

// Made with Blockbench 4.1.3


public class GoldExperienceModel extends HumanoidStandModel<GoldExperienceEntity> {

    public GoldExperienceModel() {
        super();
        
        addHumanoidBaseBoxes(null);
        texWidth = 128;
        texHeight = 128;

    }

    @Override
    protected RotationAngle[][] initSummonPoseRotations() {
        return new RotationAngle[][] {
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
}