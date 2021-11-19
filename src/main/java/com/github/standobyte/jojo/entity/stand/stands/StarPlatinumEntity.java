package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SPStarFingerEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.ModActions;

import net.minecraft.world.World;

public class StarPlatinumEntity extends StandEntity {
    private SPStarFingerEntity starFinger;
    
    public StarPlatinumEntity(StandEntityType<StarPlatinumEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void rangedAttackTick(int ticks, boolean shift) {
        if (ticks == 0 && starFinger == null) {
            starFinger = new SPStarFingerEntity(level, this);
            starFinger.setDamageFactor(rangeEfficiencyFactor());
            level.addFreshEntity(starFinger);
        }
    }
    
    @Override
    protected boolean clearTask(boolean resetPos) {
        if (super.clearTask(resetPos)) {
            if (starFinger != null && starFinger.isAlive()) {
                starFinger.remove();
            }
            starFinger = null;
            return true;
        }
        return false;
    }

    @Override
    public int rangedAttackDuration(boolean shift) {
        return ModActions.STAR_PLATINUM_STAR_FINGER.get().getCooldownValue();
    }
}
