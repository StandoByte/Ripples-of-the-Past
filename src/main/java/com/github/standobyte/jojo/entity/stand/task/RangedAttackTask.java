package com.github.standobyte.jojo.entity.stand.task;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandSoundPacket.StandSoundType;

public class RangedAttackTask extends StandEntityTask {
    private final boolean held;
    
    public RangedAttackTask(StandEntity standEntity, boolean shift, boolean held) {
        super(held ? Integer.MAX_VALUE : standEntity.rangedAttackDuration(shift), shift, standEntity);
        this.held = held;
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
        standEntity.setNoPhysics(false);
        standEntity.setRelativePos(0, 0.5);
        standEntity.setUserMovementFactor(0.2F);
        standEntity.setStandPose(StandPose.RANGED_ATTACK);
        standEntity.playStandSound(StandSoundType.RANGED_ATTACK);
    }

    @Override
    protected void tick() {
        standEntity.rangedAttackTick(ticksLeft, shift);
    }
    
    @Override
    public boolean canClearMidway() {
        return held;
    }
    
    @Override
    protected void onClear() {
        standEntity.setUserMovementFactor(1F);
    }
}
