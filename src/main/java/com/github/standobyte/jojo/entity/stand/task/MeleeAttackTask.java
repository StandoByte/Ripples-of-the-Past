package com.github.standobyte.jojo.entity.stand.task;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandSoundPacket.StandSoundType;

public class MeleeAttackTask extends StandEntityTask {
    private boolean punchDelayed = false;
    private int delayedPunches = 0;

    public MeleeAttackTask(StandEntity standEntity, boolean shift) {
        super(shift ? Integer.MAX_VALUE : Math.max((int) (standEntity.getTicksForSinglePunch()), 1), shift, standEntity);
    }
    
    @Override
    public void afterInit() {
        super.afterInit();
        standEntity.setNoPhysics(false);
        standEntity.setStandPose(StandPose.NONE);
        if (standEntity.isArmsOnlyMode()) {
            standEntity.setRelativePos(0, 0.1);
            standEntity.setRelativeY(0);
        }
        else if (shift) {
            standEntity.setRelativePos(0, 0.5);
        }
        if (shift) {
            standEntity.setUserMovementFactor(0.2F);
        }
        standEntity.playStandSound(shift ? StandSoundType.MELEE_BARRAGE : StandSoundType.MELEE_ATTACK);
    }

    @Override
    protected void tick() {
        if (shift) {
            double attackSpeed = standEntity.getAttackSpeed();
            int extraTickSwings = (int) (attackSpeed / 20D);
            for (int i = 0; i < extraTickSwings; i++) {
                standEntity.swingAlternateHands();
                standEntity.punch(false);
            }
            
            if (punchDelayed) {
                punchDelayed = false;
                standEntity.swingAlternateHands();
                standEntity.punch(false);
            }
            else {
                double sp2 = attackSpeed % 20D;
                if (sp2 > 0) {
                    double ticksInterval = 20 / sp2;
                    int intTicksInterval = (int) ticksInterval;
                    if ((ticks - ticksLeft + delayedPunches) % intTicksInterval == 0) {
                        double delayProb = ticksInterval - intTicksInterval;
                        if (standEntity.getRandom().nextDouble() < delayProb) {
                            punchDelayed = true;
                            delayedPunches++;
                        }
                        else {
                            standEntity.swingAlternateHands();
                            standEntity.punch(false);
                        }
                    }
                }
            }
        }
        else {
            if (ticksLeft == ticks) {
                standEntity.swingAlternateHands();
            }
            if (ticksLeft == 1) {
                standEntity.punch(true);
            }
        }
    }
    
    @Override
    public boolean canClearMidway() {
        return shift;
    }

    @Override
    protected void onClear() {
        if (shift) {
            standEntity.setUserMovementFactor(1F);
            standEntity.stopStandSound(StandSoundType.MELEE_BARRAGE);
        }
    }
}
