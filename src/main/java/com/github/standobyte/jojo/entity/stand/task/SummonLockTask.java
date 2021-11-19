package com.github.standobyte.jojo.entity.stand.task;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;

public class SummonLockTask extends StandEntityTask {

    public SummonLockTask(StandEntity standEntity) {
        super(standEntity.getType().getStats().getSummonTicks(), false, standEntity);
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
        standEntity.setStandPose(StandPose.SUMMON);
    }
    
    @Override
    protected void tick() {
        standEntity.setAlpha(1F - (float) (ticksLeft - 1) / (float) ticks);
    }
    
    @Override
    public boolean canClearMidway() {
        return false;
    }
    
    @Override
    public boolean resetPoseOnClear() {
        return false;
    }

}
