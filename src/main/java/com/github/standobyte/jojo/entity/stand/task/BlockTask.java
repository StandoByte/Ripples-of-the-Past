package com.github.standobyte.jojo.entity.stand.task;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;

public class BlockTask extends StandEntityTask {

    public BlockTask(int ticks, StandEntity standEntity) {
        super(ticks, false, standEntity);
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
        standEntity.setUserMovementFactor(0.2F);
        standEntity.setNoPhysics(false);
        standEntity.setStandPose(StandPose.BLOCK);
    }
    
    @Override
    protected void onClear() {
        standEntity.setUserMovementFactor(1F);
    }
}
