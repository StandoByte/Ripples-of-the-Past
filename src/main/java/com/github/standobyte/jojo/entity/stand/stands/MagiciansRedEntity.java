package com.github.standobyte.jojo.entity.stand.stands;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.world.World;

public class MagiciansRedEntity extends StandEntity {
    
    public MagiciansRedEntity(StandEntityType<MagiciansRedEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public boolean attackEntity(Supplier<Boolean> doAttack, StandEntityPunch punch, StandEntityTask task) {
        return DamageUtil.dealDamageAndSetOnFire(punch.target, 
                entity -> super.attackEntity(doAttack, punch, task), 10, true);
    }
    
    @Override
    public void playStandSummonSound() {
        if (!isArmsOnlyMode()) {
            super.playStandSummonSound();
        }
    }
}
